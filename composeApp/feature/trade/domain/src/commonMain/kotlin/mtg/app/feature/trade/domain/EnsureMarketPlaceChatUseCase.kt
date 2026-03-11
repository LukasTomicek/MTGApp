package mtg.app.feature.trade.domain

class EnsureMarketPlaceChatUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        idToken: String,
        buyerUid: String,
        buyerEmail: String,
        sellerUid: String,
        sellerEmail: String,
        cardId: String,
        cardName: String,
    ): String {
        return repository.ensureMarketPlaceChat(
            idToken = idToken,
            buyerUid = buyerUid,
            buyerEmail = buyerEmail,
            sellerUid = sellerUid,
            sellerEmail = sellerEmail,
            cardId = cardId,
            cardName = cardName,
        )
    }
}
