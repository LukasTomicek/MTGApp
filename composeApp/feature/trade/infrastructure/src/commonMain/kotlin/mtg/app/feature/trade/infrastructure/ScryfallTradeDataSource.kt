package mtg.app.feature.trade.infrastructure

import mtg.app.feature.trade.data.TradeDataSource
import mtg.app.feature.trade.data.toStoredMapPins
import mtg.app.feature.trade.data.toStoredTradeEntries
import mtg.app.feature.trade.data.toCards
import mtg.app.feature.trade.data.toDefaultCardsBulkUrl
import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceOfferType
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.infrastructure.service.TradeService

class ScryfallTradeDataSource(
    private val service: TradeService,
) : TradeDataSource {
    override suspend fun ensureMarketPlaceChat(
        idToken: String,
        buyerUid: String,
        buyerEmail: String,
        sellerUid: String,
        sellerEmail: String,
        cardId: String,
        cardName: String,
    ): String {
        return service.ensureMarketPlaceChat(
            idToken = idToken,
            buyerUid = buyerUid,
            buyerEmail = buyerEmail,
            sellerUid = sellerUid,
            sellerEmail = sellerEmail,
            cardId = cardId,
            cardName = cardName,
        )
    }

    override suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard> {
        val searchQuery = buildSearchQuery(query = query, filter = filter)
        return service.searchCards(query = searchQuery).toCards()
    }

    override suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard> {
        if (names.isEmpty()) return emptyMap()

        val sanitized = names
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
        if (sanitized.isEmpty()) return emptyMap()

        val byKey = mutableMapOf<String, MtgCard>()
        sanitized.chunked(COLLECTION_BATCH_SIZE).forEach { batch ->
            val cards = service.findCardsByNames(batch).toCards()
            batch.forEach { requestedName ->
                val key = requestedName.lowercase()
                val matched = cards.firstOrNull { it.name.equals(requestedName, ignoreCase = true) }
                    ?: cards.firstOrNull { it.name.contains(requestedName, ignoreCase = true) }
                if (matched != null) {
                    byKey[key] = matched
                }
            }
        }
        return byKey
    }

    override suspend fun fetchDefaultCardsBulkUrl(): String? {
        return service.fetchBulkData().toDefaultCardsBulkUrl()
    }

    override suspend fun searchCardPrints(cardName: String): List<MtgCard> {
        return service.searchCardPrints(cardName = cardName).toCards()
    }

    override suspend fun listEntries(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry> {
        return service.listEntries(uid = uid, idToken = idToken, listType = listType).toStoredTradeEntries()
    }

    override suspend fun listEntriesFromBackend(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry> {
        return service.listEntriesFromBackend(
            uid = uid,
            idToken = idToken,
            listType = listType,
        )
    }

    override suspend fun upsertEntry(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entry: StoredTradeCardEntry,
    ) {
        service.upsertEntry(
            uid = uid,
            idToken = idToken,
            listType = listType,
            entry = entry,
        )
    }

    override suspend fun deleteEntry(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entryId: String,
    ) {
        service.deleteEntry(
            uid = uid,
            idToken = idToken,
            listType = listType,
            entryId = entryId,
        )
    }

    override suspend fun replaceEntriesInBackend(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
    ) {
        service.replaceEntriesInBackend(
            uid = uid,
            idToken = idToken,
            listType = listType,
            entries = entries,
        )
    }

    override suspend fun syncMatchNotifications(idToken: String, listType: TradeListType?) {
        service.syncMatchNotifications(
            idToken = idToken,
            listType = listType,
        )
    }

    override suspend fun listMapPins(uid: String, idToken: String): List<StoredMapPin> {
        return service.listMapPins(uid = uid, idToken = idToken).toStoredMapPins()
    }

    override suspend fun replaceUserMapPins(uid: String, idToken: String, pins: List<StoredMapPin>) {
        service.replaceUserMapPins(uid = uid, idToken = idToken, pins = pins)
    }

    override suspend fun searchMarketPlaceCardsFromBackend(
        idToken: String,
        viewerUid: String,
        query: String,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        return service.searchMarketPlaceCardsFromBackend(
            idToken = idToken,
            viewerUid = viewerUid,
            query = query,
            offerType = offerType,
        )
    }

    override suspend fun loadRecentMarketPlaceCardsFromBackend(
        idToken: String,
        viewerUid: String,
        limit: Int,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        return service.loadRecentMarketPlaceCardsFromBackend(
            idToken = idToken,
            viewerUid = viewerUid,
            limit = limit,
            offerType = offerType,
        )
    }

    override suspend fun loadMarketPlaceSellersFromBackend(
        idToken: String,
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller> {
        return service.loadMarketPlaceSellersFromBackend(
            idToken = idToken,
            viewerUid = viewerUid,
            cardId = cardId,
            cardName = cardName,
        )
    }

    private fun buildSearchQuery(query: String, filter: TradeFilter): String {
        val base = if (query.isBlank()) "game:paper" else query
        val filterPart = filter.scryfallQuery

        return if (filterPart == null) base else "$base $filterPart"
    }

    private companion object {
        const val COLLECTION_BATCH_SIZE = 70
    }
}
