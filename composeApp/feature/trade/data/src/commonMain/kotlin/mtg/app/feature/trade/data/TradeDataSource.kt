package mtg.app.feature.trade.data

import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.TradeChatRoom
import mtg.app.feature.trade.domain.TradeMatchNotification
import mtg.app.feature.trade.domain.TradeUserMatch

interface TradeDataSource {
    suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard>
    suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard>
    suspend fun searchCardPrints(cardName: String): List<MtgCard>
    suspend fun fetchDefaultCardsBulkUrl(): String?
    suspend fun listEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry>
    suspend fun listEntriesFromBackend(
        uid: String,
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
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
    )
    suspend fun replaceMarketplaceUserSellEntries(
        uid: String,
        idToken: String,
        entries: List<StoredTradeCardEntry>,
    )
    suspend fun replaceMarketplaceUserBuyEntries(
        uid: String,
        idToken: String,
        entries: List<StoredTradeCardEntry>,
    )
    suspend fun listMarketplaceSellEntriesByUser(idToken: String): Map<String, List<StoredTradeCardEntry>>
    suspend fun listMarketplaceBuyEntriesByUser(idToken: String): Map<String, List<StoredTradeCardEntry>>
    suspend fun listUnavailableSellerMarketKeys(idToken: String): Set<String>
    suspend fun upsertUserNotification(
        uid: String,
        idToken: String,
        notification: TradeMatchNotification,
    )
    suspend fun upsertUserMatch(
        uid: String,
        idToken: String,
        match: TradeUserMatch,
    )
    suspend fun upsertChatRoom(
        idToken: String,
        room: TradeChatRoom,
    )
    suspend fun listUserMatches(uid: String, idToken: String): List<TradeUserMatch>
    suspend fun loadUserNickname(uid: String, idToken: String): String?
    suspend fun listMapPins(uid: String, idToken: String): List<StoredMapPin>
    suspend fun replaceUserMapPins(uid: String, idToken: String, pins: List<StoredMapPin>)
    suspend fun replaceMarketplaceMapPins(uid: String, idToken: String, pins: List<StoredMapPin>)
    suspend fun listMarketplaceMapPinsByUser(idToken: String): Map<String, List<StoredMapPin>>
    suspend fun searchMarketPlaceCardsFromBackend(
        viewerUid: String,
        query: String,
    ): List<MarketPlaceCard>
    suspend fun loadRecentMarketPlaceCardsFromBackend(
        viewerUid: String,
        limit: Int,
    ): List<MarketPlaceCard>
    suspend fun loadMarketPlaceSellersFromBackend(
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller>
}
