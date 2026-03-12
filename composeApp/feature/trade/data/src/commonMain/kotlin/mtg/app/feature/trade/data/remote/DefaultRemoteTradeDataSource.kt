package mtg.app.feature.trade.data.remote

import mtg.app.core.data.remote.ApiCallHandler
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.trade.data.TradeDataSource
import mtg.app.feature.trade.data.toRealtimeEntryJson
import mtg.app.feature.trade.data.remote.dto.EnsureChatRequestDto
import mtg.app.feature.trade.data.remote.dto.EnsureChatResponseDto
import mtg.app.feature.trade.data.remote.dto.CollectionEntryResponseDto
import mtg.app.feature.trade.data.remote.dto.MarketCardResponseDto
import mtg.app.feature.trade.data.remote.dto.MarketSellerResponseDto
import mtg.app.feature.trade.data.remote.dto.OfferResponseDto
import mtg.app.feature.trade.data.remote.dto.SyncMatchesRequestDto
import mtg.app.feature.trade.data.remote.dto.SyncOffersRequestDto
import mtg.app.feature.trade.data.remote.dto.toMarketPlaceCards
import mtg.app.feature.trade.data.remote.dto.toMarketPlaceSellers
import mtg.app.feature.trade.data.remote.dto.toReplaceMapPinsRequestDto
import mtg.app.feature.trade.data.remote.dto.toStoredMapPins
import mtg.app.feature.trade.data.remote.dto.toStoredTradeEntries
import mtg.app.feature.trade.data.remote.dto.toSyncOfferEntryDto
import mtg.app.feature.trade.data.remote.dto.toUpsertOfferRequestDto
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.obj.EnsureMarketPlaceChatRequest
import mtg.app.feature.trade.domain.obj.MarketPlaceCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceRecentCardsQuery
import mtg.app.feature.trade.domain.obj.MarketPlaceSellersQuery
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod

