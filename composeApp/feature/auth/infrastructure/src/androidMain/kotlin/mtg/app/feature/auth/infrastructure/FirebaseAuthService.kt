package mtg.app.feature.auth.infrastructure

import android.util.Log
import mtg.app.feature.auth.domain.AuthUser
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import io.ktor.client.HttpClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class FirebaseAuthService actual constructor(
    private val httpClient: HttpClient,
    private val firebaseWebApiKey: String,
) : AuthService {
    private companion object {
        const val TAG = "TradeAuth"
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun restoreCurrentUser(): AuthUser? {
        val currentUser = firebaseAuth.currentUser ?: return null
        val idToken = currentUser.getIdToken(true).awaitTask().token ?: return null
        return AuthUser(
            uid = currentUser.uid,
            email = currentUser.email ?: return null,
            idToken = idToken,
        )
    }

    override suspend fun signIn(email: String, password: String): AuthUser {
        return runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).awaitTask()
            result.toAuthUser()
        }.getOrElse { throwable ->
            logFirebaseFailure(operation = "signIn", throwable = throwable, email = email)
            error(mapFirebaseError(throwable))
        }
    }

    override suspend fun signUp(email: String, password: String): AuthUser {
        return runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).awaitTask()
            result.toAuthUser()
        }.getOrElse { throwable ->
            logFirebaseFailure(operation = "signUp", throwable = throwable, email = email)
            error(mapFirebaseError(throwable))
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthUser {
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).awaitTask()
            result.toAuthUser()
        }.getOrElse { throwable ->
            logFirebaseFailure(operation = "signInWithGoogleIdToken", throwable = throwable)
            error(mapFirebaseError(throwable))
        }
    }

    override suspend fun sendPasswordReset(email: String) {
        runCatching {
            firebaseAuth.sendPasswordResetEmail(email.trim()).awaitTask()
        }.getOrElse { throwable ->
            logFirebaseFailure(operation = "sendPasswordReset", throwable = throwable, email = email)
            error(mapFirebaseError(throwable))
        }
    }

    override suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        idToken: String,
    ) {
        val currentUser = firebaseAuth.currentUser ?: error("No signed in user")
        runCatching {
            val credential = EmailAuthProvider.getCredential(email.trim(), currentPassword)
            currentUser.reauthenticate(credential).awaitTask()
            currentUser.updatePassword(newPassword).awaitTask()
        }.getOrElse { throwable ->
            logFirebaseFailure(operation = "changePassword", throwable = throwable)
            error(mapFirebaseError(throwable))
        }
    }

    override suspend fun deleteAccount(idToken: String) {
        val currentUser = firebaseAuth.currentUser ?: error("No signed in user")
        runCatching {
            currentUser.delete().awaitTask()
        }.getOrElse { throwable ->
            logFirebaseFailure(operation = "deleteAccount", throwable = throwable)
            error(mapFirebaseError(throwable))
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun AuthResult.toAuthUser(): AuthUser {
        val currentUser = user ?: error("Missing Firebase user")
        val idToken = currentUser.getIdToken(true).awaitTask().token ?: error("Missing id token")
        return AuthUser(
            uid = currentUser.uid,
            email = currentUser.email ?: error("Missing email"),
            idToken = idToken,
        )
    }

    private fun mapFirebaseError(throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthUserCollisionException -> "Email is already used"
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
            is FirebaseAuthInvalidUserException -> "User not found"
            is FirebaseAuthWeakPasswordException -> "Password must have at least 6 characters"
            is FirebaseAuthRecentLoginRequiredException -> "For delete account, sign in again first"
            is FirebaseTooManyRequestsException -> "Too many attempts, try again later"
            is FirebaseNetworkException -> "Network error"
            else -> throwable.message ?: "Authentication failed"
        }
    }

    private fun logFirebaseFailure(
        operation: String,
        throwable: Throwable,
        email: String? = null,
    ) {
        val emailLabel = email?.trim().orEmpty()
        val detail = buildString {
            append("operation=").append(operation)
            if (emailLabel.isNotBlank()) {
                append(", email=").append(emailLabel)
            }
            append(", type=").append(throwable::class.qualifiedName)
            append(", message=").append(throwable.message.orEmpty())
            if (throwable is FirebaseAuthInvalidCredentialsException) {
                append(", errorCode=").append(throwable.errorCode)
            }
            if (throwable is FirebaseAuthInvalidUserException) {
                append(", errorCode=").append(throwable.errorCode)
            }
        }
        Log.e(TAG, detail, throwable)
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: IllegalStateException("Task failed"))
        }
    }
}
