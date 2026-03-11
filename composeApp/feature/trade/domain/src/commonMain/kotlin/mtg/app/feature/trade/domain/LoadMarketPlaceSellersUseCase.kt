package mtg.app.feature.trade.domain

class LoadMarketPlaceSellersUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        idToken: String,
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller> {
        return repository.loadMarketPlaceSellers(
            idToken = idToken,
            viewerUid = viewerUid,
            cardId = cardId,
            cardName = cardName,
        )
    }
}
