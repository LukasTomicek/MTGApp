package mtg.app.feature.auth.presentation.signin

import mtg.app.core.presentation.Event

sealed interface SignInUiEvent : Event {
    data class EmailChanged(val value: String) : SignInUiEvent
    data class PasswordChanged(val value: String) : SignInUiEvent
    data object SignInClicked : SignInUiEvent
    data object GoogleSignInClicked : SignInUiEvent
    data class GoogleIdTokenReceived(val idToken: String) : SignInUiEvent
    data class GoogleSignInFailed(val message: String) : SignInUiEvent
    data object NavigateToSignUpClicked : SignInUiEvent
    data object NavigateToForgotPasswordClicked : SignInUiEvent
}
