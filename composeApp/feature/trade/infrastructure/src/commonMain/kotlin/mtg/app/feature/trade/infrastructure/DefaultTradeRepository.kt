package mtg.app.feature.trade.infrastructure

import mtg.app.feature.trade.data.TradeDataSource
import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.TradeRepository
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeChatRoom
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.TradeMatchNotification
import mtg.app.feature.trade.domain.TradeUserMatch
import kotlin.math.PI
import kotlin.math.pow

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
                listType = listType,
                entries = entries,
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
        dataSource.replaceMarketplaceMapPins(uid = uid, idToken = idToken, pins = pins)

        if (!triggerRematch) return

        val actor = resolveUserDisplayName(
            uid = uid,
            idToken = idToken,
            fallback = actorEmail?.trim().orEmpty().ifBlank { uid },
            cache = mutableMapOf(),
        )
        val sellEntries = loadListEntries(
            uid = uid,
            idToken = idToken,
            listType = TradeListType.SELL_LIST,
        )
        if (sellEntries.isNotEmpty()) {
            pushMatchNotificationsForNewSellEntries(
                sellerUid = uid,
                sellerEmail = actor,
                idToken = idToken,
                before = emptyList(),
                after = sellEntries,
            )
        }

        val buyEntries = loadListEntries(
            uid = uid,
            idToken = idToken,
            listType = TradeListType.BUY_LIST,
        )
        if (buyEntries.isNotEmpty()) {
            pushMatchNotificationsForNewBuyEntries(
                buyerUid = uid,
                buyerEmail = actor,
                idToken = idToken,
                before = emptyList(),
                after = buyEntries,
            )
        }
    }

    override suspend fun searchMarketPlaceCards(
        idToken: String,
        viewerUid: String,
        query: String,
    ): List<MarketPlaceCard> {
        val marketScope = loadVisibleMarketScope(
            idToken = idToken,
            viewerUid = viewerUid,
        )
        if (marketScope.allowedSellerUids.isEmpty() || marketScope.visibleCardKeys.isEmpty()) {
            println("TradeBE: marketplace cards hidden for uid=$viewerUid (no pin or no sellers in range)")
            return emptyList()
        }

        val cards = dataSource.searchMarketPlaceCardsFromBackend(
            viewerUid = viewerUid,
            query = query,
        )
        val filtered = cards.filter { card ->
            card.marketKey() in marketScope.visibleCardKeys
        }
        println("TradeBE: marketplace cards filtered for uid=$viewerUid raw=${cards.size} visible=${filtered.size}")
        return filtered
    }

    override suspend fun loadRecentMarketPlaceCards(
        idToken: String,
        viewerUid: String,
        limit: Int,
    ): List<MarketPlaceCard> {
        val marketScope = loadVisibleMarketScope(
            idToken = idToken,
            viewerUid = viewerUid,
        )
        if (marketScope.allowedSellerUids.isEmpty() || marketScope.visibleCardKeys.isEmpty()) {
            println("TradeBE: marketplace recent hidden for uid=$viewerUid (no pin or no sellers in range)")
            return emptyList()
        }

        val cards = dataSource.loadRecentMarketPlaceCardsFromBackend(
            viewerUid = viewerUid,
            limit = limit,
        )
        val filtered = cards.filter { card ->
            card.marketKey() in marketScope.visibleCardKeys
        }
        println("TradeBE: marketplace recent filtered for uid=$viewerUid raw=${cards.size} visible=${filtered.size}")
        return filtered
    }

    override suspend fun loadMarketPlaceSellers(
        idToken: String,
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller> {
        val marketScope = loadVisibleMarketScope(
            idToken = idToken,
            viewerUid = viewerUid,
        )
        if (marketScope.allowedSellerUids.isEmpty()) {
            println("TradeBE: marketplace sellers hidden for uid=$viewerUid (no pin or no sellers in range)")
            return emptyList()
        }

        val sellers = dataSource.loadMarketPlaceSellersFromBackend(
            viewerUid = viewerUid,
            cardId = cardId,
            cardName = cardName,
        )
        val targetKey = marketKeyOf(cardId = cardId, cardName = cardName)
        val filtered = sellers.filter { seller ->
            seller.uid in marketScope.allowedSellerUids &&
                "${seller.uid.lowercase()}|$targetKey" in marketScope.availableSellerMarketKeys
        }
        println("TradeBE: marketplace sellers filtered for uid=$viewerUid raw=${sellers.size} visible=${filtered.size}")
        return filtered
    }

    private suspend fun loadVisibleMarketScope(
        idToken: String,
        viewerUid: String,
    ): VisibleMarketScope {
        val viewerPins = dataSource.listMapPins(
            uid = viewerUid,
            idToken = idToken,
        )
        if (viewerPins.isEmpty()) {
            return VisibleMarketScope()
        }

        val pinsByUser = dataSource.listMarketplaceMapPinsByUser(idToken = idToken)
        val sellsByUser = dataSource.listMarketplaceSellEntriesByUser(idToken = idToken)
        val unavailableSellerMarketKeys = dataSource.listUnavailableSellerMarketKeys(idToken = idToken)

        val availableSellerMarketKeys = mutableSetOf<String>()
        sellsByUser.forEach { (sellerUid, entries) ->
            entries.forEach { entry ->
                val key = "${sellerUid.lowercase()}|${entry.marketKey()}"
                if (key !in unavailableSellerMarketKeys) {
                    availableSellerMarketKeys += key
                }
            }
        }

        val allowedSellerUids = sellsByUser.keys.filterTo(mutableSetOf()) { sellerUid ->
            sellerUid != viewerUid &&
                availableSellerMarketKeys.any { it.startsWith("${sellerUid.lowercase()}|") } &&
                usersHaveIntersectingPinsStrict(
                    viewerPins = viewerPins,
                    sellerPins = pinsByUser[sellerUid].orEmpty(),
                )
        }

        if (allowedSellerUids.isEmpty()) {
            return VisibleMarketScope()
        }

        val visibleCardKeys = mutableSetOf<String>()
        allowedSellerUids.forEach { sellerUid ->
            sellsByUser[sellerUid].orEmpty().forEach { entry ->
                val scopedKey = "${sellerUid.lowercase()}|${entry.marketKey()}"
                if (scopedKey in availableSellerMarketKeys) {
                    visibleCardKeys += entry.marketKey()
                }
            }
        }

        return VisibleMarketScope(
            allowedSellerUids = allowedSellerUids,
            visibleCardKeys = visibleCardKeys,
            availableSellerMarketKeys = availableSellerMarketKeys,
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
        val safeCardId = cardId.ifBlank { cardName }
        val fallbackChatId = buildChatId(
            firstUid = sellerUid,
            secondUid = buyerUid,
            marketKey = safeCardId,
        )
        val chatId = resolveExistingChatIdForPair(
            idToken = idToken,
            firstUid = sellerUid,
            secondUid = buyerUid,
            fallbackChatId = fallbackChatId,
        )
        upsertMatchArtifacts(
            idToken = idToken,
            chatId = chatId,
            buyerUid = buyerUid,
            buyerEmail = buyerEmail,
            sellerUid = sellerUid,
            sellerEmail = sellerEmail,
            cardId = safeCardId,
            cardName = cardName.ifBlank { "Unknown card" },
        )
        return chatId
    }

    private suspend fun pushMatchNotificationsForNewSellEntries(
        sellerUid: String,
        sellerEmail: String,
        idToken: String,
        before: List<StoredTradeCardEntry>,
        after: List<StoredTradeCardEntry>,
    ) {
        val previousEntryIds = before.map { it.entryId }.toSet()
        val newlyAddedEntries = after.filter { it.entryId !in previousEntryIds }
        if (newlyAddedEntries.isEmpty()) return

        val buyEntriesByUser = dataSource.listMarketplaceBuyEntriesByUser(idToken = idToken)
        val mapPinsByUser = dataSource.listMarketplaceMapPinsByUser(idToken = idToken)
        val displayNameCache = mutableMapOf<String, String>()
        val chatIdCache = mutableMapOf<String, String>()
        displayNameCache[sellerUid] = sellerEmail
        buyEntriesByUser.forEach { (buyerUid, buyEntries) ->
            if (buyerUid == sellerUid) return@forEach
            if (!usersHaveIntersectingPins(sellerUid, buyerUid, mapPinsByUser)) return@forEach

            val buyerWantedKeys = buyEntries.map { it.marketKey() }.toSet()
            newlyAddedEntries.forEach { soldEntry ->
                val key = soldEntry.marketKey()
                if (key !in buyerWantedKeys) return@forEach
                val cardId = soldEntry.cardId
                val cardName = soldEntry.cardName.ifBlank { "Unknown card" }
                val fallbackChatId = buildChatId(
                    firstUid = sellerUid,
                    secondUid = buyerUid,
                    marketKey = key,
                )
                val pairKey = userPairKey(sellerUid, buyerUid)
                val chatId = chatIdCache[pairKey] ?: resolveExistingChatIdForPair(
                    idToken = idToken,
                    firstUid = sellerUid,
                    secondUid = buyerUid,
                    fallbackChatId = fallbackChatId,
                ).also { resolved -> chatIdCache[pairKey] = resolved }
                val notificationId = buildNotificationId(
                    sellerUid = sellerUid,
                    sellEntryId = soldEntry.entryId,
                )
                val buyerName = resolveUserDisplayName(
                    uid = buyerUid,
                    idToken = idToken,
                    fallback = buyerUid,
                    cache = displayNameCache,
                )
                upsertMatchArtifacts(
                    idToken = idToken,
                    chatId = chatId,
                    buyerUid = buyerUid,
                    buyerEmail = buyerName,
                    sellerUid = sellerUid,
                    sellerEmail = sellerEmail,
                    cardId = cardId,
                    cardName = cardName,
                )

                dataSource.upsertUserNotification(
                    uid = buyerUid,
                    idToken = idToken,
                    notification = TradeMatchNotification(
                        notificationId = notificationId,
                        chatId = chatId,
                        sellerUid = sellerUid,
                        sellerEmail = sellerEmail,
                        sellerImageUrl = null,
                        cardId = cardId,
                        cardName = cardName,
                        price = soldEntry.price,
                        message = "${cardName} is now in seller's Sell List",
                        isRead = false,
                    ),
                )
            }
        }
    }

    private suspend fun pushMatchNotificationsForNewBuyEntries(
        buyerUid: String,
        buyerEmail: String,
        idToken: String,
        before: List<StoredTradeCardEntry>,
        after: List<StoredTradeCardEntry>,
    ) {
        val previousEntryIds = before.map { it.entryId }.toSet()
        val newlyAddedEntries = after.filter { it.entryId !in previousEntryIds }
        if (newlyAddedEntries.isEmpty()) return

        val sellEntriesByUser = dataSource.listMarketplaceSellEntriesByUser(idToken = idToken)
        val mapPinsByUser = dataSource.listMarketplaceMapPinsByUser(idToken = idToken)
        val requestedKeys = newlyAddedEntries.map { it.marketKey() }.toSet()
        val displayNameCache = mutableMapOf<String, String>()
        val chatIdCache = mutableMapOf<String, String>()
        displayNameCache[buyerUid] = buyerEmail

        sellEntriesByUser.forEach { (sellerUid, sellerEntries) ->
            if (sellerUid == buyerUid) return@forEach
            if (!usersHaveIntersectingPins(sellerUid, buyerUid, mapPinsByUser)) return@forEach
            val sellerKeys = sellerEntries.associateBy { it.marketKey() }

            requestedKeys.forEach { key ->
                val soldEntry = sellerKeys[key] ?: return@forEach
                val requestedEntry = newlyAddedEntries.firstOrNull { it.marketKey() == key } ?: return@forEach
                val cardName = soldEntry.cardName.ifBlank { "Unknown card" }
                val fallbackChatId = buildChatId(
                    firstUid = sellerUid,
                    secondUid = buyerUid,
                    marketKey = key,
                )
                val pairKey = userPairKey(sellerUid, buyerUid)
                val chatId = chatIdCache[pairKey] ?: resolveExistingChatIdForPair(
                    idToken = idToken,
                    firstUid = sellerUid,
                    secondUid = buyerUid,
                    fallbackChatId = fallbackChatId,
                ).also { resolved -> chatIdCache[pairKey] = resolved }
                val notificationId = buildBuyNotificationId(
                    buyerUid = buyerUid,
                    buyEntryId = requestedEntry.entryId,
                )
                val sellerName = resolveUserDisplayName(
                    uid = sellerUid,
                    idToken = idToken,
                    fallback = sellerUid,
                    cache = displayNameCache,
                )
                upsertMatchArtifacts(
                    idToken = idToken,
                    chatId = chatId,
                    buyerUid = buyerUid,
                    buyerEmail = buyerEmail,
                    sellerUid = sellerUid,
                    sellerEmail = sellerName,
                    cardId = soldEntry.cardId,
                    cardName = cardName,
                )

                dataSource.upsertUserNotification(
                    uid = sellerUid,
                    idToken = idToken,
                    notification = TradeMatchNotification(
                        notificationId = notificationId,
                        chatId = chatId,
                        sellerUid = buyerUid,
                        sellerEmail = buyerEmail,
                        sellerImageUrl = null,
                        cardId = soldEntry.cardId,
                        cardName = cardName,
                        price = requestedEntry.price,
                        message = "${buyerEmail} wants to buy ${cardName}",
                        isRead = false,
                    ),
                )
            }
        }
    }

    private suspend fun upsertMatchArtifacts(
        idToken: String,
        chatId: String,
        buyerUid: String,
        buyerEmail: String?,
        sellerUid: String,
        sellerEmail: String,
        cardId: String,
        cardName: String,
    ) {
        val buyerMail = buyerEmail?.trim().orEmpty()
        dataSource.upsertChatRoom(
            idToken = idToken,
            room = TradeChatRoom(
                chatId = chatId,
                buyerUid = buyerUid,
                buyerEmail = buyerMail,
                sellerUid = sellerUid,
                sellerEmail = sellerEmail,
                cardId = cardId,
                cardName = cardName,
                createdAt = nowMillis(),
            ),
        )
        dataSource.upsertUserMatch(
            uid = buyerUid,
            idToken = idToken,
            match = TradeUserMatch(
                chatId = chatId,
                counterpartUid = sellerUid,
                counterpartEmail = sellerEmail,
                cardId = cardId,
                cardName = cardName,
                role = "buyer",
                updatedAt = nowMillis(),
            ),
        )
        dataSource.upsertUserMatch(
            uid = sellerUid,
            idToken = idToken,
            match = TradeUserMatch(
                chatId = chatId,
                counterpartUid = buyerUid,
                counterpartEmail = buyerMail,
                cardId = cardId,
                cardName = cardName,
                role = "seller",
                updatedAt = nowMillis(),
            ),
        )
    }

    private suspend fun resolveUserDisplayName(
        uid: String,
        idToken: String,
        fallback: String,
        cache: MutableMap<String, String>,
    ): String {
        cache[uid]?.let { return it }
        val nickname = dataSource.loadUserNickname(uid = uid, idToken = idToken)
        val resolved = nickname?.trim().orEmpty().ifBlank { fallback }
        cache[uid] = resolved
        return resolved
    }

    private suspend fun resolveExistingChatIdForPair(
        idToken: String,
        firstUid: String,
        secondUid: String,
        fallbackChatId: String,
    ): String {
        val firstMatches = runCatching {
            dataSource.listUserMatches(uid = firstUid, idToken = idToken)
        }.getOrDefault(emptyList())
        val existing = firstMatches
            .filter { it.counterpartUid == secondUid && it.chatId.isNotBlank() }
            .maxByOrNull { it.updatedAt }
            ?.chatId
        return existing ?: fallbackChatId
    }

}

private fun usersHaveIntersectingPins(
    firstUid: String,
    secondUid: String,
    pinsByUser: Map<String, List<StoredMapPin>>,
): Boolean {
    val firstPins = pinsByUser[firstUid].orEmpty()
    val secondPins = pinsByUser[secondUid].orEmpty()
    if (firstPins.isEmpty() || secondPins.isEmpty()) return true

    return firstPins.any { first ->
        secondPins.any { second ->
            val distanceMeters = haversineMeters(
                lat1 = first.latitude,
                lon1 = first.longitude,
                lat2 = second.latitude,
                lon2 = second.longitude,
            )
            distanceMeters <= first.radiusMeters + second.radiusMeters
        }
    }
}

private fun usersHaveIntersectingPinsStrict(
    viewerPins: List<StoredMapPin>,
    sellerPins: List<StoredMapPin>,
): Boolean {
    if (viewerPins.isEmpty() || sellerPins.isEmpty()) return false
    return viewerPins.any { viewerPin ->
        sellerPins.any { sellerPin ->
            val distanceMeters = haversineMeters(
                lat1 = viewerPin.latitude,
                lon1 = viewerPin.longitude,
                lat2 = sellerPin.latitude,
                lon2 = sellerPin.longitude,
            )
            distanceMeters <= viewerPin.radiusMeters + sellerPin.radiusMeters
        }
    }
}

private fun haversineMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
): Float {
    val earthRadius = 6_371_000.0
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0
    val a = kotlin.math.sin(dLat / 2).pow(2.0) +
        kotlin.math.cos(lat1 * PI / 180.0) *
        kotlin.math.cos(lat2 * PI / 180.0) *
        kotlin.math.sin(dLon / 2).pow(2.0)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return (earthRadius * c).toFloat()
}

