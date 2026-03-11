package mtg.app.feature.auth.domain

class SignInWithGoogleUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String): AuthUser {
        return repository.signInWithGoogleIdToken(idToken = idToken)
    }
}
