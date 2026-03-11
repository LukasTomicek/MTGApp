package mtg.app.feature.chat.presentation.chatlist

import mtg.app.feature.chat.domain.MessageThread

data class ChatListScreenState(
    // Chat threads
    val items: List<MessageThread> = emptyList(),

    // Empty/error helper text
    val infoMessage: String = "",
)
