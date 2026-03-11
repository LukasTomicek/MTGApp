package mtg.app.feature.auth.domain

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    val isInitialized: StateFlow<Boolean>

    suspend fun signIn(email: String, password: String): AuthUser
    suspend fun signUp(email: String, password: String): AuthUser
    suspend fun signInWithGoogleIdToken(idToken: String): AuthUser
    suspend fun sendPasswordReset(email: String)
    suspend fun changePassword(newPassword: String)
    suspend fun signOut()
    suspend fun deleteAccount()
}
