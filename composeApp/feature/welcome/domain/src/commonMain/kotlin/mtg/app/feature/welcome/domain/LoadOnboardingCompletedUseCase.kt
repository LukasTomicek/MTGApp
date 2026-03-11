package mtg.app.feature.welcome.domain

class LoadOnboardingCompletedUseCase(
    private val repository: WelcomeRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): Boolean {
        return repository.loadOnboardingCompleted(uid = uid, idToken = idToken)
    }
}
