package mtg.app.feature.chat.infrastructure

import mtg.app.feature.chat.data.MessagesDataSource
import mtg.app.feature.chat.data.toChatMessages
import mtg.app.feature.chat.data.toChatMeta
import mtg.app.feature.chat.data.toUserMatchThreads
import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.MessageThread
import mtg.app.feature.chat.domain.UserRatingSummary
import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer
import mtg.app.feature.chat.infrastructure.service.MessagesService
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class DefaultMessagesDataSource(
    private val service: MessagesService,
) : MessagesDataSource {
    override suspend fun loadUserNickname(uid: String, idToken: String): String? {
        return service.loadUserNickname(uid = uid, idToken = idToken)
    }

    override suspend fun loadThreads(uid: String, idToken: String): List<MessageThread> {
        val nicknameCache = mutableMapOf<String, String>()

        suspend fun resolveNickname(counterpartUid: String): String {
            if (counterpartUid.isBlank()) return ""
            nicknameCache[counterpartUid]?.let { return it }
            val resolved = service.loadUserNickname(uid = counterpartUid, idToken = idToken)
                ?.trim()
                .orEmpty()
            nicknameCache[counterpartUid] = resolved
            return resolved
        }

        val baseThreads = service.listUserMatches(uid = uid, idToken = idToken).toUserMatchThreads()
        val enrichedBase = baseThreads.map { thread ->
            val meta = service.getChatMeta(chatId = thread.chatId, idToken = idToken)?.toChatMeta(chatId = thread.chatId)
            val nickname = resolveNickname(thread.counterpartUid)
            thread.copy(
                cardId = if (thread.cardId.isNotBlank()) thread.cardId else meta?.cardId.orEmpty(),
                cardName = if (thread.cardName.isNotBlank()) thread.cardName else meta?.cardName.orEmpty(),
                counterpartNickname = nickname.ifBlank { thread.counterpartNickname },
                lastMessage = meta?.lastMessage,
                lastMessageAt = meta?.lastMessageAt ?: thread.lastMessageAt,
            )
        }

        val fallbackThreads = service.listChats(idToken = idToken).entries.mapNotNull { (chatId, chatNode) ->
            val rootObj = chatNode as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
            val metaObj = rootObj["meta"] as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
            val meta = metaObj.toChatMeta(chatId = chatId) ?: return@mapNotNull null
            if (meta.buyerUid != uid && meta.sellerUid != uid) return@mapNotNull null

            val isBuyer = meta.buyerUid == uid
            val counterpartUid = if (isBuyer) meta.sellerUid else meta.buyerUid
            val counterpartEmail = if (isBuyer) meta.sellerEmail else meta.buyerEmail
            val counterpartNickname = resolveNickname(counterpartUid)
            MessageThread(
                chatId = meta.chatId,
                counterpartUid = counterpartUid,
                counterpartEmail = counterpartEmail,
                counterpartNickname = counterpartNickname,
                cardId = meta.cardId,
                cardName = meta.cardName,
                role = if (isBuyer) "buyer" else "seller",
                lastMessage = meta.lastMessage,
                lastMessageAt = meta.lastMessageAt ?: meta.createdAt,
            )
        }

        return (enrichedBase + fallbackThreads)
            .distinctBy { it.chatId }
            .sortedByDescending { it.lastMessageAt }
    }

    override suspend fun deleteThread(uid: String, idToken: String, chatId: String, counterpartUid: String) {
        service.deleteThread(
            uid = uid,
            idToken = idToken,
            chatId = chatId,
            counterpartUid = counterpartUid,
        )
    }

    override suspend fun loadChatMeta(chatId: String, idToken: String): ChatMeta? {
        return service.getChatMeta(chatId = chatId, idToken = idToken)?.toChatMeta(chatId = chatId)
    }

    override suspend fun loadChatMessages(chatId: String, idToken: String): List<ChatMessage> {
        return service.listChatMessages(chatId = chatId, idToken = idToken).toChatMessages()
    }

    override suspend fun sendMessage(
        uid: String,
        idToken: String,
        chatId: String,
        senderEmail: String,
        text: String,
    ) {
        service.sendMessage(
            uid = uid,
            idToken = idToken,
            chatId = chatId,
            senderEmail = senderEmail,
            text = text,
        )
    }

    override suspend fun proposeDeal(uid: String, idToken: String, chatId: String) {
        service.proposeDeal(uid = uid, chatId = chatId, idToken = idToken)
    }

    override suspend fun confirmDeal(uid: String, idToken: String, chatId: String) {
        service.confirmDeal(uid = uid, chatId = chatId, idToken = idToken)
    }

    override suspend fun hasRatedChat(uid: String, idToken: String, chatId: String): Boolean {
        val chatMeta = service.getChatMeta(chatId = chatId, idToken = idToken)
            ?: return service.hasRatedChat(uid = uid, chatId = chatId, idToken = idToken)
        val tradeKey = buildTradeRatingKey(chatId = chatId, chatMeta = chatMeta)
        return service.hasRatedChat(uid = uid, chatId = tradeKey, idToken = idToken)
    }

    override suspend fun loadUserRatingSummary(uid: String, idToken: String): UserRatingSummary {
        val received = service.loadUserReceivedRatings(uid = uid, idToken = idToken)
        val scores = received.values.mapNotNull { value ->
            val obj = value as? JsonObject ?: return@mapNotNull null
            val primitive = obj["score"] as? JsonPrimitive ?: return@mapNotNull null
            primitive.content.toIntOrNull()
        }
        if (scores.isEmpty()) {
            return UserRatingSummary(average = 0.0, count = 0)
        }
        val average = scores.average()
        return UserRatingSummary(average = average, count = scores.size)
    }

    override suspend fun loadUserReviews(uid: String, idToken: String): List<UserReview> {
        val received = service.loadUserReceivedRatings(uid = uid, idToken = idToken)
        return received.values.mapNotNull { value ->
            val obj = value as? JsonObject ?: return@mapNotNull null
            val score = (obj["score"] as? JsonPrimitive)?.content?.toIntOrNull() ?: return@mapNotNull null
            val comment = (obj["comment"] as? JsonPrimitive)?.content.orEmpty()
            val createdAt = (obj["createdAt"] as? JsonPrimitive)?.content?.toLongOrNull() ?: 0L
            val raterUid = (obj["raterUid"] as? JsonPrimitive)?.content.orEmpty()
            UserReview(
                raterUid = raterUid,
                score = score,
                comment = comment,
                createdAt = createdAt,
            )
        }.sortedByDescending { it.createdAt }
    }

    override suspend fun loadUserSellOffers(uid: String, idToken: String): List<UserSellOffer> {
        val entries = service.listMarketplaceSellEntriesByUser(uid = uid, idToken = idToken)
        return entries.values.mapNotNull { raw ->
            raw as? JsonObject
        }.groupBy { entry ->
            val cardId = (entry["cardId"] as? JsonPrimitive)?.content.orEmpty()
            val cardName = (entry["cardName"] as? JsonPrimitive)?.content.orEmpty()
            cardId.ifBlank { cardName.trim().lowercase() }
        }.values.mapNotNull { group ->
            val first = group.firstOrNull() ?: return@mapNotNull null
            val cardId = (first["cardId"] as? JsonPrimitive)?.content.orEmpty()
            val cardName = (first["cardName"] as? JsonPrimitive)?.content.orEmpty()
            val cardTypeLine = (first["cardTypeLine"] as? JsonPrimitive)?.content.orEmpty()
            val imageUrl = first.stringOrNull("artImageUrl")
                ?: first.stringOrNull("cardImageUrl")
                ?: first.stringOrNull("imageUrl")
            val fromPrice = group.mapNotNull { item ->
                (item["price"] as? JsonPrimitive)?.content?.toDoubleOrNull()
            }.minOrNull()
            UserSellOffer(
                cardId = cardId.ifBlank { cardName },
                cardName = cardName,
                cardTypeLine = cardTypeLine,
                imageUrl = imageUrl,
                offerCount = group.size,
                fromPrice = fromPrice,
            )
        }.sortedBy { it.cardName.lowercase() }
    }

    override suspend fun submitRating(
        raterUid: String,
        ratedUid: String,
        chatId: String,
        idToken: String,
        score: Int,
        comment: String,
    ) {
        if (raterUid == ratedUid) throw IllegalStateException("Cannot rate yourself")
        val clampedScore = score.coerceIn(1, 5)
        val chatMeta = service.getChatMeta(chatId = chatId, idToken = idToken)
            ?: throw IllegalStateException("Chat not found")
        val dealStatus = (chatMeta["dealStatus"] as? JsonPrimitive)?.content.orEmpty()
        if (!dealStatus.equals("COMPLETED", ignoreCase = true)) {
            throw IllegalStateException("Trade must be completed before rating")
        }
        val tradeKey = buildTradeRatingKey(chatId = chatId, chatMeta = chatMeta)
        val hasRated = service.hasRatedChat(uid = raterUid, chatId = tradeKey, idToken = idToken)
        if (hasRated) throw IllegalStateException("You already rated this trade")
        service.submitRating(
            chatId = chatId,
            idToken = idToken,
            ratedUid = ratedUid,
            score = clampedScore,
            comment = comment.trim().take(300),
        )
    }

    private fun JsonObject?.stringOrNull(key: String): String? {
        val element = this?.get(key) ?: return null
        if (element is JsonNull) return null
        val primitive = element as? JsonPrimitive ?: return null
        return runCatching { primitive.content }
            .getOrNull()
            ?.trim()
            ?.takeUnless { it.isBlank() }
    }

    private fun buildTradeRatingKey(chatId: String, chatMeta: JsonObject): String {
        val closedAt = (chatMeta["closedAt"] as? JsonPrimitive)?.content?.toLongOrNull()
        if (closedAt != null && closedAt > 0L) return "${chatId}_$closedAt"

        val createdAt = (chatMeta["createdAt"] as? JsonPrimitive)?.content?.toLongOrNull()
        if (createdAt != null && createdAt > 0L) return "${chatId}_$createdAt"

        return chatId
    }
}
