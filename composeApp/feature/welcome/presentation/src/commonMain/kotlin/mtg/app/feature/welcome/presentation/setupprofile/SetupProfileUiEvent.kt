package mtg.app.feature.welcome.presentation.setupprofile

import mtg.app.core.presentation.Event

sealed interface SetupProfileUiEvent : Event {
    data class NicknameChanged(val value: String) : SetupProfileUiEvent
    data object BackClicked : SetupProfileUiEvent
    data object ContinueClicked : SetupProfileUiEvent
}
