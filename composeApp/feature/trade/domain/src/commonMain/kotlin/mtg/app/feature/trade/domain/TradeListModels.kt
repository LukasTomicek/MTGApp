package mtg.app.feature.trade.domain

enum class TradeListType(val collectionPath: String) {
    COLLECTION("collection"),
    BUY_LIST("buyList"),
    SELL_LIST("sellList"),
}

data class StoredTradeCardEntry(
    val entryId: String,
    val cardId: String,
    val cardName: String,
    val cardTypeLine: String,
    val cardImageUrl: String?,
    val cardArtDescriptor: String?,
    val quantity: Int,
    val foil: String,
    val language: String,
    val condition: String,
    val price: Double?,
    val artLabel: String,
    val artImageUrl: String?,
)

data class MarketPlaceCard(
    val cardId: String,
    val cardName: String,
    val cardTypeLine: String,
    val imageUrl: String?,
    val offerCount: Int,
    val fromPrice: Double?,
)

enum class MarketPlaceOfferType {
    SELL,
    BUY,
}

data class MarketPlaceSeller(
    val uid: String,
    val displayName: String,
    val offerCount: Int,
    val fromPrice: Double?,
)

data class StoredMapPin(
    val pinId: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
)

data class TradeMatchNotification(
    val notificationId: String,
    val chatId: String,
    val sellerUid: String,
    val sellerEmail: String,
    val sellerImageUrl: String?,
    val cardId: String,
    val cardName: String,
    val price: Double?,
    val message: String?,
    val isRead: Boolean,
    val type: String = "card_match",
)

data class TradeUserMatch(
    val chatId: String,
    val counterpartUid: String,
    val counterpartEmail: String,
    val cardId: String,
    val cardName: String,
    val role: String,
    val updatedAt: Long,
)

data class TradeChatRoom(
    val chatId: String,
    val buyerUid: String,
    val buyerEmail: String,
    val sellerUid: String,
    val sellerEmail: String,
    val cardId: String,
    val cardName: String,
    val createdAt: Long,
)
