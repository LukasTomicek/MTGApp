package mtg.app.feature.trade.domain.obj

import mtg.app.feature.trade.domain.MarketPlaceOfferType

data class MarketPlaceRecentCardsQuery(
    val limit: Int,
    val offerType: MarketPlaceOfferType,
)
