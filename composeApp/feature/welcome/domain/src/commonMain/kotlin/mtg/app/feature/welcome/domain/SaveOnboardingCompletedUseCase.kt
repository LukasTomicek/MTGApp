package mtg.app.feature.welcome.domain

class SaveOnboardingCompletedUseCase(
    private val repository: WelcomeRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String, completed: Boolean) {
        repository.saveOnboardingCompleted(uid = uid, idToken = idToken, completed = completed)
    }
}
