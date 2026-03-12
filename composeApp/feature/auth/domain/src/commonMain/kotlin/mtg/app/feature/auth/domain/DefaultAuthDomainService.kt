package mtg.app.feature.auth.domain

import kotlinx.coroutines.flow.StateFlow

class DefaultAuthDomainService(
    private val repository: AuthRepository,
) : AuthDomainService {
    override val currentUser: StateFlow<AuthUser?> = repository.currentUser
    override val isInitialized: StateFlow<Boolean> = repository.isInitialized

    override suspend fun signIn(email: String, password: String): AuthUser {
        return repository.signIn(email = email, password = password)
    }

    override suspend fun signUp(email: String, password: String): AuthUser {
        return repository.signUp(email = email, password = password)
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthUser {
        return repository.signInWithGoogleIdToken(idToken = idToken)
    }

    override suspend fun sendPasswordReset(email: String) {
        repository.sendPasswordReset(email = email)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        repository.changePassword(currentPassword = currentPassword, newPassword = newPassword)
    }

    override suspend fun signOut() {
        repository.signOut()
    }

    override suspend fun deleteAccount() {
        repository.deleteAccount()
    }
}
