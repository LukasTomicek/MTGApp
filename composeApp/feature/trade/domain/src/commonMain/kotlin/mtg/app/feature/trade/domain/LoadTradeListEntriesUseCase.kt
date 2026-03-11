package mtg.app.feature.trade.domain

class LoadTradeListEntriesUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry> {
        return repository.loadListEntries(
            uid = uid,
            idToken = idToken,
            listType = listType,
        )
    }
}
