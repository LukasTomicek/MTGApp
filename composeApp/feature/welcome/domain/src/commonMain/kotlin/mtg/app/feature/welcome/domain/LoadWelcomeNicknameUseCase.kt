package mtg.app.feature.welcome.domain

class LoadWelcomeNicknameUseCase(
    private val repository: WelcomeRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): String? {
        return repository.loadNickname(uid = uid, idToken = idToken)
    }
}
