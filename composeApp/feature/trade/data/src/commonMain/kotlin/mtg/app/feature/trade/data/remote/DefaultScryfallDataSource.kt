package mtg.app.feature.trade.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import mtg.app.feature.trade.data.ScryfallDataSource
import mtg.app.feature.trade.data.remote.dto.ScryfallCardsResponseDto
import mtg.app.feature.trade.data.remote.dto.ScryfallBulkDataResponseDto
import mtg.app.feature.trade.data.remote.dto.ScryfallCollectionRequestDto
import mtg.app.feature.trade.data.remote.dto.ScryfallNameIdentifierDto
import mtg.app.feature.trade.data.remote.dto.toCards
import mtg.app.feature.trade.data.remote.dto.toDefaultCardsBulkUrl
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.TradeFilter

class DefaultScryfallDataSource(
    private val httpClient: HttpClient,
) : ScryfallDataSource {

    override suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard> {
        val searchQuery = buildSearchQuery(query = query, filter = filter)
        return httpClient.get("https://api.scryfall.com/cards/search") {
            parameter("q", searchQuery)
            parameter("order", "name")
            parameter("unique", "cards")
        }.body<ScryfallCardsResponseDto>().toCards()
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
            val payload = ScryfallCollectionRequestDto(
                identifiers = batch.map { name -> ScryfallNameIdentifierDto(name = name) },
            )
            val cards = httpClient.post("https://api.scryfall.com/cards/collection") {
                setBody(payload)
            }.body<ScryfallCardsResponseDto>().toCards()
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

    override suspend fun searchCardPrints(cardName: String): List<MtgCard> {
        val safeName = cardName.trim().replace("\"", "\\\"")
        if (safeName.isBlank()) return emptyList()
        return httpClient.get("https://api.scryfall.com/cards/search") {
            parameter("q", "!\"$safeName\" game:paper")
            parameter("order", "released")
            parameter("dir", "desc")
            parameter("unique", "prints")
        }.body<ScryfallCardsResponseDto>().toCards()
    }

    override suspend fun fetchDefaultCardsBulkUrl(): String? {
        return httpClient.get("https://api.scryfall.com/bulk-data").body<ScryfallBulkDataResponseDto>().toDefaultCardsBulkUrl()
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
