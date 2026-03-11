package mtg.app.feature.chat.presentation.chatlist

import mtg.app.core.presentation.Event

sealed interface ChatListUiEvent : Event {
    data object ScreenOpened : ChatListUiEvent
    data object ScreenClosed : ChatListUiEvent
    data object ReloadClicked : ChatListUiEvent
    data class ThreadClicked(val chatId: String) : ChatListUiEvent
    data class DeleteThreadClicked(val chatId: String, val counterpartUid: String) : ChatListUiEvent
}
