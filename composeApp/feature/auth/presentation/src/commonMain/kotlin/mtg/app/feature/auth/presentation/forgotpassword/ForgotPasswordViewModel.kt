package mtg.app.feature.auth.presentation.forgotpassword

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService

class ForgotPasswordViewModel(
    private val authService: AuthDomainService,
) : BaseViewModel<ForgotPasswordScreenState, ForgotPasswordUiEvent, ForgotPasswordDirection>(
    initialState = ForgotPasswordScreenState(),
) {

    override fun onUiEvent(event: ForgotPasswordUiEvent) {
        when (event) {
            is ForgotPasswordUiEvent.EmailChanged -> updateState { it.copy(email = event.value) }
            ForgotPasswordUiEvent.ResetClicked -> submitReset()
            ForgotPasswordUiEvent.BackToSignInClicked -> navigate(ForgotPasswordDirection.NavigateToSignIn)
        }
    }

    private fun submitReset() {
        domainCall(
            action = {
                val email = state.value.data.email.trim()
                if (email.isBlank()) {
                    updateState { it.copy(infoMessage = "Email is required") }
                    return@domainCall Unit
                }

                authService.sendPasswordReset(email = email)
            },
            onError = { throwable ->
                setError(throwable.message ?: "Password reset failed")
            },
        ) {
            updateState { it.copy(infoMessage = "Reset link sent to email") }
        }
    }
}
