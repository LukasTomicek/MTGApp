package mtg.app.feature.notifications.presentation

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.notifications.domain.NotificationType
import mtg.app.feature.notifications.domain.NotificationsService
import mtg.app.core.domain.obj.AuthContext
import kotlinx.coroutines.flow.collect

class NotificationsViewModel(
    private val authService: AuthDomainService,
    private val notificationsService: NotificationsService,
) : BaseViewModel<NotificationsScreenState, NotificationsUiEvent, NotificationsDirection>(
    initialState = NotificationsScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            authService.currentUser.collect { user ->
                currentUid = user?.uid
                currentIdToken = user?.idToken
                if (user == null) {
                    updateState { it.copy(items = emptyList()) }
                } else {
                    load()
                }
            }
        }
    }

    override fun onUiEvent(event: NotificationsUiEvent) {
        when (event) {
            NotificationsUiEvent.ReloadClicked -> load()
            NotificationsUiEvent.ScreenOpened -> load()
            is NotificationsUiEvent.NotificationClicked -> {
                markRead(event.notificationId)
                if (event.type == NotificationType.CARD_MATCH) {
                    val sameCardOffers = state.value.data.items.filter {
                        if (it.type != NotificationType.CARD_MATCH) {
                            false
                        } else if (event.cardId.isNotBlank()) {
                            it.cardId == event.cardId
                        } else {
                            it.cardName.equals(event.cardName, ignoreCase = true)
                        }
                    }
                    if (sameCardOffers.size > 1) {
                        updateState {
                            it.copy(
                                isOffersDialogVisible = true,
                                selectedOfferCardName = event.cardName,
                                offersForSelectedCard = sameCardOffers,
                                selectedOfferNotificationId = sameCardOffers.firstOrNull()?.id,
                            )
                        }
                        return
                    }
                }

                event.chatId?.takeIf { it.isNotBlank() }?.let { chatId ->
                    navigate(NotificationsDirection.NavigateToChat(chatId))
                }
            }
            is NotificationsUiEvent.OfferSelected -> {
                updateState { it.copy(selectedOfferNotificationId = event.notificationId) }
                markRead(event.notificationId)
            }
            NotificationsUiEvent.OfferDialogDismissed -> {
                updateState {
                    it.copy(
                        isOffersDialogVisible = false,
                        selectedOfferCardName = "",
                        offersForSelectedCard = emptyList(),
                        selectedOfferNotificationId = null,
                    )
                }
            }
            NotificationsUiEvent.OfferViewProfileClicked -> {
                val selectedId = state.value.data.selectedOfferNotificationId ?: return
                val selected = state.value.data.offersForSelectedCard.firstOrNull { it.id == selectedId } ?: return
                if (selected.sellerUid.isNotBlank()) {
                    updateState {
                        it.copy(
                            isOffersDialogVisible = false,
                            selectedOfferCardName = "",
                            offersForSelectedCard = emptyList(),
                            selectedOfferNotificationId = null,
                        )
                    }
                    navigate(NotificationsDirection.NavigateToPublicProfile(selected.sellerUid))
                }
            }
            NotificationsUiEvent.OfferMessageClicked -> {
                val selectedId = state.value.data.selectedOfferNotificationId ?: return
                val selected = state.value.data.offersForSelectedCard.firstOrNull { it.id == selectedId } ?: return
                markRead(selected.id)
                selected.chatId?.takeIf { it.isNotBlank() }?.let { chatId ->
                    updateState {
                        it.copy(
                            isOffersDialogVisible = false,
                            selectedOfferCardName = "",
                            offersForSelectedCard = emptyList(),
                            selectedOfferNotificationId = null,
                        )
                    }
                    navigate(NotificationsDirection.NavigateToChat(chatId))
                }
            }
            is NotificationsUiEvent.DeleteNotificationClicked -> delete(event.notificationId)
        }
    }

    private fun load() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        domainCall(
            action = {
                notificationsService.loadNotifications(
                    context = AuthContext(uid = uid, idToken = idToken),
                )
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to load notifications")
            },
        ) { items ->
                updateState { it.copy(items = items) }
        }
    }

    private fun markRead(notificationId: String) {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { throwable ->
                setError(throwable.message ?: "Failed to mark notification as read")
            },
            action = {
                notificationsService.markNotificationRead(
                    context = AuthContext(uid = uid, idToken = idToken),
                    notificationId = notificationId,
                )
            },
        ) {
                updateState { state ->
                    state.copy(
                        items = state.items.map { item ->
                            if (item.id == notificationId) item.copy(isRead = true) else item
                        }
                    )
                }
        }
    }

    private fun delete(notificationId: String) {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { throwable ->
                setError(throwable.message ?: "Failed to delete notification")
            },
            action = {
                notificationsService.deleteNotification(
                    context = AuthContext(uid = uid, idToken = idToken),
                    notificationId = notificationId,
                )
            },
        ) {
                updateState { state ->
                    state.copy(items = state.items.filterNot { it.id == notificationId })
                }
        }
    }
}
