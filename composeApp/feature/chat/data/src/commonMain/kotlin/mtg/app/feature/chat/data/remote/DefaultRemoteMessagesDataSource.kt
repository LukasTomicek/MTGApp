package mtg.app.feature.chat.data.remote

import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import mtg.app.core.data.remote.ApiCallHandler
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.chat.data.MessagesDataSource
import mtg.app.feature.chat.data.remote.dto.ChatMetaDto
import mtg.app.feature.chat.data.remote.dto.ChatMessageDto
import mtg.app.feature.chat.data.remote.dto.ChatNodeDto
import mtg.app.feature.chat.data.remote.dto.NicknameResponseDto
import mtg.app.feature.chat.data.remote.dto.PatchChatDealRequestDto
import mtg.app.feature.chat.data.remote.dto.RatingExistsResponseDto
import mtg.app.feature.chat.data.remote.dto.SendMessageRequestDto
import mtg.app.feature.chat.data.remote.dto.SubmitRatingRequestDto
import mtg.app.feature.chat.data.remote.dto.UserMatchThreadDto
import mtg.app.feature.chat.data.remote.dto.UserReviewDto
import mtg.app.feature.chat.data.remote.dto.UserSellOfferDto
import mtg.app.feature.chat.data.remote.dto.buildTradeRatingKey
import mtg.app.feature.chat.data.remote.dto.toChatMessages
import mtg.app.feature.chat.data.remote.dto.toDomain
import mtg.app.feature.chat.data.remote.dto.toMessageThreads
import mtg.app.feature.chat.data.remote.dto.toUserReviews
import mtg.app.feature.chat.data.remote.dto.toUserSellOffers
import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.MessageThread
import mtg.app.feature.chat.domain.UserRatingSummary
import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer
import mtg.app.feature.chat.domain.obj.DeleteChatThreadRequest
import mtg.app.feature.chat.domain.obj.SendChatMessageRequest
import mtg.app.feature.chat.domain.obj.SubmitChatRatingRequest

