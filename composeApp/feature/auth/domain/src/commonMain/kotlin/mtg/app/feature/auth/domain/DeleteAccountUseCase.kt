package mtg.app.feature.auth.domain

class DeleteAccountUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.deleteAccount()
    }
}
