package mtg.app.feature.auth.presentation.forgotpassword

import mtg.app.core.presentation.Event

sealed interface ForgotPasswordUiEvent : Event {
    data class EmailChanged(val value: String) : ForgotPasswordUiEvent
    data object ResetClicked : ForgotPasswordUiEvent
    data object BackToSignInClicked : ForgotPasswordUiEvent
}
