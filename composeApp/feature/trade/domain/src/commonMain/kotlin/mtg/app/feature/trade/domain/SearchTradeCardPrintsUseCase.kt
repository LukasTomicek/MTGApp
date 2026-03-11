package mtg.app.feature.trade.domain

class SearchTradeCardPrintsUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(cardName: String): List<MtgCard> {
        return repository.searchCardPrints(cardName = cardName)
    }
}
