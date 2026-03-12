package mtg.app.feature.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mtg.app.feature.auth.domain.AuthUser

@Serializable
data class FirebaseEmailPasswordRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("returnSecureToken")
    val returnSecureToken: Boolean = true,
)

@Serializable
data class FirebasePasswordResetRequestDto(
    @SerialName("requestType")
    val requestType: String,
    @SerialName("email")
    val email: String,
)

@Serializable
data class FirebaseUpdatePasswordRequestDto(
    @SerialName("idToken")
    val idToken: String,
    @SerialName("password")
    val password: String,
    @SerialName("returnSecureToken")
    val returnSecureToken: Boolean = true,
)

@Serializable
data class FirebaseDeleteAccountRequestDto(
    @SerialName("idToken")
    val idToken: String,
)

@Serializable
data class FirebaseAuthResponseDto(
    @SerialName("localId")
    val localId: String,
    @SerialName("email")
    val email: String,
    @SerialName("idToken")
    val idToken: String,
)

@Serializable
data class FirebaseErrorResponseDto(
    @SerialName("error")
    val error: FirebaseErrorDto? = null,
)

@Serializable
data class FirebaseErrorDto(
    @SerialName("message")
    val message: String? = null,
)

fun FirebaseAuthResponseDto.toAuthUser(): AuthUser {
    return AuthUser(
        uid = localId,
        email = email,
        idToken = idToken,
    )
}

fun FirebaseErrorResponseDto.toFirebaseErrorMessage(): String {
    val code = error?.message ?: return "Authentication failed"
    return when (code) {
        "EMAIL_EXISTS" -> "Email is already used"
        "INVALID_PASSWORD" -> "Invalid password"
        "EMAIL_NOT_FOUND" -> "User not found"
        "INVALID_EMAIL" -> "Invalid email"
        "WEAK_PASSWORD : Password should be at least 6 characters" -> "Password must have at least 6 characters"
        else -> code.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }
}
