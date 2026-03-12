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
        launch {
            setLoading(true)
            setError(null)

            try {
                val email = state.value.data.email.trim()
                if (email.isBlank()) {
                    updateState { it.copy(infoMessage = "Email is required") }
                    return@launch
                }

                authService.sendPasswordReset(email = email)
                updateState { it.copy(infoMessage = "Reset link sent to email") }
            } catch (e: Throwable) {
                setError(e.message ?: "Password reset failed")
            } finally {
                setLoading(false)
            }
        }
    }
}
