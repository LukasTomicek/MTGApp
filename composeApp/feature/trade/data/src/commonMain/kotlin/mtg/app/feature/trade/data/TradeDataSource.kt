package mtg.app.feature.trade.data

import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller

interface TradeDataSource {
    suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard>
    suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard>
    suspend fun searchCardPrints(cardName: String): List<MtgCard>
    suspend fun fetchDefaultCardsBulkUrl(): String?
    suspend fun ensureMarketPlaceChat(
        idToken: String,
        buyerUid: String,
        buyerEmail: String,
        sellerUid: String,
        sellerEmail: String,
        cardId: String,
        cardName: String,
    ): String
    suspend fun listEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry>
    suspend fun listEntriesFromBackend(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry>
    suspend fun upsertEntry(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entry: StoredTradeCardEntry,
    )
    suspend fun deleteEntry(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entryId: String,
    )
    suspend fun replaceEntriesInBackend(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
    )
    suspend fun syncMatchNotifications(
        idToken: String,
        listType: TradeListType? = null,
    )
    suspend fun listMapPins(uid: String, idToken: String): List<StoredMapPin>
    suspend fun replaceUserMapPins(uid: String, idToken: String, pins: List<StoredMapPin>)
    suspend fun searchMarketPlaceCardsFromBackend(
        idToken: String,
        viewerUid: String,
        query: String,
        offerType: mtg.app.feature.trade.domain.MarketPlaceOfferType,
    ): List<MarketPlaceCard>
    suspend fun loadRecentMarketPlaceCardsFromBackend(
        idToken: String,
        viewerUid: String,
        limit: Int,
        offerType: mtg.app.feature.trade.domain.MarketPlaceOfferType,
    ): List<MarketPlaceCard>
    suspend fun loadMarketPlaceSellersFromBackend(
        idToken: String,
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller>
}
