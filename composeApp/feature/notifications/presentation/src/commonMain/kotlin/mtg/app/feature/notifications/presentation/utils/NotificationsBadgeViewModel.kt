package mtg.app.feature.notifications.presentation

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.notifications.domain.NotificationsService
import mtg.app.core.domain.obj.AuthContext

class NotificationsBadgeViewModel(
    private val authService: AuthDomainService,
    private val notificationsService: NotificationsService,
) : BaseViewModel<NotificationsBadgeScreenState, NotificationsBadgeUiEvent, NotificationsDirection>(
    initialState = NotificationsBadgeScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            authService.currentUser.collect { user ->
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
                notificationsService.hasUnreadNotifications(
                    context = AuthContext(uid = uid, idToken = idToken),
                )
            }.onSuccess { hasUnread ->
                updateState { it.copy(hasUnread = hasUnread) }
            }
        }
    }
}
