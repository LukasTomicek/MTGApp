package mtg.app.feature.trade.infrastructure.service

import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.TradeChatRoom
import mtg.app.feature.trade.domain.TradeMatchNotification
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.TradeUserMatch
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import kotlinx.serialization.json.JsonObject

interface TradeService {
    suspend fun searchCards(query: String): JsonObject
    suspend fun findCardsByNames(names: List<String>): JsonObject
    suspend fun searchCardPrints(cardName: String): JsonObject
    suspend fun fetchBulkData(): JsonObject
    suspend fun listEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): JsonObject
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
    suspend fun listMarketplaceSellEntriesByUser(idToken: String): JsonObject
    suspend fun listMarketplaceBuyEntries(idToken: String): JsonObject
    suspend fun listChats(idToken: String): JsonObject
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
    suspend fun listMapPins(uid: String, idToken: String): JsonObject
    suspend fun replaceUserMapPins(uid: String, idToken: String, pins: List<StoredMapPin>)
    suspend fun replaceMarketplaceMapPins(uid: String, idToken: String, pins: List<StoredMapPin>)
    suspend fun listMarketplaceMapPins(idToken: String): JsonObject
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
