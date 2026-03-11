package mtg.app.feature.chat.domain

class LoadChatMessagesUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(chatId: String, idToken: String): List<ChatMessage> {
        return repository.loadChatMessages(chatId = chatId, idToken = idToken)
    }
}
