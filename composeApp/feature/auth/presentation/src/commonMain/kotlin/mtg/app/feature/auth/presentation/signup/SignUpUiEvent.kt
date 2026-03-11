package mtg.app.feature.auth.presentation.signup

import mtg.app.core.presentation.Event

sealed interface SignUpUiEvent : Event {
    data class EmailChanged(val value: String) : SignUpUiEvent
    data class PasswordChanged(val value: String) : SignUpUiEvent
    data object CreateAccountClicked : SignUpUiEvent
    data object NavigateToSignInClicked : SignUpUiEvent
}
