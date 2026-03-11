package mtg.app.feature.chat.presentation.publicprofile

import mtg.app.core.presentation.Event

sealed interface PublicProfileUiEvent : Event {
    data class ScreenOpened(val uid: String) : PublicProfileUiEvent
    data object RetryClicked : PublicProfileUiEvent
}
