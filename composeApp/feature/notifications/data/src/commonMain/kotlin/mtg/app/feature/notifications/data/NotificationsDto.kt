package mtg.app.feature.notifications.data

import mtg.app.feature.notifications.domain.NotificationItem
import mtg.app.feature.notifications.domain.NotificationType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

fun JsonObject.toNotificationItems(): List<NotificationItem> {
    return entries.mapNotNull { (key, value) ->
        val obj = value as? JsonObject ?: return@mapNotNull null
        val sellerEmail = obj.getString("sellerEmail") ?: return@mapNotNull null
        val sellerUid = obj.getString("sellerUid") ?: ""
        val cardId = obj.getString("cardId") ?: ""
        val cardName = obj.getString("cardName") ?: return@mapNotNull null
        val notificationId = obj.getString("notificationId") ?: key
        val message = obj.getString("message")
        val chatId = obj.getString("chatId")
        NotificationItem(
            id = notificationId,
            chatId = chatId,
            cardId = cardId,
            sellerUid = sellerUid,
            sellerEmail = sellerEmail,
            sellerImageUrl = obj.getString("sellerImageUrl"),
            cardName = cardName,
            price = obj.getDouble("price"),
            message = message,
            isRead = obj.getBoolean("isRead") ?: false,
            type = obj.getString("type")
                .toNotificationTypeOrNull()
                ?: inferNotificationType(
                    notificationId = notificationId,
                    chatId = chatId,
                    message = message,
                ),
        )
    }
}

private fun inferNotificationType(
    notificationId: String,
    chatId: String?,
    message: String?,
): NotificationType {
    if (notificationId.startsWith("chat_msg_", ignoreCase = true)) return NotificationType.NEW_MESSAGE
    if (!chatId.isNullOrBlank() && message?.startsWith("New message", ignoreCase = true) == true) {
        return NotificationType.NEW_MESSAGE
    }
    return NotificationType.CARD_MATCH
}

private fun String?.toNotificationTypeOrNull(): NotificationType? {
    return when (this?.trim()?.lowercase()) {
        "card_match" -> NotificationType.CARD_MATCH
        "new_message", "chat_message" -> NotificationType.NEW_MESSAGE
        else -> null
    }
}

private fun JsonObject.getString(key: String): String? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return runCatching { primitive.content }.getOrNull()
}

private fun JsonObject.getBoolean(key: String): Boolean? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return primitive.booleanOrNull
}

private fun JsonObject.getDouble(key: String): Double? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    val raw = runCatching { primitive.content }.getOrNull() ?: return null
    return raw.toDoubleOrNull()
}
