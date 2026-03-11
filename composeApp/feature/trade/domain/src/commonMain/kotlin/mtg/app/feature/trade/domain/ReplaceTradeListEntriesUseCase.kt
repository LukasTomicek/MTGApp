package mtg.app.feature.trade.domain

class ReplaceTradeListEntriesUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
        actorEmail: String? = null,
    ) {
        repository.replaceListEntries(
            uid = uid,
            idToken = idToken,
            listType = listType,
            entries = entries,
            actorEmail = actorEmail,
        )
    }
}
