package mtg.app.feature.auth.presentation.signin

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import kotlinx.coroutines.flow.collectLatest

class SignInViewModel(
    private val authService: AuthDomainService,
) : BaseViewModel<SignInScreenState, SignInUiEvent, SignInDirection>(
    initialState = SignInScreenState(),
) {
    init {
        launch {
            authService.currentUser.collectLatest { user ->
                if (user != null) {
                    navigate(SignInDirection.NavigateToHome)
                }
            }
        }
    }

    override fun onUiEvent(event: SignInUiEvent) {
        when (event) {
            is SignInUiEvent.EmailChanged -> updateState { it.copy(email = event.value) }
            is SignInUiEvent.PasswordChanged -> updateState { it.copy(password = event.value) }
            SignInUiEvent.SignInClicked -> submit()
            SignInUiEvent.GoogleSignInClicked -> Unit
            is SignInUiEvent.GoogleIdTokenReceived -> submitGoogle(event.idToken)
            is SignInUiEvent.GoogleSignInFailed -> setError(event.message)
            SignInUiEvent.NavigateToSignUpClicked -> navigate(SignInDirection.NavigateToSignUp)
            SignInUiEvent.NavigateToForgotPasswordClicked -> navigate(SignInDirection.NavigateToForgotPassword)
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

                authService.signIn(email = email, password = password)
                updateState { it.copy(infoMessage = "Signed in") }
            } catch (e: Throwable) {
                setError(e.message ?: "Sign in failed")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun submitGoogle(idToken: String) {
        launch {
            setLoading(true)
            setError(null)
            try {
                val normalizedToken = idToken.trim()
                if (normalizedToken.isBlank()) {
                    setError("Missing Google token")
                    return@launch
                }
                authService.signInWithGoogleIdToken(idToken = normalizedToken)
                updateState { it.copy(infoMessage = "Signed in with Google") }
            } catch (e: Throwable) {
                setError(e.message ?: "Google sign in failed")
            } finally {
                setLoading(false)
            }
        }
    }
}
