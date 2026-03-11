package mtg.app.feature.chat.domain

class SendChatMessageUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(
        uid: String,
        idToken: String,
        chatId: String,
        senderEmail: String,
        text: String,
    ) {
        repository.sendMessage(
            uid = uid,
            idToken = idToken,
            chatId = chatId,
            senderEmail = senderEmail,
            text = text,
        )
    }
}
