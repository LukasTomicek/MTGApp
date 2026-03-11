package mtg.app.feature.auth.domain

class SignOutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}
