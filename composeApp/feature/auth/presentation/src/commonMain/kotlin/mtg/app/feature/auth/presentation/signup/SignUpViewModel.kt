package mtg.app.feature.auth.presentation.signup

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import kotlinx.coroutines.flow.collectLatest

class SignUpViewModel(
    private val authService: AuthDomainService,
) : BaseViewModel<SignUpScreenState, SignUpUiEvent, SignUpDirection>(
    initialState = SignUpScreenState(),
) {
    init {
        launch {
            authService.currentUser.collectLatest { user ->
                if (user != null) {
                    navigate(SignUpDirection.NavigateToHome)
                }
            }
        }
    }

    override fun onUiEvent(event: SignUpUiEvent) {
        when (event) {
            is SignUpUiEvent.EmailChanged -> updateState { it.copy(email = event.value) }
            is SignUpUiEvent.PasswordChanged -> updateState { it.copy(password = event.value) }
            SignUpUiEvent.CreateAccountClicked -> submit()
            SignUpUiEvent.NavigateToSignInClicked -> navigate(SignUpDirection.NavigateToSignIn)
        }
    }

    private fun submit() {
        launch {
            setLoading(true)
            setError(null)

            try {
                val email = state.value.data.email.trim()
                val password = state.value.data.password
                if (email.isBlank() || password.isBlank()) {
                    updateState { it.copy(infoMessage = "Email and password are required") }
                    return@launch
                }

                authService.signUp(email = email, password = password)
                updateState { it.copy(infoMessage = "Account created") }
            } catch (e: Throwable) {
                setError(e.message ?: "Sign up failed")
            } finally {
                setLoading(false)
            }
        }
    }
}
