package mtg.app.feature.chat.domain

class LoadUserSellOffersUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): List<UserSellOffer> {
        return repository.loadUserSellOffers(uid = uid, idToken = idToken)
    }
}
