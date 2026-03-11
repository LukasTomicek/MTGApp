package mtg.app.feature.auth.domain

data class AuthUser(
    val uid: String,
    val email: String,
    val idToken: String,
)
