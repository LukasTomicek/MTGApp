package mtg.app.feature.notifications.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mtg.app.feature.notifications.domain.NotificationItem
import mtg.app.feature.notifications.domain.NotificationType

@Serializable
data class NotificationEntryDto(
    @SerialName("notificationId")
    val notificationId: String? = null,
    @SerialName("chatId")
    val chatId: String? = null,
    @SerialName("sellerUid")
    val sellerUid: String = "",
    @SerialName("sellerEmail")
    val sellerEmail: String? = null,
    @SerialName("sellerImageUrl")
    val sellerImageUrl: String? = null,
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String? = null,
    @SerialName("price")
    val price: Double? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("isRead")
    val isRead: Boolean = false,
    @SerialName("type")
    val type: String? = null,
)

@Serializable
data class HasUnreadNotificationsResponseDto(
    @SerialName("hasUnread")
    val hasUnread: Boolean = false,
)

internal fun Map<String, NotificationEntryDto>.toNotificationItems(): List<NotificationItem> {
    return entries.mapNotNull { (fallbackId, dto) ->
        val sellerEmail = dto.sellerEmail?.trim().takeUnless { it.isNullOrBlank() } ?: return@mapNotNull null
        val cardName = dto.cardName?.trim().takeUnless { it.isNullOrBlank() } ?: return@mapNotNull null
        val notificationId = dto.notificationId?.trim().takeUnless { it.isNullOrBlank() } ?: fallbackId
        val message = dto.message?.trim()?.takeUnless { it.isBlank() }
        val chatId = dto.chatId?.trim()?.takeUnless { it.isBlank() }
        NotificationItem(
            id = notificationId,
            chatId = chatId,
            cardId = dto.cardId.trim(),
            sellerUid = dto.sellerUid.trim(),
            sellerEmail = sellerEmail,
            sellerImageUrl = dto.sellerImageUrl?.trim()?.takeUnless { it.isBlank() },
            cardName = cardName,
            price = dto.price,
            message = message,
            isRead = dto.isRead,
            type = dto.type.toNotificationTypeOrNull()
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
