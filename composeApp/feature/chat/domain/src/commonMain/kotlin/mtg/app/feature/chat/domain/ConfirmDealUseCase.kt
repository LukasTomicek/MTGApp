package mtg.app.feature.chat.domain

class ConfirmDealUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String, chatId: String) {
        repository.confirmDeal(uid = uid, idToken = idToken, chatId = chatId)
    }
}
