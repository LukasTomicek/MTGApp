package mtg.app.feature.trade.domain

class SearchTradeCardsUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(query: String, filter: TradeFilter): List<MtgCard> {
        return repository.searchCards(query = query, filter = filter)
    }
}
