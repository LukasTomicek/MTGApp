package mtg.app.feature.chat.presentation.chatlist

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.chat.domain.DeleteChatThreadUseCase
import mtg.app.feature.chat.domain.LoadMessageThreadsUseCase
import kotlinx.coroutines.flow.collect

class ChatListViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val loadMessageThreads: LoadMessageThreadsUseCase,
    private val deleteChatThread: DeleteChatThreadUseCase,
) : BaseViewModel<ChatListScreenState, ChatListUiEvent, ChatListDirection>(
    initialState = ChatListScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            observeAuthState().collect { user ->
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

        launch {
            setLoading(true)
            setError(null)
            runCatching {
                loadMessageThreads(uid = uid, idToken = idToken)
            }.onSuccess { threads ->
                updateState {
                    it.copy(
                        items = threads,
                        infoMessage = if (threads.isEmpty()) "No chats yet" else "",
                    )
                }
            }.onFailure {
                setError(it.message ?: "Failed to load chats")
            }
            setLoading(false)
        }
    }

    private fun deleteThread(chatId: String, counterpartUid: String) {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        launch {
            setLoading(true)
            setError(null)
            runCatching {
                deleteChatThread(
                    uid = uid,
                    idToken = idToken,
                    chatId = chatId,
                    counterpartUid = counterpartUid,
                )
            }.onSuccess {
                updateState { state ->
                    val updatedItems = state.items.filterNot { it.chatId == chatId }
                    state.copy(
                        items = updatedItems,
                        infoMessage = if (updatedItems.isEmpty()) "No chats yet" else state.infoMessage,
                    )
                }
            }.onFailure {
                setError(it.message ?: "Failed to delete chat")
            }
            setLoading(false)
        }
    }
}
