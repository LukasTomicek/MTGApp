package mtg.app.feature.trade.domain

class SearchMarketPlaceCardsUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        idToken: String,
        viewerUid: String,
        query: String,
    ): List<MarketPlaceCard> {
        return repository.searchMarketPlaceCards(
            idToken = idToken,
            viewerUid = viewerUid,
            query = query,
        )
    }
}
