package mtg.app.feature.chat.domain

class DeleteChatThreadUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(
        uid: String,
        idToken: String,
        chatId: String,
        counterpartUid: String,
    ) {
        repository.deleteThread(
            uid = uid,
            idToken = idToken,
            chatId = chatId,
            counterpartUid = counterpartUid,
        )
    }
}
