package mtg.app.feature.trade.data

import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.TradeFilter

interface ScryfallDataSource {
    suspend fun searchCards(query: String, filter: TradeFilter): List<MtgCard>
    suspend fun resolveCardsByExactNames(names: Set<String>): Map<String, MtgCard>
    suspend fun searchCardPrints(cardName: String): List<MtgCard>
    suspend fun fetchDefaultCardsBulkUrl(): String?
}
