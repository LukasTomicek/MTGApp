package mtg.app.feature.trade.infrastructure

import mtg.app.feature.trade.data.ScryfallDataSource
import mtg.app.feature.trade.data.TradeDataSource
import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.TradeRepository
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceOfferType
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.obj.EnsureMarketPlaceChatRequest
import mtg.app.feature.trade.domain.obj.MarketPlaceCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceRecentCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceSellersQuery
import mtg.app.core.domain.obj.AuthContext

class DefaultTradeRepository(
    private val scryfallDataSource: ScryfallDataSource,
    private val dataSource: TradeDataSource,
) : TradeRepository {
    private fun authContext(uid: String, idToken: String): AuthContext = AuthContext(uid = uid, idToken = idToken)

    override suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard> {
        return scryfallDataSource.searchCards(query = query, filter = filter)
    }

    override suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard> {
        return scryfallDataSource.resolveCardsByExactNames(names)
    }

    override suspend fun searchCardPrints(cardName: String): List<MtgCard> {
        return scryfallDataSource.searchCardPrints(cardName = cardName)
    }

    override suspend fun fetchDefaultCardsBulkUrl(): String? {
        return scryfallDataSource.fetchDefaultCardsBulkUrl()
    }

    override suspend fun loadListEntries(
        context: AuthContext,
        listType: TradeListType
    ): List<StoredTradeCardEntry> {
        if (listType == TradeListType.BUY_LIST || listType == TradeListType.SELL_LIST) {
            val backendEntries = dataSource.listOfferEntries(context = context, listType = listType)
            println("TradeBE: repository loadListEntries BE-only listType=$listType count=${backendEntries.size}")
            return backendEntries
        }
        return dataSource.listEntries(context = context, listType = listType)
    }

    override suspend fun replaceListEntries(
        context: AuthContext,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
        actorEmail: String?,
    ) {
        if (listType == TradeListType.BUY_LIST || listType == TradeListType.SELL_LIST) {
            dataSource.syncOfferEntries(context = context, listType = listType, entries = entries)
            dataSource.syncMatchNotifications(context = context, listType = listType)
            println("TradeBE: repository replaceListEntries BE-only listType=$listType count=${entries.size}")
            return
        }

        val currentEntries = dataSource.listEntries(context = context, listType = listType)
        val currentIds = currentEntries.map { it.entryId }.toSet()
        val newIds = entries.map { it.entryId }.toSet()
        val idsToDelete = currentIds - newIds

        entries.forEach { entry ->
            dataSource.upsertEntry(context = context, listType = listType, entry = entry)
        }

        idsToDelete.forEach { entryId ->
            dataSource.deleteEntry(context = context, listType = listType, entryId = entryId)
        }

    }

    override suspend fun loadMapPins(context: AuthContext): List<StoredMapPin> {
        return dataSource.listMapPins(context = context)
    }

    override suspend fun replaceMapPins(
        context: AuthContext,
        pins: List<StoredMapPin>,
        actorEmail: String?,
        triggerRematch: Boolean,
    ) {
        dataSource.replaceMapPins(context = context, pins = pins)

        if (!triggerRematch) return
        dataSource.syncMatchNotifications(context = context, listType = null)
    }

    override suspend fun searchMarketPlaceCards(
        context: AuthContext,
        query: MarketPlaceCardsQuery,
    ): List<MarketPlaceCard> {
        return dataSource.searchMarketPlaceCards(
            context = context,
            query = query,
        )
    }

    override suspend fun loadRecentMarketPlaceCards(
        context: AuthContext,
        query: MarketPlaceRecentCardsQuery,
    ): List<MarketPlaceCard> {
        return dataSource.loadRecentMarketPlaceCards(
            context = context,
            query = query,
        )
    }

    override suspend fun loadMarketPlaceSellers(
        context: AuthContext,
        query: MarketPlaceSellersQuery,
    ): List<MarketPlaceSeller> {
        return dataSource.loadMarketPlaceSellers(
            context = context,
            query = query,
        )
    }

    override suspend fun ensureMarketPlaceChat(
        context: AuthContext,
        request: EnsureMarketPlaceChatRequest,
    ): String {
        return dataSource.ensureMarketPlaceChat(
            context = context,
            request = request,
        )
    }
}
