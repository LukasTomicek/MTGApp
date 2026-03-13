package mtg.app.feature.chat.presentation.chatlist

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.chat.domain.ChatService
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.chat.domain.obj.DeleteChatThreadRequest
import kotlinx.coroutines.flow.collect

class ChatListViewModel(
    private val authService: AuthDomainService,
    private val chatService: ChatService,
) : BaseViewModel<ChatListScreenState, ChatListUiEvent, ChatListDirection>(
    initialState = ChatListScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            authService.currentUser.collect { user ->
                currentUid = user?.uid
                currentIdToken = user?.idToken
                if (user == null) {
                    updateState { it.copy(items = emptyList(), infoMessage = "Sign in to open chats") }
                } else {
                    load()
                }
            }
        }
    }

    override fun onUiEvent(event: ChatListUiEvent) {
        when (event) {
            ChatListUiEvent.ScreenOpened -> load()
            ChatListUiEvent.ScreenClosed -> Unit
            ChatListUiEvent.ReloadClicked -> load()
            is ChatListUiEvent.ThreadClicked -> navigate(ChatListDirection.NavigateToChat(event.chatId))
            is ChatListUiEvent.DeleteThreadClicked -> deleteThread(event.chatId, event.counterpartUid)
        }
    }

    private fun load() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        domainCall(
            action = { chatService.loadThreads(context = AuthContext(uid = uid, idToken = idToken)) },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to load chats")
            },
        ) { threads ->
                updateState {
                    it.copy(
                        items = threads,
                        infoMessage = if (threads.isEmpty()) "No chats yet" else "",
                    )
                }
        }
    }

    private fun deleteThread(chatId: String, counterpartUid: String) {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        domainCall(
            action = {
                chatService.deleteThread(
                    context = AuthContext(uid = uid, idToken = idToken),
                    request = DeleteChatThreadRequest(chatId = chatId, counterpartUid = counterpartUid),
                )
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to delete chat")
            },
        ) {
                updateState { state ->
                    val updatedItems = state.items.filterNot { it.chatId == chatId }
                    state.copy(
                        items = updatedItems,
                        infoMessage = if (updatedItems.isEmpty()) "No chats yet" else state.infoMessage,
                    )
                }
        }
    }
}
