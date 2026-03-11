package mtg.app.feature.chat.data

import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.DealStatus
import mtg.app.feature.chat.domain.MessageThread
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

fun JsonObject.toUserMatchThreads(): List<MessageThread> {
    return entries.mapNotNull { (chatId, value) ->
        val obj = value as? JsonObject ?: return@mapNotNull null
        val counterpartUid = obj.getString("counterpartUid").orEmpty()
        val counterpartEmail = obj.getString("counterpartEmail").orEmpty()
        val counterpartNickname = obj.getString("counterpartNickname").orEmpty()
        val cardId = obj.getString("cardId").orEmpty()
        val cardName = obj.getString("cardName").orEmpty()
        val role = obj.getString("role").orEmpty()
        val updatedAt = obj.getLong("updatedAt") ?: 0L

        MessageThread(
            chatId = obj.getString("chatId") ?: chatId,
            counterpartUid = counterpartUid,
            counterpartEmail = counterpartEmail,
            counterpartNickname = counterpartNickname,
            cardId = cardId,
            cardName = cardName,
            role = role,
            lastMessage = null,
            lastMessageAt = updatedAt,
        )
    }
}

fun JsonObject.toChatMeta(chatId: String): ChatMeta? {
    return ChatMeta(
        chatId = getString("chatId") ?: chatId,
        buyerUid = getString("buyerUid").orEmpty(),
        buyerEmail = getString("buyerEmail").orEmpty(),
        sellerUid = getString("sellerUid").orEmpty(),
        sellerEmail = getString("sellerEmail").orEmpty(),
        cardId = getString("cardId").orEmpty(),
        cardName = getString("cardName").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
        lastMessage = getString("lastMessage"),
        lastMessageAt = getLong("lastMessageAt"),
        dealStatus = getString("dealStatus").toDealStatus(),
        buyerConfirmed = getBoolean("buyerConfirmed") ?: false,
        sellerConfirmed = getBoolean("sellerConfirmed") ?: false,
        buyerCompleted = getBoolean("buyerCompleted") ?: false,
        sellerCompleted = getBoolean("sellerCompleted") ?: false,
    )
}

fun JsonObject.toChatMessages(): List<ChatMessage> {
    return entries.mapNotNull { (messageId, value) ->
        val obj = value as? JsonObject ?: return@mapNotNull null
        val text = obj.getString("text") ?: return@mapNotNull null
        ChatMessage(
            id = obj.getString("messageId") ?: messageId,
            senderUid = obj.getString("senderUid").orEmpty(),
            senderEmail = obj.getString("senderEmail").orEmpty(),
            text = text,
            createdAt = obj.getLong("createdAt") ?: 0L,
        )
    }.sortedBy { it.createdAt }
}

private fun JsonObject.getString(key: String): String? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return runCatching { primitive.content }.getOrNull()
}

private fun JsonObject.getLong(key: String): Long? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    val raw = runCatching { primitive.content }.getOrNull() ?: return null
    return raw.toLongOrNull()
}

private fun JsonObject.getBoolean(key: String): Boolean? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return primitive.booleanOrNull
}

private fun String?.toDealStatus(): DealStatus {
    return when (this?.trim()?.uppercase()) {
        "PROPOSED" -> DealStatus.PROPOSED
        "COMPLETED" -> DealStatus.COMPLETED
        "CANCELED" -> DealStatus.CANCELED
        else -> DealStatus.OPEN
    }
}
