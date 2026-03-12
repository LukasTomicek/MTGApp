package mtg.app.feature.trade.domain

class SearchMarketPlaceCardsUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        idToken: String,
        viewerUid: String,
        query: String,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        return repository.searchMarketPlaceCards(
            idToken = idToken,
            viewerUid = viewerUid,
            query = query,
            offerType = offerType,
        )
    }
}
