package mtg.app.feature.notifications.presentation

import mtg.app.feature.notifications.domain.NotificationItem

data class NotificationsScreenState(
    // Notification feed
    val items: List<NotificationItem> = emptyList(),

    // Multi-offer dialog for same card
    val isOffersDialogVisible: Boolean = false,
    val selectedOfferCardName: String = "",
    val offersForSelectedCard: List<NotificationItem> = emptyList(),
    val selectedOfferNotificationId: String? = null,
)
