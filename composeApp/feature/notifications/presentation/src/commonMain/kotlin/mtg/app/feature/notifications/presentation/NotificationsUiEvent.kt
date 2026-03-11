package mtg.app.feature.notifications.presentation

import mtg.app.core.presentation.Event
import mtg.app.feature.notifications.domain.NotificationType

sealed interface NotificationsUiEvent : Event {
    data object ReloadClicked : NotificationsUiEvent
    data object ScreenOpened : NotificationsUiEvent
    data class NotificationClicked(
        val notificationId: String,
        val chatId: String?,
        val cardId: String,
        val cardName: String,
        val type: NotificationType,
    ) : NotificationsUiEvent
    data class OfferSelected(val notificationId: String) : NotificationsUiEvent
    data object OfferDialogDismissed : NotificationsUiEvent
    data object OfferViewProfileClicked : NotificationsUiEvent
    data object OfferMessageClicked : NotificationsUiEvent
    data class DeleteNotificationClicked(val notificationId: String) : NotificationsUiEvent
}
