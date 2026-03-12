package mtg.app.feature.trade.domain

import mtg.app.feature.trade.domain.obj.EnsureMarketPlaceChatRequest
import mtg.app.feature.trade.domain.obj.MarketPlaceCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceRecentCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceSellersQuery
import mtg.app.core.domain.obj.AuthContext

interface TradeRepository {
    suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard>
    suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard>
    suspend fun searchCardPrints(cardName: String): List<MtgCard>
    suspend fun fetchDefaultCardsBulkUrl(): String?
    suspend fun loadListEntries(context: AuthContext, listType: TradeListType): List<StoredTradeCardEntry>
    suspend fun replaceListEntries(
        context: AuthContext,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
        actorEmail: String? = null,
    )
    suspend fun loadMapPins(context: AuthContext): List<StoredMapPin>
    suspend fun replaceMapPins(
        context: AuthContext,
        pins: List<StoredMapPin>,
        actorEmail: String? = null,
        triggerRematch: Boolean = true,
    )
    suspend fun searchMarketPlaceCards(context: AuthContext, query: MarketPlaceCardsQuery): List<MarketPlaceCard>
    suspend fun loadRecentMarketPlaceCards(
        context: AuthContext,
        query: MarketPlaceRecentCardsQuery,
    ): List<MarketPlaceCard>
    suspend fun loadMarketPlaceSellers(
        context: AuthContext,
        query: MarketPlaceSellersQuery,
    ): List<MarketPlaceSeller>
    suspend fun ensureMarketPlaceChat(context: AuthContext, request: EnsureMarketPlaceChatRequest): String
}
