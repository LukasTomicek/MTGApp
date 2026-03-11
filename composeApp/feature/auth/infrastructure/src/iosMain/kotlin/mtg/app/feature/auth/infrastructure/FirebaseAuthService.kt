package mtg.app.feature.auth.infrastructure

import mtg.app.feature.auth.data.toAuthUser
import mtg.app.feature.auth.data.toFirebaseErrorMessage
import mtg.app.feature.auth.domain.AuthUser
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

actual class FirebaseAuthService actual constructor(
    private val httpClient: HttpClient,
    private val firebaseWebApiKey: String,
) : AuthService {
    override suspend fun restoreCurrentUser(): AuthUser? = null

    override suspend fun signIn(email: String, password: String): AuthUser {
        return authenticate(
            endpoint = "accounts:signInWithPassword",
            email = email,
            password = password,
        )
    }

    override suspend fun signUp(email: String, password: String): AuthUser {
        return authenticate(
            endpoint = "accounts:signUp",
            email = email,
            password = password,
        )
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthUser {
        error("Google sign up is available only on Android for now")
    }

    override suspend fun sendPasswordReset(email: String) {
        post(
            endpoint = "accounts:sendOobCode",
            payload = buildJsonObject {
                put("requestType", "PASSWORD_RESET")
                put("email", email.trim())
            },
        )
    }

    override suspend fun changePassword(newPassword: String, idToken: String) {
        post(
            endpoint = "accounts:update",
            payload = buildJsonObject {
                put("idToken", idToken)
                put("password", newPassword)
                put("returnSecureToken", true)
            },
        )
    }

    override suspend fun deleteAccount(idToken: String) {
        post(
            endpoint = "accounts:delete",
            payload = buildJsonObject {
                put("idToken", idToken)
            },
        )
    }

    override suspend fun signOut() = Unit

    private suspend fun authenticate(endpoint: String, email: String, password: String): AuthUser {
        val responseJson = post(
            endpoint = endpoint,
            payload = buildJsonObject {
                put("email", email.trim())
                put("password", password)
                put("returnSecureToken", true)
            },
        )

        return responseJson.toAuthUser()
    }

    private suspend fun post(endpoint: String, payload: JsonObject): JsonObject {
        val key = firebaseWebApiKey.trim()
        if (key.isBlank()) {
            error("Firebase API key is missing. Set FIREBASE_WEB_API_KEY in auth DI module.")
        }

        val response = httpClient.post("https://identitytoolkit.googleapis.com/v1/$endpoint?key=$key") {
            contentType(ContentType.Application.Json)
            setBody(payload.toString())
        }
        val responseBody = response.bodyAsText()
        val responseJson: JsonObject = runCatching {
            Json.parseToJsonElement(responseBody) as JsonObject
        }.getOrElse {
            error("Authentication failed")
        }

        if (!response.status.isSuccess()) {
            error(responseJson.toFirebaseErrorMessage())
        }

        return responseJson
    }
}
