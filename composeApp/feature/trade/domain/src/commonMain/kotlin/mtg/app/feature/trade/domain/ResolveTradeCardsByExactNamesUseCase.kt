package mtg.app.feature.trade.domain

class ResolveTradeCardsByExactNamesUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(names: Set<String>): Map<String, MtgCard> {
        if (names.isEmpty()) return emptyMap()
        return repository.resolveCardsByExactNames(names)
    }
}
