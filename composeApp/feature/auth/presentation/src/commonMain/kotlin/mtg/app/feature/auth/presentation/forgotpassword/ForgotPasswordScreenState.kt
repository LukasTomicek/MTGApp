package mtg.app.feature.auth.presentation.forgotpassword

data class ForgotPasswordScreenState(
    // Form input
    val email: String = "",

    // Validation/server message
    val infoMessage: String = "",
)
