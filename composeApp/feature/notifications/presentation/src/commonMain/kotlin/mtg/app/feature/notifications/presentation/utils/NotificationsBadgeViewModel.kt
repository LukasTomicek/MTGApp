package mtg.app.feature.notifications.presentation

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.notifications.domain.HasUnreadNotificationsUseCase

class NotificationsBadgeViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val hasUnreadNotifications: HasUnreadNotificationsUseCase,
) : BaseViewModel<NotificationsBadgeScreenState, NotificationsBadgeUiEvent, NotificationsDirection>(
    initialState = NotificationsBadgeScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            observeAuthState().collect { user ->
                currentUid = user?.uid
                currentIdToken = user?.idToken
                if (user == null) {
                    updateState { it.copy(hasUnread = false) }
                } else {
                    refresh()
                }
            }
        }
    }

    override fun onUiEvent(event: NotificationsBadgeUiEvent) {
        when (event) {
            NotificationsBadgeUiEvent.RefreshRequested -> refresh()
        }
    }

    private fun refresh() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        launch {
            runCatching {
                hasUnreadNotifications(uid = uid, idToken = idToken)
            }.onSuccess { hasUnread ->
                updateState { it.copy(hasUnread = hasUnread) }
            }
        }
    }
}
