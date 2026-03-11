package mtg.app.feature.chat.domain

class LoadChatMetaUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(chatId: String, idToken: String): ChatMeta? {
        return repository.loadChatMeta(chatId = chatId, idToken = idToken)
    }
}
