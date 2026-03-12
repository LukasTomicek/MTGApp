package mtg.app.feature.trade.domain

import mtg.app.feature.trade.domain.obj.EnsureMarketPlaceChatRequest
import mtg.app.feature.trade.domain.obj.MarketPlaceCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceRecentCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceSellersQuery
import mtg.app.core.domain.obj.AuthContext

class DefaultTradeService(
    private val repository: TradeRepository,
) : TradeService {

    override suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard> {
        return repository.searchCards(query = query, filter = filter)
    }

    override suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard> {
        return repository.resolveCardsByExactNames(names = names)
    }

    override suspend fun searchCardPrints(cardName: String): List<MtgCard> {
        return repository.searchCardPrints(cardName = cardName)
    }

    override suspend fun fetchDefaultCardsBulkUrl(): String? {
        return repository.fetchDefaultCardsBulkUrl()
    }

    override suspend fun loadListEntries(
        context: AuthContext,
        listType: TradeListType
    ): List<StoredTradeCardEntry> {
        return repository.loadListEntries(context = context, listType = listType)
    }

    override suspend fun replaceListEntries(
        context: AuthContext,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
        actorEmail: String?,
    ) {
        repository.replaceListEntries(
            context = context,
            listType = listType,
            entries = entries,
            actorEmail = actorEmail,
        )
    }

    override suspend fun loadMapPins(context: AuthContext): List<StoredMapPin> {
        return repository.loadMapPins(context = context)
    }

    override suspend fun replaceMapPins(
        context: AuthContext,
        pins: List<StoredMapPin>,
        actorEmail: String?,
        triggerRematch: Boolean,
    ) {
        repository.replaceMapPins(
            context = context,
            pins = pins,
            actorEmail = actorEmail,
            triggerRematch = triggerRematch,
        )
    }

    override suspend fun searchMarketPlaceCards(context: AuthContext, query: MarketPlaceCardsQuery): List<MarketPlaceCard> {
        return repository.searchMarketPlaceCards(context = context, query = query)
    }

    override suspend fun loadRecentMarketPlaceCards(
        context: AuthContext,
        query: MarketPlaceRecentCardsQuery,
    ): List<MarketPlaceCard> {
        return repository.loadRecentMarketPlaceCards(context = context, query = query)
    }

    override suspend fun loadMarketPlaceSellers(
        context: AuthContext,
        query: MarketPlaceSellersQuery,
    ): List<MarketPlaceSeller> {
        return repository.loadMarketPlaceSellers(context = context, query = query)
    }

    override suspend fun ensureMarketPlaceChat(context: AuthContext, request: EnsureMarketPlaceChatRequest): String {
        return repository.ensureMarketPlaceChat(context = context, request = request)
    }
}
