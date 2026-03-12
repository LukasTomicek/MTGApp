package mtg.app.feature.trade.infrastructure.service

import mtg.app.core.domain.config.BackendEnvironment
import mtg.app.feature.trade.data.toRealtimeEntryJson
import mtg.app.feature.trade.data.toRealtimeMapPinJson
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceOfferType
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeListType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScryfallTradeService(
    private val httpClient: HttpClient,
) : TradeService {
    private val tradeBackendBaseUrl = BackendEnvironment.primaryBaseUrl

    override suspend fun ensureMarketPlaceChat(
        idToken: String,
        buyerUid: String,
        buyerEmail: String,
        sellerUid: String,
        sellerEmail: String,
        cardId: String,
        cardName: String,
    ): String {
        println("TradeBE: calling /v1/chats/ensure buyer=$buyerUid seller=$sellerUid")
        val response = httpClient.post("$tradeBackendBaseUrl/v1/chats/ensure") {
            withBearer(idToken)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("buyerUid", buyerUid)
                    put("buyerEmail", buyerEmail)
                    put("sellerUid", sellerUid)
                    put("sellerEmail", sellerEmail)
                    put("cardId", cardId.ifBlank { cardName })
                    put("cardName", cardName.ifBlank { "Unknown card" })
                }
            )
        }
        response.requireSuccess("POST /v1/chats/ensure")
        val root = parseJsonObjectResponse(response.bodyAsText())
        return root.string("chatId")?.trim().orEmpty()
    }

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
                println("TradeBE: calling /v1/users/me/collection")
                val response = httpClient.get("$tradeBackendBaseUrl/v1/users/me/collection") {
                    withBearer(idToken)
                }
                response.requireSuccess("GET /v1/users/me/collection")
                parseJsonObjectResponse(response.bodyAsText())
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                val entries = listEntriesFromBackend(uid = uid, idToken = idToken, listType = listType)
                JsonObject(entries.associate { it.entryId to it.toRealtimeEntryJson() })
            }
        }
    }

    override suspend fun listEntriesFromBackend(
        uid: String,
        idToken: String,
        listType: TradeListType,
    ): List<StoredTradeCardEntry> {
        val offerType = offerTypeFor(listType) ?: return emptyList()
        println("TradeBE: calling /v1/offers list for uid=$uid, type=$offerType")
        val offers = loadOffersArray(uid = uid, idToken = idToken, offerType = offerType)
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
                val entryId = entry.entryId.encodeURLPath()
                println("TradeBE: calling PUT /v1/users/me/collection/${entry.entryId}")
                val response = httpClient.put("$tradeBackendBaseUrl/v1/users/me/collection/$entryId") {
                    withBearer(idToken)
                    contentType(ContentType.Application.Json)
                    setBody(entry.toRealtimeEntryJson())
                }
                response.requireSuccess("PUT /v1/users/me/collection/${entry.entryId}")
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                val offerType = offerTypeFor(listType) ?: return
                val cardName = entry.cardName.trim()
                if (cardName.isBlank()) return
                val cardId = entry.cardId.trim().ifBlank { cardName }
                val payload = buildJsonObject {
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
                    withBearer(idToken)
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
                val id = entryId.encodeURLPath()
                println("TradeBE: calling DELETE /v1/users/me/collection/$entryId")
                val response = httpClient.delete("$tradeBackendBaseUrl/v1/users/me/collection/$id") {
                    withBearer(idToken)
                }
                response.requireSuccess("DELETE /v1/users/me/collection/$entryId")
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                if (entryId.isBlank()) return
                println("TradeBE: calling DELETE /v1/offers/$entryId")
                val response = httpClient.delete("$tradeBackendBaseUrl/v1/offers/${entryId.encodeURLPath()}") {
                    withBearer(idToken)
                }
                response.requireSuccess("DELETE /v1/offers/$entryId")
            }
        }
    }

    override suspend fun replaceEntriesInBackend(
        uid: String,
        idToken: String,
        listType: TradeListType,
        entries: List<StoredTradeCardEntry>,
    ) {
        val offerType = offerTypeFor(listType) ?: return
        println("TradeBE: calling PUT /v1/offers/me sync for uid=$uid, type=$offerType, incoming=${entries.size}")
        val response = httpClient.put("$tradeBackendBaseUrl/v1/offers/me") {
            withBearer(idToken)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("type", offerType)
                    put(
                        "entries",
                        JsonArray(
                            entries.map { entry ->
                                val cardName = entry.cardName.trim()
                                val cardId = entry.cardId.trim().ifBlank { cardName }
                                buildJsonObject {
                                    put("cardId", cardId)
                                    put("cardName", cardName)
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
                            }
                        )
                    )
                }
            )
        }
        response.requireSuccess("PUT /v1/offers/me")
        println("TradeBE: /v1/offers/me sync success for uid=$uid, type=$offerType, incoming=${entries.size}")
    }

    override suspend fun syncMatchNotifications(
        idToken: String,
        listType: TradeListType?,
    ) {
        val syncType = when (listType) {
            TradeListType.BUY_LIST -> "BUY"
            TradeListType.SELL_LIST -> "SELL"
            else -> "ALL"
        }
        println("TradeBE: calling POST /v1/matches/sync type=$syncType")
        val response = httpClient.post("$tradeBackendBaseUrl/v1/matches/sync") {
            withBearer(idToken)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("type", syncType)
                }
            )
        }
        response.requireSuccess("POST /v1/matches/sync")
    }

    override suspend fun listMapPins(uid: String, idToken: String): JsonObject {
        println("TradeBE: calling /v1/users/me/map-pins")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/me/map-pins") {
            withBearer(idToken)
        }
        response.requireSuccess("GET /v1/users/me/map-pins")
        return parseJsonObjectResponse(response.bodyAsText())
    }

    override suspend fun replaceUserMapPins(uid: String, idToken: String, pins: List<StoredMapPin>) {
        val payload = JsonObject(
            pins.associate { pin ->
                pin.pinId to pin.toRealtimeMapPinJson()
            }
        )
        println("TradeBE: calling PUT /v1/users/me/map-pins count=${pins.size}")
        val response = httpClient.put("$tradeBackendBaseUrl/v1/users/me/map-pins") {
            withBearer(idToken)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        response.requireSuccess("PUT /v1/users/me/map-pins")
    }

    override suspend fun searchMarketPlaceCardsFromBackend(
        idToken: String,
        viewerUid: String,
        query: String,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        println("TradeBE: calling /v1/market/cards query=${query.trim()} type=$offerType viewer=$viewerUid")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/market/cards") {
            withBearer(idToken)
            query.trim().takeIf { it.isNotBlank() }?.let { parameter("query", it) }
            parameter("type", offerType.name)
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
        println("TradeBE: /v1/market/cards success viewer=$viewerUid type=$offerType count=${parsed.size}")
        return parsed
    }

    override suspend fun loadRecentMarketPlaceCardsFromBackend(
        idToken: String,
        viewerUid: String,
        limit: Int,
        offerType: MarketPlaceOfferType,
    ): List<MarketPlaceCard> {
        println("TradeBE: calling /v1/market/cards recent type=$offerType viewer=$viewerUid limit=$limit")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/market/cards") {
            withBearer(idToken)
            parameter("type", offerType.name)
            parameter("limit", limit.coerceAtLeast(0))
        }
        response.requireSuccess("GET /v1/market/cards?limit")
        val raw = response.bodyAsText().trim()
        val root = Json.parseToJsonElement(raw) as? JsonArray ?: return emptyList()
        val result = root.mapNotNull { element ->
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
        println("TradeBE: /v1/market/cards recent success viewer=$viewerUid type=$offerType count=${result.size}")
        return result
    }

    override suspend fun loadMarketPlaceSellersFromBackend(
        idToken: String,
        viewerUid: String,
        cardId: String,
        cardName: String,
    ): List<MarketPlaceSeller> {
        println("TradeBE: calling /v1/market/sellers cardId=${cardId.trim()} cardName=${cardName.trim()} viewer=$viewerUid")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/market/sellers") {
            withBearer(idToken)
            cardId.trim().takeIf { it.isNotBlank() }?.let { parameter("cardId", it) }
            cardName.trim().takeIf { it.isNotBlank() }?.let { parameter("cardName", it) }
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
        println("TradeBE: /v1/market/sellers success viewer=$viewerUid count=${parsed.size}")
        return parsed.sortedBy { it.fromPrice ?: Double.MAX_VALUE }
    }

    private suspend fun loadOffersArray(uid: String, idToken: String, offerType: String): List<JsonObject> {
        val response = httpClient.get("$tradeBackendBaseUrl/v1/offers/me") {
            withBearer(idToken)
            parameter("type", offerType)
        }
        response.requireSuccess("GET /v1/offers/me")
        val body = response.bodyAsText().trim()
        val root = Json.parseToJsonElement(body) as? JsonArray ?: return emptyList()
        return root.mapNotNull { it as? JsonObject }
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

    private fun io.ktor.client.request.HttpRequestBuilder.withBearer(idToken: String) {
        header(HttpHeaders.Authorization, "Bearer $idToken")
    }

    private suspend fun HttpResponse.requireSuccess(operation: String) {
        if (status.isSuccess()) return
        val payload = runCatching { bodyAsText() }.getOrNull().orEmpty()
        error("TradeBE: $operation failed with status=${status.value} body=$payload")
    }
}
