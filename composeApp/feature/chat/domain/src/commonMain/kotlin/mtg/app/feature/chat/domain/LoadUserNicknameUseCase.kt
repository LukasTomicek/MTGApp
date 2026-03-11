package mtg.app.feature.chat.domain

class LoadUserNicknameUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): String? {
        return repository.loadUserNickname(uid = uid, idToken = idToken)
    }
}
