package mtg.app.feature.auth.domain

class ChangePasswordUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String) {
        repository.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword,
        )
    }
}
