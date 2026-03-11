package mtg.app.feature.notifications.domain

enum class NotificationType {
    CARD_MATCH,
    NEW_MESSAGE,
}

data class NotificationItem(
    val id: String,
    val chatId: String?,
    val cardId: String,
    val sellerUid: String,
    val sellerEmail: String,
    val sellerImageUrl: String?,
    val cardName: String,
    val price: Double?,
    val message: String?,
    val isRead: Boolean,
    val type: NotificationType,
)
