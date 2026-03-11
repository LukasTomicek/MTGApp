package mtg.app.feature.welcome.presentation.welcome

import mtg.app.core.presentation.Event

sealed interface WelcomeUiEvent : Event {
    data object ContinueClicked : WelcomeUiEvent
}
