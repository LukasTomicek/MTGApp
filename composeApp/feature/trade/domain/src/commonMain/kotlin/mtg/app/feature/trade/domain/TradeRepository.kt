package mtg.app.feature.trade.domain

interface TradeRepository {
    suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard>
    suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard>
    suspend fun searchCardPrints(cardName: String): List<MtgCard>
    suspend fun fetchDefaultCardsBulkUrl(): String?
    suspend fun loadListEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry>
    suspend fun replaceListEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
        actorEmail: String? = null,
    )
    suspend fun loadMapPins(uid: String, idToken: String): List<StoredMapPin>
    suspend fun replaceMapPins(
        uid: String,
        idToken: String,
        pins: List<StoredMapPin>,
        actorEmail: String? = null,
        triggerRematch: Boolean = true,
    )
    suspend fun searchMarketPlaceCards(
        idToken: String,
        viewerUid: String,
        query: String,
    ): List<MarketPlaceCard>
    suspend fun loadRecentMarketPlaceCards(
        idToken: String,
        viewerUid: String,
        limit: Int,
    ): List<MarketPlaceCard>
    suspend fun loadMarketPlaceSellers(
        idToken: String,
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller>
    suspend fun ensureMarketPlaceChat(
        idToken: String,
        buyerUid: String,
        buyerEmail: String,
        sellerUid: String,
        sellerEmail: String,
        cardId: String,
        cardName: String,
    ): String
}
