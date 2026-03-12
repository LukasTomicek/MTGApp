package mtg.app.feature.trade.domain.obj

data class EnsureMarketPlaceChatRequest(
    val buyerUid: String,
    val buyerEmail: String,
    val sellerUid: String,
    val sellerEmail: String,
    val cardId: String,
    val cardName: String,
)