private fun StoredTradeCardEntry.marketKey(): String {
    return if (cardId.isNotBlank()) {
        "id:${cardId.lowercase()}"
    } else {
        "name:${cardName.trim().lowercase()}"
    }
}

private fun MarketPlaceCard.marketKey(): String {
    return if (cardId.isNotBlank()) {
        "id:${cardId.lowercase()}"
    } else {
        "name:${cardName.trim().lowercase()}"
    }
}

private data class VisibleMarketScope(
    val allowedSellerUids: Set<String> = emptySet(),
    val visibleCardKeys: Set<String> = emptySet(),
    val availableSellerMarketKeys: Set<String> = emptySet(),
)

private fun marketKeyOf(cardId: String, cardName: String): String {
    return if (cardId.isNotBlank()) {
        "id:${cardId.trim().lowercase()}"
    } else {
        "name:${cardName.trim().lowercase()}"
    }
}

private fun buildNotificationId(sellerUid: String, sellEntryId: String): String {
    val safeEntryId = sellEntryId
        .lowercase()
        .replace(":", "_")
        .replace("/", "_")
        .replace(".", "_")
    return "sell_match_${sellerUid}_$safeEntryId"
}

private fun buildBuyNotificationId(buyerUid: String, buyEntryId: String): String {
    val safeEntryId = buyEntryId
        .lowercase()
        .replace(":", "_")
        .replace("/", "_")
        .replace(".", "_")
    return "buy_match_${buyerUid}_$safeEntryId"
}

private fun buildChatId(firstUid: String, secondUid: String, @Suppress("UNUSED_PARAMETER") marketKey: String): String {
    val first = firstUid.lowercase().replace("[^a-z0-9_]".toRegex(), "_")
    val second = secondUid.lowercase().replace("[^a-z0-9_]".toRegex(), "_")
    val normalizedPair = listOf(first, second).sorted()
    return "chat_${normalizedPair[0]}_${normalizedPair[1]}"
}

private fun userPairKey(firstUid: String, secondUid: String): String {
    return listOf(firstUid, secondUid).sorted().joinToString("|")
}

private fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
