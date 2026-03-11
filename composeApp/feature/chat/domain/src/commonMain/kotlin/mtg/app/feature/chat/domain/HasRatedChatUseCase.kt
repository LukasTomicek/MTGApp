package mtg.app.feature.chat.domain

class HasRatedChatUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String, chatId: String): Boolean {
        return repository.hasRatedChat(uid = uid, idToken = idToken, chatId = chatId)
    }
}
