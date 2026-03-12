package mtg.app.feature.trade.domain

class LoadRecentMarketPlaceCardsUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        idToken: String,
        viewerUid: String,
        limit: Int,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        return repository.loadRecentMarketPlaceCards(
            idToken = idToken,
            viewerUid = viewerUid,
            limit = limit,
            offerType = offerType,
        )
    }
}
