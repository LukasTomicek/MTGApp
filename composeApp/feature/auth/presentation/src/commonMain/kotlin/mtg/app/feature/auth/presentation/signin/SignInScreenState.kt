package mtg.app.feature.auth.presentation.signin

data class SignInScreenState(
    // Form input
    val email: String = "",
    val password: String = "",

    // Validation/server message
    val infoMessage: String = "",
)
