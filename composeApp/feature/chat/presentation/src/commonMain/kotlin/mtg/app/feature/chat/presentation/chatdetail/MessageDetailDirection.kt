package mtg.app.feature.chat.presentation.chatdetail

import mtg.app.core.presentation.Direction

sealed interface MessageDetailDirection : Direction {
    data object None : MessageDetailDirection
    data object CloseChat : MessageDetailDirection
    data class OpenExternalUrl(val url: String) : MessageDetailDirection
}
