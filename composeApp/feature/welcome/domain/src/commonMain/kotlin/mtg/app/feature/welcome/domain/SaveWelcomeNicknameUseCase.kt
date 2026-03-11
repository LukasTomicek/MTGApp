package mtg.app.feature.welcome.domain

class SaveWelcomeNicknameUseCase(
    private val repository: WelcomeRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String, nickname: String) {
        repository.saveNickname(uid = uid, idToken = idToken, nickname = nickname)
    }
}
