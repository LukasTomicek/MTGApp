package mtg.app.feature.auth.infrastructure

import mtg.app.feature.auth.data.AuthDataSource
import mtg.app.feature.auth.domain.AuthUser

class FirebaseAuthDataSource(
    private val service: AuthService,
) : AuthDataSource {
    override suspend fun restoreCurrentUser(): AuthUser? {
        return service.restoreCurrentUser()
    }

    override suspend fun signIn(email: String, password: String): AuthUser {
        return service.signIn(email = email, password = password)
    }

    override suspend fun signUp(email: String, password: String): AuthUser {
        return service.signUp(email = email, password = password)
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthUser {
        return service.signInWithGoogleIdToken(idToken = idToken)
    }

    override suspend fun sendPasswordReset(email: String) {
        service.sendPasswordReset(email = email)
    }

    override suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        idToken: String,
    ) {
        service.changePassword(
            email = email,
            currentPassword = currentPassword,
            newPassword = newPassword,
            idToken = idToken,
        )
    }

    override suspend fun deleteAccount(idToken: String) {
        service.deleteAccount(idToken = idToken)
    }

    override suspend fun signOut() {
        service.signOut()
    }
}
