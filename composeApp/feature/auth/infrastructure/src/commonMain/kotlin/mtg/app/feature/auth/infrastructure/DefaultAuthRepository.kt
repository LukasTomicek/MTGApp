package mtg.app.feature.auth.infrastructure

import mtg.app.feature.auth.data.AuthDataSource
import mtg.app.feature.auth.domain.AuthRepository
import mtg.app.feature.auth.domain.AuthUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultAuthRepository(
    private val dataSource: AuthDataSource,
    private val sessionStore: InMemoryAuthSessionStore,
) : AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val currentUser: StateFlow<AuthUser?> = sessionStore.currentUser
    override val isInitialized: StateFlow<Boolean> = sessionStore.isInitialized

    init {
        scope.launch {
            runCatching { dataSource.restoreCurrentUser() }
                .onSuccess { restored ->
                    val current = sessionStore.currentUser.value
                    if (restored != null || current == null) {
                        sessionStore.updateUser(restored)
                    }
                }
                .onFailure {
                    if (sessionStore.currentUser.value == null) {
                        sessionStore.updateUser(null)
                    }
                }
            sessionStore.markInitialized()
        }
    }

    override suspend fun signIn(email: String, password: String): AuthUser {
        val user = dataSource.signIn(email = email, password = password)
        sessionStore.updateUser(user)
        return user
    }

    override suspend fun signUp(email: String, password: String): AuthUser {
        val user = dataSource.signUp(email = email, password = password)
        sessionStore.updateUser(user)
        return user
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthUser {
        val user = dataSource.signInWithGoogleIdToken(idToken = idToken)
        sessionStore.updateUser(user)
        return user
    }

    override suspend fun sendPasswordReset(email: String) {
        dataSource.sendPasswordReset(email = email)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        val user = currentUser.value ?: error("No signed in user")
        dataSource.changePassword(
            email = user.email,
            currentPassword = currentPassword,
            newPassword = newPassword,
            idToken = user.idToken,
        )
    }

    override suspend fun signOut() {
        dataSource.signOut()
        sessionStore.updateUser(null)
    }

    override suspend fun deleteAccount() {
        val user = currentUser.value ?: error("No signed in user")
        dataSource.deleteAccount(idToken = user.idToken)
        dataSource.signOut()
        sessionStore.updateUser(null)
    }
}
