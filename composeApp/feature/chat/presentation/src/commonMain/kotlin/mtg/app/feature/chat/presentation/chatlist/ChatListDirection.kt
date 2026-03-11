package mtg.app.feature.chat.presentation.chatlist

import mtg.app.core.presentation.Direction

sealed interface ChatListDirection : Direction {
    data object None : ChatListDirection
    data class NavigateToChat(val chatId: String) : ChatListDirection
}
