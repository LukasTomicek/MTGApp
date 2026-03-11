package mtg.app.feature.auth.domain

class SignUpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AuthUser {
        return repository.signUp(email = email, password = password)
    }
}
