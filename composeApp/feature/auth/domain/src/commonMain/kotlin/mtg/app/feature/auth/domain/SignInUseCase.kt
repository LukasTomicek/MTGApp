package mtg.app.feature.auth.domain

class SignInUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AuthUser {
        return repository.signIn(email = email, password = password)
    }
}
