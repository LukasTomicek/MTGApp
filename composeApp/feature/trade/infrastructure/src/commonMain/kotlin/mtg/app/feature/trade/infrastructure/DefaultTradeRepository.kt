package mtg.app.feature.trade.infrastructure

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

class DefaultTradeRepository(
    private val dataSource: TradeDataSource,
) : TradeRepository {
    override suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard> {
        return dataSource.searchCards(query = query, filter = filter)
    }

    override suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard> {
        return dataSource.resolveCardsByExactNames(names)
    }

    override suspend fun searchCardPrints(cardName: String): List<MtgCard> {
        return dataSource.searchCardPrints(cardName = cardName)
    }

    override suspend fun fetchDefaultCardsBulkUrl(): String? {
        return dataSource.fetchDefaultCardsBulkUrl()
    }

    override suspend fun loadListEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry> {
        if (listType == TradeListType.BUY_LIST || listType == TradeListType.SELL_LIST) {
            val backendEntries = dataSource.listEntriesFromBackend(
                uid = uid,
                idToken = idToken,
                listType = listType,
            )
            println("TradeBE: repository loadListEntries BE-only listType=$listType count=${backendEntries.size}")
            return backendEntries
        }
        return dataSource.listEntries(
            uid = uid,
            idToken = idToken,
            listType = listType,
        )
    }

    override suspend fun replaceListEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
        actorEmail: String?,
    ) {
        if (listType == TradeListType.BUY_LIST || listType == TradeListType.SELL_LIST) {
            dataSource.replaceEntriesInBackend(
                uid = uid,
                idToken = idToken,
                listType = listType,
                entries = entries,
            )
            dataSource.syncMatchNotifications(
                idToken = idToken,
                listType = listType,
            )
            println("TradeBE: repository replaceListEntries BE-only listType=$listType count=${entries.size}")
            return
        }

        val currentEntries = dataSource.listEntries(
            uid = uid,
            idToken = idToken,
            listType = listType,
        )
        val currentIds = currentEntries.map { it.entryId }.toSet()
        val newIds = entries.map { it.entryId }.toSet()
        val idsToDelete = currentIds - newIds

        entries.forEach { entry ->
            dataSource.upsertEntry(
                uid = uid,
                idToken = idToken,
                listType = listType,
                entry = entry,
            )
        }

        idsToDelete.forEach { entryId ->
            dataSource.deleteEntry(
                uid = uid,
                idToken = idToken,
                listType = listType,
                entryId = entryId,
            )
        }

    }

    override suspend fun loadMapPins(uid: String, idToken: String): List<StoredMapPin> {
        return dataSource.listMapPins(uid = uid, idToken = idToken)
    }

    override suspend fun replaceMapPins(
        uid: String,
        idToken: String,
        pins: List<StoredMapPin>,
        actorEmail: String?,
        triggerRematch: Boolean,
    ) {
        dataSource.replaceUserMapPins(uid = uid, idToken = idToken, pins = pins)

        if (!triggerRematch) return
        dataSource.syncMatchNotifications(
            idToken = idToken,
            listType = null,
        )
    }

    override suspend fun searchMarketPlaceCards(
        idToken: String,
        viewerUid: String,
        query: String,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        return dataSource.searchMarketPlaceCardsFromBackend(
            idToken = idToken,
            viewerUid = viewerUid,
            query = query,
            offerType = offerType,
        )
    }

    override suspend fun loadRecentMarketPlaceCards(
        idToken: String,
        viewerUid: String,
        limit: Int,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        return dataSource.loadRecentMarketPlaceCardsFromBackend(
            idToken = idToken,
            viewerUid = viewerUid,
            limit = limit,
            offerType = offerType,
        )
    }

    override suspend fun loadMarketPlaceSellers(
        idToken: String,
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller> {
        return dataSource.loadMarketPlaceSellersFromBackend(
            idToken = idToken,
            viewerUid = viewerUid,
            cardId = cardId,
            cardName = cardName,
        )
    }

    override suspend fun ensureMarketPlaceChat(
        idToken: String,
        buyerUid: String,
        buyerEmail: String,
        sellerUid: String,
        sellerEmail: String,
        cardId: String,
        cardName: String,
    ): String {
        return dataSource.ensureMarketPlaceChat(
            idToken = idToken,
            buyerUid = buyerUid,
            buyerEmail = buyerEmail,
            sellerUid = sellerUid,
            sellerEmail = sellerEmail,
            cardId = cardId,
            cardName = cardName,
        )
    }
}
