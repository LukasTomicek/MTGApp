package mtg.app.feature.trade.domain.obj

import mtg.app.feature.trade.domain.MarketPlaceOfferType

data class MarketPlaceCardsQuery(
    val query: String,
    val offerType: MarketPlaceOfferType,
)