class DefaultRemoteMessagesDataSource(
    private val apiCallHandler: ApiCallHandler,
) : MessagesDataSource {
    override suspend fun loadUserNickname(uid: String): String? {
        val response = apiCallHandler.backendRequest(
            path = "/v1/users/profile/${uid}",
            requiresAuth = false,
        )
        if (!response.status.isSuccess()) return null
        return response.body<NicknameResponseDto>().nickname
            ?.trim()
            ?.takeUnless { it.isBlank() }
    }

    override suspend fun loadThreads(context: AuthContext): List<MessageThread> {
        val nicknameCache = mutableMapOf<String, String>()

        suspend fun resolveNickname(counterpartUid: String): String {
            if (counterpartUid.isBlank()) return ""
            nicknameCache[counterpartUid]?.let { return it }
            val resolved = loadUserNickname(uid = counterpartUid)
                ?.trim()
                .orEmpty()
            nicknameCache[counterpartUid] = resolved
            return resolved
        }

        println("TradeBE: calling /v1/users/me/matches")
        val baseThreads = apiCallHandler.apiRequest<Map<String, UserMatchThreadDto>>(
            path = "/v1/users/me/matches",
            idToken = context.idToken,
        ).toMessageThreads()

        val enrichedBase = baseThreads.map { thread ->
            val meta = loadChatMeta(context = context, chatId = thread.chatId)
            val nickname = resolveNickname(thread.counterpartUid)
            thread.copy(
                cardId = if (thread.cardId.isNotBlank()) thread.cardId else meta?.cardId.orEmpty(),
                cardName = if (thread.cardName.isNotBlank()) thread.cardName else meta?.cardName.orEmpty(),
                counterpartNickname = nickname.ifBlank { thread.counterpartNickname },
                lastMessage = meta?.lastMessage,
                lastMessageAt = meta?.lastMessageAt ?: thread.lastMessageAt,
            )
        }

        println("TradeBE: calling /v1/chats")
        val fallbackThreads = apiCallHandler.apiRequest<Map<String, ChatNodeDto>>(
            path = "/v1/chats",
            idToken = context.idToken,
        ).entries.mapNotNull { (chatId, chatNode) ->
            val meta = chatNode.meta?.toDomain(fallbackChatId = chatId) ?: return@mapNotNull null
            if (meta.buyerUid != context.uid && meta.sellerUid != context.uid) return@mapNotNull null

            val isBuyer = meta.buyerUid == context.uid
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

    override suspend fun deleteThread(context: AuthContext, request: DeleteChatThreadRequest) {
        val chatId = request.chatId
        println("TradeBE: calling DELETE /v1/chats/${chatId}")
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/chats/${chatId}",
            method = HttpMethod.Delete,
            idToken = context.idToken,
        )
    }

    override suspend fun loadChatMeta(context: AuthContext, chatId: String): ChatMeta? {
        println("TradeBE: calling /v1/chats/${chatId}")
        val response = apiCallHandler.backendRequest(
            path = "/v1/chats/${chatId}",
            idToken = context.idToken,
        )
        if (response.status.value == 404) return null
        apiCallHandler.requireSuccess(response, "GET /v1/chats/${chatId}")
        return response.body<ChatMetaDto>().toDomain(fallbackChatId = chatId)
    }

    override suspend fun loadChatMessages(context: AuthContext, chatId: String): List<ChatMessage> {
        println("TradeBE: calling /v1/chats/${chatId}/messages")
        return apiCallHandler.apiRequest<Map<String, ChatMessageDto>>(
            path = "/v1/chats/${chatId}/messages",
            idToken = context.idToken,
        ).toChatMessages()
    }

    override suspend fun sendMessage(context: AuthContext, request: SendChatMessageRequest) {
        val chatId = request.chatId
        val normalizedText = request.text.trim()
        if (normalizedText.isBlank()) return

        val senderDisplayName = runCatching {
            loadUserNickname(uid = context.uid)
        }.onFailure { throwable ->
            println("TradeBE: loadUserNickname failed before send, fallback to uid. error=${throwable.message}")
        }.getOrNull()
            ?.trim()
            .orEmpty()
            .ifBlank { request.senderEmail.trim().ifBlank { context.uid } }

        println("TradeBE: calling /v1/chats/${chatId}/messages")
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/chats/${chatId}/messages",
            method = HttpMethod.Post,
            idToken = context.idToken,
            body = SendMessageRequestDto(
                senderDisplayName = senderDisplayName,
                text = normalizedText,
            ),
        )
    }

    override suspend fun proposeDeal(context: AuthContext, chatId: String) {
        val meta = rawChatMeta(chatId = chatId, idToken = context.idToken)
            ?: throw IllegalStateException("Chat not found")
        val buyerUid = meta.buyerUid.trim()
        if (context.uid != buyerUid) {
            throw IllegalStateException("Only buyer can reserve deal")
        }

        apiCallHandler.apiRequest<Unit>(
            path = "/v1/chats/${chatId}",
            method = HttpMethod.Patch,
            idToken = context.idToken,
            body = PatchChatDealRequestDto(
                dealStatus = "PROPOSED",
                buyerConfirmed = true,
                sellerConfirmed = false,
                buyerCompleted = false,
                sellerCompleted = false,
            ),
        )
    }

    override suspend fun confirmDeal(context: AuthContext, chatId: String) {
        val meta = rawChatMeta(chatId = chatId, idToken = context.idToken)
            ?: throw IllegalStateException("Chat not found")
        val buyerUid = meta.buyerUid.trim()
        val sellerUid = meta.sellerUid.trim()
        if (context.uid != buyerUid && context.uid != sellerUid) {
            throw IllegalStateException("Only chat participants can confirm deal")
        }

        val dealStatus = meta.dealStatus.orEmpty()
        if (!dealStatus.equals("PROPOSED", ignoreCase = true)) {
            throw IllegalStateException("Deal must be in PROPOSED state")
        }
        val buyerConfirmed = meta.buyerConfirmed
        val sellerConfirmed = meta.sellerConfirmed
        val buyerCompleted = meta.buyerCompleted
        val sellerCompleted = meta.sellerCompleted
        if (!buyerConfirmed) {
            throw IllegalStateException("Buyer must reserve deal first")
        }

        if (!sellerConfirmed) {
            if (context.uid != sellerUid) {
                throw IllegalStateException("Only seller can confirm reserved deal")
            }
            apiCallHandler.apiRequest<Unit>(
                path = "/v1/chats/${chatId}",
                method = HttpMethod.Patch,
                idToken = context.idToken,
                body = PatchChatDealRequestDto(
                    dealStatus = "PROPOSED",
                    buyerConfirmed = true,
                    sellerConfirmed = true,
                    buyerCompleted = false,
                    sellerCompleted = false,
                ),
            )
            return
        }

        val markBuyerCompleted = if (context.uid == buyerUid) true else buyerCompleted
        val markSellerCompleted = if (context.uid == sellerUid) true else sellerCompleted
        val shouldFinalize = markBuyerCompleted && markSellerCompleted
        val completeStatus = if (shouldFinalize) "COMPLETED" else "PROPOSED"

        apiCallHandler.apiRequest<Unit>(
            path = "/v1/chats/${chatId}",
            method = HttpMethod.Patch,
            idToken = context.idToken,
            body = PatchChatDealRequestDto(
                dealStatus = completeStatus,
                buyerConfirmed = true,
                sellerConfirmed = true,
                buyerCompleted = markBuyerCompleted,
                sellerCompleted = markSellerCompleted,
                closed = shouldFinalize.takeIf { it },
                closedAt = nowMillis().takeIf { shouldFinalize },
                offerState = "SOLD".takeIf { shouldFinalize },
            ),
        )
    }

    override suspend fun hasRatedChat(context: AuthContext, chatId: String): Boolean {
        val response = apiCallHandler.apiRequest<RatingExistsResponseDto>(
            path = "/v1/users/me/ratings/given/${chatId}",
            idToken = context.idToken,
        )
        return response.exists
    }

    override suspend fun loadUserRatingSummary(context: AuthContext, uid: String): UserRatingSummary {
        val received = loadUserReceivedRatings(uid = uid, idToken = context.idToken)
        val scores = received.values.mapNotNull { it.score }
        if (scores.isEmpty()) {
            return UserRatingSummary(average = 0.0, count = 0)
        }
        return UserRatingSummary(average = scores.average(), count = scores.size)
    }

    override suspend fun loadUserReviews(context: AuthContext, uid: String): List<UserReview> {
        val received = loadUserReceivedRatings(uid = uid, idToken = context.idToken)
        return received.toUserReviews()
    }

    override suspend fun loadUserSellOffers(context: AuthContext, uid: String): List<UserSellOffer> {
        println("TradeBE: calling /v1/users/${uid}/sell-offers")
        val entries = apiCallHandler.apiRequest<Map<String, UserSellOfferDto>>(
            path = "/v1/users/${uid}/sell-offers",
            idToken = context.idToken,
        )
        return entries.toUserSellOffers()
    }

    override suspend fun submitRating(context: AuthContext, request: SubmitChatRatingRequest) {
        if (context.uid == request.ratedUid) throw IllegalStateException("Cannot rate yourself")
        val clampedScore = request.score.coerceIn(1, 5)
        val chatId = request.chatId
        val chatMeta = rawChatMeta(chatId = chatId, idToken = context.idToken)
            ?: throw IllegalStateException("Chat not found")
        val dealStatus = chatMeta.dealStatus.orEmpty()
        if (!dealStatus.equals("COMPLETED", ignoreCase = true)) {
            throw IllegalStateException("Trade must be completed before rating")
        }
        val tradeKey = chatMeta.buildTradeRatingKey(chatId = chatId)
        val hasRated = hasRatedChat(context = context, chatId = tradeKey)
        if (hasRated) throw IllegalStateException("You already rated this trade")

        apiCallHandler.apiRequest<Unit>(
            path = "/v1/chats/${chatId}/ratings",
            method = HttpMethod.Post,
            idToken = context.idToken,
            body = SubmitRatingRequestDto(
                ratedUid = request.ratedUid,
                score = clampedScore,
                comment = request.comment.trim().take(300),
            ),
        )
    }

    private suspend fun rawChatMeta(chatId: String, idToken: String): ChatMetaDto? {
        println("TradeBE: calling /v1/chats/$chatId")
        val response = apiCallHandler.backendRequest(
            path = "/v1/chats/$chatId",
            idToken = idToken,
        )
        if (response.status.value == 404) return null
        apiCallHandler.requireSuccess(response, "GET /v1/chats/$chatId")
        return response.body<ChatMetaDto>()
    }

    private suspend fun loadUserReceivedRatings(uid: String, idToken: String): Map<String, UserReviewDto> {
        return apiCallHandler.apiRequest(
            path = "/v1/users/$uid/ratings",
            idToken = idToken,
        )
    }
}

private fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
