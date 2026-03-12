package mtg.app.feature.auth.infrastructure

import mtg.app.feature.auth.domain.AuthUser

interface AuthService {
    suspend fun restoreCurrentUser(): AuthUser?
    suspend fun signIn(email: String, password: String): AuthUser
    suspend fun signUp(email: String, password: String): AuthUser
    suspend fun signInWithGoogleIdToken(idToken: String): AuthUser
    suspend fun sendPasswordReset(email: String)
    suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        idToken: String,
    )
    suspend fun deleteAccount(idToken: String)
    suspend fun signOut()
}
