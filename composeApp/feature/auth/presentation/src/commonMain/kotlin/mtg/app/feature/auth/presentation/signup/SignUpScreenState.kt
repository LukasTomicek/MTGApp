package mtg.app.feature.auth.presentation.signup

data class SignUpScreenState(
    // Form input
    val email: String = "",
    val password: String = "",

    // Validation/server message
    val infoMessage: String = "",
)
