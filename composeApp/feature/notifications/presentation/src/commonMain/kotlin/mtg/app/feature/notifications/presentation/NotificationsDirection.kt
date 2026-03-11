package mtg.app.feature.notifications.presentation

import mtg.app.core.presentation.Direction

sealed interface NotificationsDirection : Direction {
    data object None : NotificationsDirection
    data class NavigateToChat(val chatId: String) : NotificationsDirection
    data class NavigateToPublicProfile(val uid: String) : NotificationsDirection
}
