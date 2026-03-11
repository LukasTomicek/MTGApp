package mtg.app.feature.chat.presentation.chatdetail

import mtg.app.core.presentation.Event

sealed interface MessageDetailUiEvent : Event {
    data class ScreenOpened(val chatId: String) : MessageDetailUiEvent
    data object ScreenClosed : MessageDetailUiEvent
    data class InputChanged(val value: String) : MessageDetailUiEvent
    data object SendClicked : MessageDetailUiEvent
    data object ReloadClicked : MessageDetailUiEvent
    data object ProposeDealClicked : MessageDetailUiEvent
    data object ConfirmDealClicked : MessageDetailUiEvent
    data object RatingDismissed : MessageDetailUiEvent
    data class RatingScoreChanged(val value: Int) : MessageDetailUiEvent
    data class RatingCommentChanged(val value: String) : MessageDetailUiEvent
    data object SubmitRatingClicked : MessageDetailUiEvent
}
