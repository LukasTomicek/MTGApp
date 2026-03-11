package mtg.app.feature.notifications.presentation

import mtg.app.core.presentation.Event

sealed interface NotificationsBadgeUiEvent : Event {
    data object RefreshRequested : NotificationsBadgeUiEvent
}
