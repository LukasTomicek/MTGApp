package mtg.app.feature.auth.data

import mtg.app.feature.auth.domain.AuthUser
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun JsonObject.toAuthUser(): AuthUser {
    val uid = getString("localId") ?: error("Missing user id")
    val email = getString("email") ?: error("Missing email")
    val token = getString("idToken") ?: error("Missing id token")

    return AuthUser(
        uid = uid,
        email = email,
        idToken = token,
    )
}

fun JsonObject.toFirebaseErrorMessage(): String {
    val errorObject = this["error"] as? JsonObject ?: return "Authentication failed"
    val code = errorObject.getString("message") ?: return "Authentication failed"

    return when (code) {
        "EMAIL_EXISTS" -> "Email is already used"
        "INVALID_PASSWORD" -> "Invalid password"
        "EMAIL_NOT_FOUND" -> "User not found"
        "INVALID_EMAIL" -> "Invalid email"
        "WEAK_PASSWORD : Password should be at least 6 characters" -> "Password must have at least 6 characters"
        else -> code.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun JsonObject.getString(key: String): String? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return runCatching { primitive.content }.getOrNull()
}
