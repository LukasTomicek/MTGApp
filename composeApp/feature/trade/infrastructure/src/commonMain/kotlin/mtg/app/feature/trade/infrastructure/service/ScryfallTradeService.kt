package mtg.app.feature.trade.infrastructure.service

import mtg.app.core.domain.config.BackendEnvironment
import mtg.app.feature.trade.data.toRealtimeChatRoomJson
import mtg.app.feature.trade.data.toRealtimeEntryJson
import mtg.app.feature.trade.data.toRealtimeMapPinJson
import mtg.app.feature.trade.data.toRealtimeNotificationJson
import mtg.app.feature.trade.data.toRealtimeUserMatchJson
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeChatRoom
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.TradeMatchNotification
import mtg.app.feature.trade.domain.TradeUserMatch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.time.Clock

class ScryfallTradeService(
    private val httpClient: HttpClient,
) : TradeService {
    private val tradeBackendBaseUrl = BackendEnvironment.primaryBaseUrl

    override suspend fun searchCards(query: String): JsonObject {
        return httpClient.get("https://api.scryfall.com/cards/search") {
            parameter("q", query)
            parameter("order", "name")
            parameter("unique", "cards")
        }.body()
    }

    override suspend fun findCardsByNames(names: List<String>): JsonObject {
        if (names.isEmpty()) return JsonObject(emptyMap())
        val identifiers = JsonArray(
            names.map { name ->
                JsonObject(
                    mapOf("name" to JsonPrimitive(name))
                )
            }
        )
        val payload = JsonObject(mapOf("identifiers" to identifiers))
        return httpClient.post("https://api.scryfall.com/cards/collection") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()
    }

    override suspend fun searchCardPrints(cardName: String): JsonObject {
        val safeName = cardName.trim().replace("\"", "\\\"")
        if (safeName.isBlank()) {
            return JsonObject(emptyMap())
        }

        return httpClient.get("https://api.scryfall.com/cards/search") {
            parameter("q", "!\"$safeName\" game:paper")
            parameter("order", "released")
            parameter("dir", "desc")
            parameter("unique", "prints")
        }.body()
    }

    override suspend fun fetchBulkData(): JsonObject {
        return httpClient.get("https://api.scryfall.com/bulk-data").body()
    }

    override suspend fun listEntries(uid: String, idToken: String, listType: TradeListType): JsonObject {
        return when (listType) {
            TradeListType.COLLECTION -> {
                val user = uid.encodeURLPath()
                println("TradeBE: calling /v1/bridge/users/$uid/collection")
                val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/collection")
                response.requireSuccess("GET /v1/bridge/users/$uid/collection")
                parseJsonObjectResponse(response.bodyAsText())
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                val entries = listEntriesFromBackend(uid = uid, listType = listType)
                JsonObject(entries.associate { it.entryId to it.toRealtimeEntryJson() })
            }
        }
    }

    override suspend fun listEntriesFromBackend(
        uid: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry> {
        val offerType = offerTypeFor(listType) ?: return emptyList()
        println("TradeBE: calling /v1/offers list for uid=$uid, type=$offerType")
        val offers = loadOffersArray(uid = uid, offerType = offerType)
        val parsed = offers.mapNotNull { obj ->
            val offerId = obj.string("id")?.trim().orEmpty()
            val cardId = obj.string("cardId")?.trim().orEmpty()
            val cardName = obj.string("cardName")?.trim().orEmpty()
            if (offerId.isBlank() || cardName.isBlank()) return@mapNotNull null
            val price = obj.string("price")?.toDoubleOrNull()
            val cardImageUrl = obj.string("cardImageUrl")
                ?.trim()
                ?.takeUnless { it.isBlank() }
                ?: obj.string("imageUrl")
                    ?.trim()
                    ?.takeUnless { it.isBlank() }
            val cardTypeLine = obj.string("cardTypeLine")
                ?.trim()
                ?.takeUnless { it.isBlank() }
                ?: obj.string("typeLine")
                    ?.trim()
                    ?.takeUnless { it.isBlank() }
                ?: ""

            StoredTradeCardEntry(
                entryId = offerId,
                cardId = cardId,
                cardName = cardName,
                cardTypeLine = cardTypeLine,
                cardImageUrl = cardImageUrl,
                cardArtDescriptor = null,
                quantity = 1,
                foil = "NON_FOIL",
                language = "EN",
                condition = "NM",
                price = price,
                artLabel = "Default art",
                artImageUrl = null,
            )
        }
        println("TradeBE: /v1/offers list success for uid=$uid, type=$offerType, count=${parsed.size}")
        return parsed
    }

    override suspend fun upsertEntry(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entry: StoredTradeCardEntry,
    ) {
        when (listType) {
            TradeListType.COLLECTION -> {
                val user = uid.encodeURLPath()
                val entryId = entry.entryId.encodeURLPath()
                println("TradeBE: calling PUT /v1/bridge/users/$uid/collection/${entry.entryId}")
                val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/users/$user/collection/$entryId") {
                    contentType(ContentType.Application.Json)
                    setBody(entry.toRealtimeEntryJson())
                }
                response.requireSuccess("PUT /v1/bridge/users/$uid/collection/${entry.entryId}")
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                val offerType = offerTypeFor(listType) ?: return
                val cardName = entry.cardName.trim()
                if (cardName.isBlank()) return
                val cardId = entry.cardId.trim().ifBlank { cardName }
                val payload = buildJsonObject {
                    put("userId", uid)
                    put("cardId", cardId)
                    put("cardName", cardName)
                    put("type", offerType)
                    entry.cardTypeLine
                        .trim()
                        .takeIf { it.isNotBlank() }
                        ?.let { put("cardTypeLine", it) }
                    entry.artImageUrl
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { put("cardImageUrl", it) }
                        ?: entry.cardImageUrl
                            ?.trim()
                            ?.takeIf { it.isNotBlank() }
                            ?.let { put("cardImageUrl", it) }
                    entry.price?.let { put("price", it) }
                }
                println("TradeBE: calling POST /v1/offers for uid=$uid, type=$offerType")
                val response = httpClient.post("$tradeBackendBaseUrl/v1/offers") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
                response.requireSuccess("POST /v1/offers")
            }
        }
    }

    override suspend fun deleteEntry(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entryId: String,
    ) {
        when (listType) {
            TradeListType.COLLECTION -> {
                val user = uid.encodeURLPath()
                val id = entryId.encodeURLPath()
                println("TradeBE: calling DELETE /v1/bridge/users/$uid/collection/$entryId")
                val response = httpClient.delete("$tradeBackendBaseUrl/v1/bridge/users/$user/collection/$id")
                response.requireSuccess("DELETE /v1/bridge/users/$uid/collection/$entryId")
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                if (entryId.isBlank()) return
                println("TradeBE: calling DELETE /v1/offers/$entryId")
                val response = httpClient.delete("$tradeBackendBaseUrl/v1/offers/${entryId.encodeURLPath()}")
                response.requireSuccess("DELETE /v1/offers/$entryId")
            }
        }
    }

    override suspend fun replaceEntriesInBackend(
        uid: String,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
    ) {
        val offerType = offerTypeFor(listType) ?: return
        println("TradeBE: calling /v1/offers replace for uid=$uid, type=$offerType, incoming=${entries.size}")
        val existingArray = loadOffersArray(uid = uid, offerType = offerType)
        println("TradeBE: /v1/offers existing loaded for replace uid=$uid, type=$offerType, existing=${existingArray.size}")
        existingArray.forEach { obj ->
            val id = obj.string("id")?.trim().orEmpty()
            if (id.isBlank()) return@forEach
            val response = httpClient.delete("$tradeBackendBaseUrl/v1/offers/$id")
            response.requireSuccess("DELETE /v1/offers/$id (replace)")
        }

        entries.forEach { entry ->
            val cardName = entry.cardName.trim()
            if (cardName.isBlank()) return@forEach
            val cardId = entry.cardId.trim().ifBlank { cardName }
            val payload = buildJsonObject {
                put("userId", uid)
                put("cardId", cardId)
                put("cardName", cardName)
                put("type", offerType)
                entry.cardTypeLine
                    .trim()
                    .takeIf { it.isNotBlank() }
                    ?.let { put("cardTypeLine", it) }
                entry.artImageUrl
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { put("cardImageUrl", it) }
                    ?: entry.cardImageUrl
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { put("cardImageUrl", it) }
                entry.price?.let { put("price", it) }
            }
            val response = httpClient.post("$tradeBackendBaseUrl/v1/offers") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            response.requireSuccess("POST /v1/offers (replace)")
        }
        println("TradeBE: /v1/offers replace success for uid=$uid, type=$offerType, written=${entries.size}")
    }

    override suspend fun replaceMarketplaceUserSellEntries(
        uid: String,
        idToken: String,
        entries: List<StoredTradeCardEntry>,
    ) {
        println("TradeBE: marketplace sell mirror is derived from /v1/offers (no direct write), uid=$uid count=${entries.size}")
    }

    override suspend fun replaceMarketplaceUserBuyEntries(
        uid: String,
        idToken: String,
        entries: List<StoredTradeCardEntry>,
    ) {
        println("TradeBE: marketplace buy mirror is derived from /v1/offers (no direct write), uid=$uid count=${entries.size}")
    }

    override suspend fun listMarketplaceSellEntriesByUser(idToken: String): JsonObject {
        println("TradeBE: calling /v1/bridge/market/sell-offers")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/market/sell-offers")
        response.requireSuccess("GET /v1/bridge/market/sell-offers")
        return parseJsonObjectResponse(response.bodyAsText())
    }

    override suspend fun listMarketplaceBuyEntries(idToken: String): JsonObject {
        println("TradeBE: calling /v1/bridge/market/buy-offers")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/market/buy-offers")
        response.requireSuccess("GET /v1/bridge/market/buy-offers")
        return parseJsonObjectResponse(response.bodyAsText())
    }

    override suspend fun listChats(idToken: String): JsonObject {
        println("TradeBE: calling /v1/bridge/chats")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/chats")
        response.requireSuccess("GET /v1/bridge/chats")
        return parseJsonObjectResponse(response.bodyAsText())
    }

    override suspend fun upsertUserNotification(
        uid: String,
        idToken: String,
        notification: TradeMatchNotification,
    ) {
        val user = uid.encodeURLPath()
        val notificationId = notification.notificationId.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/notifications/${notification.notificationId}")
        val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/users/$user/notifications/$notificationId") {
            contentType(ContentType.Application.Json)
            setBody(notification.toRealtimeNotificationJson())
        }
        response.requireSuccess("PUT /v1/bridge/users/$uid/notifications/${notification.notificationId}")
    }

    override suspend fun upsertUserMatch(
        uid: String,
        idToken: String,
        match: TradeUserMatch,
    ) {
        val user = uid.encodeURLPath()
        val chatId = match.chatId.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/matches/${match.chatId}")
        val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/users/$user/matches/$chatId") {
            contentType(ContentType.Application.Json)
            setBody(match.toRealtimeUserMatchJson())
        }
        response.requireSuccess("PUT /v1/bridge/users/$uid/matches/${match.chatId}")
    }

    override suspend fun upsertChatRoom(
        idToken: String,
        room: TradeChatRoom,
    ) {
        val chatId = room.chatId.encodeURLPath()
        println("TradeBE: calling /v1/bridge/chats/${room.chatId}/meta")
        val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/chats/$chatId/meta") {
            contentType(ContentType.Application.Json)
            setBody(room.toRealtimeChatRoomJson())
        }
        response.requireSuccess("PUT /v1/bridge/chats/${room.chatId}/meta")
    }

    override suspend fun listUserMatches(uid: String, idToken: String): List<TradeUserMatch> {
        println("TradeBE: calling /v1/matches for uid=$uid at $tradeBackendBaseUrl")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/matches") {
            parameter("userId", uid)
        }
        response.requireSuccess("GET /v1/matches?userId=$uid")
        val matches = parseBackendMatches(rawBody = response.bodyAsText(), uid = uid)
        println("TradeBE: /v1/matches success for uid=$uid, count=${matches.size}")
        return matches
    }

    override suspend fun loadUserNickname(uid: String, idToken: String): String? {
        println("TradeBE: calling /v1/users/profile for uid=$uid")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/profile") {
            parameter("userId", uid)
        }
        if (response.status.isSuccess()) {
            val body = response.bodyAsText().trim()
            val root = runCatching { Json.parseToJsonElement(body) as? JsonObject }.getOrNull()
            root.stringOrNull("nickname")?.let { return it }
        }

        val pathUid = uid.encodeURLPath()
        val pathResponse = httpClient.get("$tradeBackendBaseUrl/v1/users/profile/$pathUid")
        if (!pathResponse.status.isSuccess()) return null
        val pathBody = pathResponse.bodyAsText().trim()
        val pathRoot = runCatching { Json.parseToJsonElement(pathBody) as? JsonObject }.getOrNull() ?: return null
        return pathRoot.stringOrNull("nickname")
    }

    override suspend fun listMapPins(uid: String, idToken: String): JsonObject {
        val user = uid.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/map-pins")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/map-pins")
        response.requireSuccess("GET /v1/bridge/users/$uid/map-pins")
        return parseJsonObjectResponse(response.bodyAsText())
    }

    override suspend fun replaceUserMapPins(uid: String, idToken: String, pins: List<StoredMapPin>) {
        val user = uid.encodeURLPath()
        val payload = JsonObject(
            pins.associate { pin ->
                pin.pinId to pin.toRealtimeMapPinJson()
            }
        )
        println("TradeBE: calling PUT /v1/bridge/users/$uid/map-pins count=${pins.size}")
        val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/users/$user/map-pins") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        response.requireSuccess("PUT /v1/bridge/users/$uid/map-pins")
    }

    override suspend fun replaceMarketplaceMapPins(uid: String, idToken: String, pins: List<StoredMapPin>) {
        println("TradeBE: marketplace map pin mirror is derived from user map-pins (no direct write), uid=$uid count=${pins.size}")
    }

    override suspend fun listMarketplaceMapPins(idToken: String): JsonObject {
        println("TradeBE: calling /v1/bridge/market/map-pins")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/market/map-pins")
        response.requireSuccess("GET /v1/bridge/market/map-pins")
        return parseJsonObjectResponse(response.bodyAsText())
    }

    override suspend fun searchMarketPlaceCardsFromBackend(
        viewerUid: String,
        query: String,
    ): List<MarketPlaceCard> {
        println("TradeBE: calling /v1/market/cards query=${query.trim()} excludeUserId=$viewerUid")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/market/cards") {
            query.trim().takeIf { it.isNotBlank() }?.let { parameter("query", it) }
            parameter("excludeUserId", viewerUid)
        }
        response.requireSuccess("GET /v1/market/cards")
        val raw = response.bodyAsText().trim()
        val root = Json.parseToJsonElement(raw) as? JsonArray ?: return emptyList()
        val parsed = root.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val cardId = obj.string("cardId")?.trim().orEmpty()
            val cardName = obj.string("cardName")?.trim().orEmpty()
            if (cardId.isBlank() || cardName.isBlank()) return@mapNotNull null
            MarketPlaceCard(
                cardId = cardId,
                cardName = cardName,
                cardTypeLine = obj.string("cardTypeLine")
                    ?.trim()
                    ?.takeUnless { it.isBlank() }
                    ?: obj.string("typeLine")
                        ?.trim()
                        ?.takeUnless { it.isBlank() }
                    ?: "",
                imageUrl = obj.string("imageUrl")
                    ?.trim()
                    ?.takeUnless { it.isBlank() }
                    ?: obj.string("cardImageUrl")
                        ?.trim()
                        ?.takeUnless { it.isBlank() },
                offerCount = obj.int("offerCount") ?: 0,
                fromPrice = obj.double("fromPrice"),
            )
        }
        println("TradeBE: /v1/market/cards success excludeUserId=$viewerUid count=${parsed.size}")
        return parsed
    }

    override suspend fun loadRecentMarketPlaceCardsFromBackend(
        viewerUid: String,
        limit: Int,
    ): List<MarketPlaceCard> {
        val result = searchMarketPlaceCardsFromBackend(
            viewerUid = viewerUid,
            query = "",
        ).take(limit.coerceAtLeast(0))
        println("TradeBE: /v1/market/cards recent success excludeUserId=$viewerUid count=${result.size}")
        return result
    }

    override suspend fun loadMarketPlaceSellersFromBackend(
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller> {
        println("TradeBE: calling /v1/market/sellers cardId=${cardId.trim()} cardName=${cardName.trim()} excludeUserId=$viewerUid")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/market/sellers") {
            cardId.trim().takeIf { it.isNotBlank() }?.let { parameter("cardId", it) }
            cardName.trim().takeIf { it.isNotBlank() }?.let { parameter("cardName", it) }
            parameter("excludeUserId", viewerUid)
        }
        response.requireSuccess("GET /v1/market/sellers")
        val raw = response.bodyAsText().trim()
        val root = Json.parseToJsonElement(raw) as? JsonArray ?: return emptyList()
        val parsed = root.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val uid = obj.string("userId")?.trim().orEmpty()
            if (uid.isBlank()) return@mapNotNull null
            val displayName = obj.string("displayName")
                ?.trim()
                .takeUnless { it.isNullOrBlank() }
                ?: uid
            MarketPlaceSeller(
                uid = uid,
                displayName = displayName,
                offerCount = obj.int("offerCount") ?: 0,
                fromPrice = obj.double("fromPrice"),
            )
        }
        println("TradeBE: /v1/market/sellers success excludeUserId=$viewerUid count=${parsed.size}")
        return parsed.sortedBy { it.fromPrice ?: Double.MAX_VALUE }
    }

    private suspend fun loadOffersArray(uid: String, offerType: String): List<JsonObject> {
        val response = httpClient.get("$tradeBackendBaseUrl/v1/offers") {
            parameter("userId", uid)
            parameter("type", offerType)
        }
        response.requireSuccess("GET /v1/offers")
        val body = response.bodyAsText().trim()
        val root = Json.parseToJsonElement(body) as? JsonArray ?: return emptyList()
        return root.mapNotNull { it as? JsonObject }
    }

    private fun parseBackendMatches(rawBody: String, uid: String): List<TradeUserMatch> {
        val root = Json.parseToJsonElement(rawBody.trim()) as? JsonArray ?: return emptyList()
        val now = Clock.System.now().toEpochMilliseconds()

        return root.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val counterpartUid = obj.string("counterpartUserId")?.trim().orEmpty()
            if (counterpartUid.isBlank()) return@mapNotNull null

            val role = when (obj.string("myType")?.uppercase()) {
                "BUY" -> "buyer"
                "SELL" -> "seller"
                else -> ""
            }

            TradeUserMatch(
                chatId = obj.string("chatId")
                    ?.takeIf { it.isNotBlank() }
                    ?: buildPairChatId(uid, counterpartUid),
                counterpartUid = counterpartUid,
                counterpartEmail = obj.string("counterpartDisplayName").orEmpty(),
                cardId = obj.string("cardId").orEmpty(),
                cardName = obj.string("cardName").orEmpty(),
                role = role,
                updatedAt = now,
            )
        }
    }

    private fun parseJsonObjectResponse(rawBody: String): JsonObject {
        val json = rawBody.trim()
        if (json == "null" || json.isBlank()) return JsonObject(emptyMap())
        return Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
    }

    private fun offerTypeFor(listType: TradeListType): String? {
        return when (listType) {
            TradeListType.BUY_LIST -> "BUY"
            TradeListType.SELL_LIST -> "SELL"
            TradeListType.COLLECTION -> null
        }
    }

    private fun buildPairChatId(firstUid: String, secondUid: String): String {
        val normalized = listOf(firstUid, secondUid)
            .map { raw -> raw.lowercase().replace("[^a-z0-9_]".toRegex(), "_") }
            .sorted()
        return "chat_${normalized[0]}_${normalized[1]}"
    }

    private fun JsonObject.string(key: String): String? {
        val primitive = this[key] as? JsonPrimitive ?: return null
        return runCatching { primitive.content }.getOrNull()
    }

    private fun JsonObject.int(key: String): Int? {
        val primitive = this[key] as? JsonPrimitive ?: return null
        return primitive.content.toIntOrNull()
    }

    private fun JsonObject.double(key: String): Double? {
        val primitive = this[key] as? JsonPrimitive ?: return null
        return primitive.content.toDoubleOrNull()
    }

    private fun JsonObject?.stringOrNull(key: String): String? {
        val element = this?.get(key) ?: return null
        if (element is JsonNull) return null
        val primitive = element as? JsonPrimitive ?: return null
        return runCatching { primitive.content }.getOrNull()?.trim()?.takeUnless { it.isBlank() }
    }

    private suspend fun HttpResponse.requireSuccess(operation: String) {
        if (status.isSuccess()) return
        val payload = runCatching { bodyAsText() }.getOrNull().orEmpty()
        error("TradeBE: $operation failed with status=${status.value} body=$payload")
    }
}
