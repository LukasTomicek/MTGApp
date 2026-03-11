package mtg.app.feature.auth.domain

class SendPasswordResetUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String) {
        repository.sendPasswordReset(email = email)
    }
}
