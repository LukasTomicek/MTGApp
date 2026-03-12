package mtg.app.feature.trade.data

import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.obj.EnsureMarketPlaceChatRequest
import mtg.app.feature.trade.domain.obj.MarketPlaceCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceRecentCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceSellersQuery
import mtg.app.core.domain.obj.AuthContext

interface TradeDataSource {
    suspend fun ensureMarketPlaceChat(
        context: AuthContext,
        request: EnsureMarketPlaceChatRequest,
    ): String

    suspend fun listEntries(context: AuthContext, listType: TradeListType): List<StoredTradeCardEntry>

    suspend fun listOfferEntries(context: AuthContext, listType: TradeListType): List<StoredTradeCardEntry>

    suspend fun upsertEntry(
        context: AuthContext,
        listType: TradeListType,
        entry: StoredTradeCardEntry,
    )

    suspend fun deleteEntry(
        context: AuthContext,
        listType: TradeListType,
        entryId: String,
    )

    suspend fun syncOfferEntries(
        context: AuthContext,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
    )

    suspend fun syncMatchNotifications(context: AuthContext, listType: TradeListType? = null)

    suspend fun listMapPins(context: AuthContext): List<StoredMapPin>

    suspend fun replaceMapPins(context: AuthContext, pins: List<StoredMapPin>)

    suspend fun searchMarketPlaceCards(context: AuthContext, query: MarketPlaceCardsQuery): List<MarketPlaceCard>

    suspend fun loadRecentMarketPlaceCards(
        context: AuthContext,
        query: MarketPlaceRecentCardsQuery,
    ): List<MarketPlaceCard>

    suspend fun loadMarketPlaceSellers(
        context: AuthContext,
        query: MarketPlaceSellersQuery,
    ): List<MarketPlaceSeller>
}
