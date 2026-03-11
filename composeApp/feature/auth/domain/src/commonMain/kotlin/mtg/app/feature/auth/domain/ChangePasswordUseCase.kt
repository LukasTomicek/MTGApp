package mtg.app.feature.auth.domain

class ChangePasswordUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(newPassword: String) {
        repository.changePassword(newPassword = newPassword)
    }
}
