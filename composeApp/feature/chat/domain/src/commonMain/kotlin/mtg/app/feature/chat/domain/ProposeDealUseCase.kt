package mtg.app.feature.chat.domain

class ProposeDealUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String, chatId: String) {
        repository.proposeDeal(uid = uid, idToken = idToken, chatId = chatId)
    }
}
