package mtg.app.feature.trade.infrastructure

import mtg.app.feature.trade.data.TradeDataSource
import mtg.app.feature.trade.data.toStoredTradeEntriesByUserFromMarketplace
import mtg.app.feature.trade.data.toStoredMapPins
import mtg.app.feature.trade.data.toStoredMapPinsByUserFromMarketplace
import mtg.app.feature.trade.data.toStoredTradeEntries
import mtg.app.feature.trade.data.toCards
import mtg.app.feature.trade.data.toDefaultCardsBulkUrl
import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeChatRoom
import mtg.app.feature.trade.domain.TradeMatchNotification
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.TradeUserMatch
import mtg.app.feature.trade.infrastructure.service.TradeService

class ScryfallTradeDataSource(
    private val service: TradeService,
) : TradeDataSource {

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
        listType: TradeListType,
    ): List<StoredTradeCardEntry> {
        return service.listEntriesFromBackend(
            uid = uid,
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
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
    ) {
        service.replaceEntriesInBackend(
            uid = uid,
            listType = listType,
            entries = entries,
        )
    }

    override suspend fun replaceMarketplaceUserSellEntries(
        uid: String,
        idToken: String,
        entries: List<StoredTradeCardEntry>,
    ) {
        service.replaceMarketplaceUserSellEntries(
            uid = uid,
            idToken = idToken,
            entries = entries,
        )
    }

    override suspend fun replaceMarketplaceUserBuyEntries(
        uid: String,
        idToken: String,
        entries: List<StoredTradeCardEntry>,
    ) {
        service.replaceMarketplaceUserBuyEntries(
            uid = uid,
            idToken = idToken,
            entries = entries,
        )
    }

    override suspend fun listMarketplaceSellEntriesByUser(idToken: String): Map<String, List<StoredTradeCardEntry>> {
        return service.listMarketplaceSellEntriesByUser(idToken = idToken).toStoredTradeEntriesByUserFromMarketplace()
    }

    override suspend fun listMarketplaceBuyEntriesByUser(idToken: String): Map<String, List<StoredTradeCardEntry>> {
        return service.listMarketplaceBuyEntries(idToken = idToken).toStoredTradeEntriesByUserFromMarketplace()
    }

    override suspend fun listUnavailableSellerMarketKeys(idToken: String): Set<String> {
        val chats = service.listChats(idToken = idToken)
        if (chats.isEmpty()) return emptySet()

        return chats.values.mapNotNull { node ->
            val chatRoot = node as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
            val meta = chatRoot["meta"] as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
            val status = (meta["dealStatus"] as? kotlinx.serialization.json.JsonPrimitive)?.content.orEmpty()
            if (!status.equals("PROPOSED", ignoreCase = true) &&
                !status.equals("COMPLETED", ignoreCase = true)
            ) {
                return@mapNotNull null
            }
            val sellerUid = (meta["sellerUid"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                ?.trim()
                .orEmpty()
            if (sellerUid.isBlank()) return@mapNotNull null

            val cardId = (meta["cardId"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                ?.trim()
                .orEmpty()
            val cardName = (meta["cardName"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                ?.trim()
                .orEmpty()

            val marketKey = if (cardId.isNotBlank()) {
                "id:${cardId.lowercase()}"
            } else {
                "name:${cardName.lowercase()}"
            }
            "${sellerUid.lowercase()}|$marketKey"
        }.toSet()
    }

    override suspend fun upsertUserNotification(
        uid: String,
        idToken: String,
        notification: TradeMatchNotification,
    ) {
        service.upsertUserNotification(
            uid = uid,
            idToken = idToken,
            notification = notification,
        )
    }

    override suspend fun upsertUserMatch(
        uid: String,
        idToken: String,
        match: TradeUserMatch,
    ) {
        service.upsertUserMatch(
            uid = uid,
            idToken = idToken,
            match = match,
        )
    }

    override suspend fun upsertChatRoom(idToken: String, room: TradeChatRoom) {
        service.upsertChatRoom(
            idToken = idToken,
            room = room,
        )
    }

    override suspend fun listUserMatches(uid: String, idToken: String): List<TradeUserMatch> {
        return service.listUserMatches(uid = uid, idToken = idToken)
    }

    override suspend fun loadUserNickname(uid: String, idToken: String): String? {
        return service.loadUserNickname(uid = uid, idToken = idToken)
    }

    override suspend fun listMapPins(uid: String, idToken: String): List<StoredMapPin> {
        return service.listMapPins(uid = uid, idToken = idToken).toStoredMapPins()
    }

    override suspend fun replaceUserMapPins(uid: String, idToken: String, pins: List<StoredMapPin>) {
        service.replaceUserMapPins(uid = uid, idToken = idToken, pins = pins)
    }

    override suspend fun replaceMarketplaceMapPins(uid: String, idToken: String, pins: List<StoredMapPin>) {
        service.replaceMarketplaceMapPins(uid = uid, idToken = idToken, pins = pins)
    }

    override suspend fun listMarketplaceMapPinsByUser(idToken: String): Map<String, List<StoredMapPin>> {
        return service.listMarketplaceMapPins(idToken = idToken).toStoredMapPinsByUserFromMarketplace()
    }

    override suspend fun searchMarketPlaceCardsFromBackend(
        viewerUid: String,
        query: String,
    ): List<MarketPlaceCard> {
        return service.searchMarketPlaceCardsFromBackend(
            viewerUid = viewerUid,
            query = query,
        )
    }

    override suspend fun loadRecentMarketPlaceCardsFromBackend(
        viewerUid: String,
        limit: Int,
    ): List<MarketPlaceCard> {
        return service.loadRecentMarketPlaceCardsFromBackend(
            viewerUid = viewerUid,
            limit = limit,
        )
    }

    override suspend fun loadMarketPlaceSellersFromBackend(
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller> {
        return service.loadMarketPlaceSellersFromBackend(
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