class DefaultRemoteTradeDataSource(
    private val apiCallHandler: ApiCallHandler,
) : TradeDataSource {
    override suspend fun ensureMarketPlaceChat(
        context: AuthContext,
        request: EnsureMarketPlaceChatRequest,
    ): String {
        println("TradeBE: calling /v1/chats/ensure buyer=${request.buyerUid} seller=${request.sellerUid}")
        val response = apiCallHandler.apiRequest<EnsureChatResponseDto>(
            path = "/v1/chats/ensure",
            method = HttpMethod.Post,
            idToken = context.idToken,
            body = EnsureChatRequestDto(
                buyerUid = request.buyerUid,
                buyerEmail = request.buyerEmail,
                sellerUid = request.sellerUid,
                sellerEmail = request.sellerEmail,
                cardId = request.cardId.ifBlank { request.cardName },
                cardName = request.cardName.ifBlank { "Unknown card" },
            ),
        )
        return response.chatId.trim()
    }

    override suspend fun listEntries(
        context: AuthContext,
        listType: TradeListType
    ): List<StoredTradeCardEntry> {
        return when (listType) {
            TradeListType.COLLECTION -> {
                println("TradeBE: calling /v1/users/me/collection")
                apiCallHandler.apiRequest<Map<String, CollectionEntryResponseDto>>(
                    path = "/v1/users/me/collection",
                    idToken = context.idToken,
                ).toStoredTradeEntries()
            }
            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> listOfferEntries(context = context, listType = listType)
        }
    }

    override suspend fun listOfferEntries(context: AuthContext, listType: TradeListType): List<StoredTradeCardEntry> {
        val offerType = offerTypeFor(listType) ?: return emptyList()
        println("TradeBE: calling /v1/offers/me type=$offerType")
        val offers = loadOffersArray(idToken = context.idToken, offerType = offerType)
        val parsed = offers.toStoredTradeEntries()
        println("TradeBE: /v1/offers/me success type=$offerType count=${parsed.size}")
        return parsed
    }

    override suspend fun upsertEntry(context: AuthContext, listType: TradeListType, entry: StoredTradeCardEntry) {
        when (listType) {
            TradeListType.COLLECTION -> {
                println("TradeBE: calling PUT /v1/users/me/collection/${entry.entryId}")
                apiCallHandler.apiRequest<Unit>(
                    path = "/v1/users/me/collection/${entry.entryId}",
                    method = HttpMethod.Put,
                    idToken = context.idToken,
                    body = entry.toRealtimeEntryJson(),
                )
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                val offerType = offerTypeFor(listType) ?: return
                val cardName = entry.cardName.trim()
                if (cardName.isBlank()) return
                val cardId = entry.cardId.trim().ifBlank { cardName }
                println("TradeBE: calling POST /v1/offers type=$offerType")
                apiCallHandler.apiRequest<Unit>(
                    path = "/v1/offers",
                    method = HttpMethod.Post,
                    idToken = context.idToken,
                    body = entry.copy(cardId = cardId, cardName = cardName).toUpsertOfferRequestDto(type = offerType),
                )
            }
        }
    }

    override suspend fun deleteEntry(context: AuthContext, listType: TradeListType, entryId: String) {
        when (listType) {
            TradeListType.COLLECTION -> {
                println("TradeBE: calling DELETE /v1/users/me/collection/$entryId")
                apiCallHandler.apiRequest<Unit>(
                    path = "/v1/users/me/collection/$entryId",
                    method = HttpMethod.Delete,
                    idToken = context.idToken,
                )
            }

            TradeListType.BUY_LIST,
            TradeListType.SELL_LIST,
            -> {
                if (entryId.isBlank()) return
                println("TradeBE: calling DELETE /v1/offers/$entryId")
                apiCallHandler.apiRequest<Unit>(
                    path = "/v1/offers/$entryId",
                    method = HttpMethod.Delete,
                    idToken = context.idToken,
                )
            }
        }
    }

    override suspend fun syncOfferEntries(context: AuthContext, listType: TradeListType, entries: List<StoredTradeCardEntry>) {
        val offerType = offerTypeFor(listType) ?: return
        println("TradeBE: calling PUT /v1/offers/me sync type=$offerType incoming=${entries.size}")
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/offers/me",
            method = HttpMethod.Put,
            idToken = context.idToken,
            body = SyncOffersRequestDto(
                type = offerType,
                entries = entries.map { it.toSyncOfferEntryDto() },
            ),
        )
    }

    override suspend fun syncMatchNotifications(context: AuthContext, listType: TradeListType?) {
        val syncType = when (listType) {
            TradeListType.BUY_LIST -> "BUY"
            TradeListType.SELL_LIST -> "SELL"
            else -> "ALL"
        }
        println("TradeBE: calling POST /v1/matches/sync type=$syncType")
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/matches/sync",
            method = HttpMethod.Post,
            idToken = context.idToken,
            body = SyncMatchesRequestDto(type = syncType),
        )
    }

    override suspend fun listMapPins(context: AuthContext): List<StoredMapPin> {
        println("TradeBE: calling /v1/users/me/map-pins")
        return apiCallHandler.apiRequest<Map<String, mtg.app.feature.trade.data.remote.dto.MapPinDto>>(
            path = "/v1/users/me/map-pins",
            idToken = context.idToken,
        ).toStoredMapPins()
    }

    override suspend fun replaceMapPins(context: AuthContext, pins: List<StoredMapPin>) {
        println("TradeBE: calling PUT /v1/users/me/map-pins count=${pins.size}")
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/users/me/map-pins",
            method = HttpMethod.Put,
            idToken = context.idToken,
            body = pins.toReplaceMapPinsRequestDto(),
        )
    }

    override suspend fun searchMarketPlaceCards(context: AuthContext, query: MarketPlaceCardsQuery): List<MarketPlaceCard> {
        println("TradeBE: calling /v1/market/cards query=${query.query.trim()} type=${query.offerType} viewer=${context.uid}")
        val response = apiCallHandler.apiRequest<List<MarketCardResponseDto>>(
            path = "/v1/market/cards",
            idToken = context.idToken,
        ) {
            query.query.trim().takeIf { it.isNotBlank() }?.let { parameter("query", it) }
            parameter("type", query.offerType.name)
        }
        val parsed = response.toMarketPlaceCards()
        println("TradeBE: /v1/market/cards success viewer=${context.uid} type=${query.offerType} count=${parsed.size}")
        return parsed
    }

    override suspend fun loadRecentMarketPlaceCards(
        context: AuthContext,
        query: MarketPlaceRecentCardsQuery,
    ): List<MarketPlaceCard> {
        println("TradeBE: calling /v1/market/cards recent type=${query.offerType} viewer=${context.uid} limit=${query.limit}")
        val response = apiCallHandler.apiRequest<List<MarketCardResponseDto>>(
            path = "/v1/market/cards",
            idToken = context.idToken,
        ) {
            parameter("type", query.offerType.name)
            parameter("limit", query.limit.coerceAtLeast(0))
        }
        val parsed = response.toMarketPlaceCards()
        println("TradeBE: /v1/market/cards recent success viewer=${context.uid} type=${query.offerType} count=${parsed.size}")
        return parsed
    }

    override suspend fun loadMarketPlaceSellers(
        context: AuthContext,
        query: MarketPlaceSellersQuery,
    ): List<MarketPlaceSeller> {
        println("TradeBE: calling /v1/market/sellers cardId=${query.cardId.trim()} cardName=${query.cardName.trim()} viewer=${context.uid}")
        val response = apiCallHandler.apiRequest<List<MarketSellerResponseDto>>(
            path = "/v1/market/sellers",
            idToken = context.idToken,
        ) {
            query.cardId.trim().takeIf { it.isNotBlank() }?.let { parameter("cardId", it) }
            query.cardName.trim().takeIf { it.isNotBlank() }?.let { parameter("cardName", it) }
        }
        val parsed = response.toMarketPlaceSellers()
        println("TradeBE: /v1/market/sellers success viewer=${context.uid} count=${parsed.size}")
        return parsed
    }

    private suspend fun loadOffersArray(idToken: String, offerType: String): List<OfferResponseDto> {
        return apiCallHandler.apiRequest(
            path = "/v1/offers/me",
            idToken = idToken,
        ) {
            parameter("type", offerType)
        }
    }

    private fun offerTypeFor(listType: TradeListType): String? {
        return when (listType) {
            TradeListType.BUY_LIST -> "BUY"
            TradeListType.SELL_LIST -> "SELL"
            TradeListType.COLLECTION -> null
        }
    }
}
