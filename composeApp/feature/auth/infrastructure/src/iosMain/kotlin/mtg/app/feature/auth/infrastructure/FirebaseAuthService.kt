package mtg.app.feature.auth.infrastructure

import io.ktor.client.call.body
import mtg.app.feature.auth.data.toAuthUser
import mtg.app.feature.auth.data.toFirebaseErrorMessage
import mtg.app.feature.auth.data.FirebaseAuthResponseDto
import mtg.app.feature.auth.data.FirebaseDeleteAccountRequestDto
import mtg.app.feature.auth.data.FirebaseEmailPasswordRequestDto
import mtg.app.feature.auth.data.FirebaseErrorResponseDto
import mtg.app.feature.auth.data.FirebasePasswordResetRequestDto
import mtg.app.feature.auth.data.FirebaseUpdatePasswordRequestDto
import mtg.app.feature.auth.domain.AuthUser
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

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
        post<Unit>(
            endpoint = "accounts:sendOobCode",
            payload = FirebasePasswordResetRequestDto(
                requestType = "PASSWORD_RESET",
                email = email.trim(),
            ),
        )
    }

    override suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        idToken: String,
    ) {
        authenticate(
            endpoint = "accounts:signInWithPassword",
            email = email,
            password = currentPassword,
        )
        post<Unit>(
            endpoint = "accounts:update",
            payload = FirebaseUpdatePasswordRequestDto(
                idToken = idToken,
                password = newPassword,
            ),
        )
    }

    override suspend fun deleteAccount(idToken: String) {
        post<Unit>(
            endpoint = "accounts:delete",
            payload = FirebaseDeleteAccountRequestDto(idToken = idToken),
        )
    }

    override suspend fun signOut() = Unit

    private suspend fun authenticate(endpoint: String, email: String, password: String): AuthUser {
        val response = post<FirebaseAuthResponseDto>(
            endpoint = endpoint,
            payload = FirebaseEmailPasswordRequestDto(
                email = email.trim(),
                password = password,
            ),
        )

        return response.toAuthUser()
    }

    private suspend inline fun <reified ResponseDto> post(endpoint: String, payload: Any): ResponseDto {
        val key = firebaseWebApiKey.trim()
        if (key.isBlank()) {
            error("Firebase API key is missing. Set FIREBASE_WEB_API_KEY in auth DI module.")
        }

        val response = httpClient.post("https://identitytoolkit.googleapis.com/v1/$endpoint?key=$key") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<FirebaseErrorResponseDto>() }.getOrNull()
            error(errorBody?.toFirebaseErrorMessage() ?: "Authentication failed")
        }

        return if (ResponseDto::class == Unit::class) {
            Unit as ResponseDto
        } else {
            runCatching { response.body<ResponseDto>() }.getOrElse {
                error("Authentication failed")
            }
        }
    }
}
