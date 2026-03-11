package mtg.app.feature.chat.domain

enum class DealStatus {
    OPEN,
    PROPOSED,
    COMPLETED,
    CANCELED,
}

data class MessageThread(
    val chatId: String,
    val counterpartUid: String,
    val counterpartEmail: String,
    val counterpartNickname: String,
    val cardId: String,
    val cardName: String,
    val role: String,
    val lastMessage: String?,
    val lastMessageAt: Long,
)

data class ChatMeta(
    val chatId: String,
    val buyerUid: String,
    val buyerEmail: String,
    val sellerUid: String,
    val sellerEmail: String,
    val cardId: String,
    val cardName: String,
    val createdAt: Long,
    val lastMessage: String?,
    val lastMessageAt: Long?,
    val dealStatus: DealStatus,
    val buyerConfirmed: Boolean,
    val sellerConfirmed: Boolean,
    val buyerCompleted: Boolean,
    val sellerCompleted: Boolean,
)

data class ChatMessage(
    val id: String,
    val senderUid: String,
    val senderEmail: String,
    val text: String,
    val createdAt: Long,
)

data class UserRatingSummary(
    val average: Double,
    val count: Int,
)

data class UserReview(
    val raterUid: String,
    val score: Int,
    val comment: String,
    val createdAt: Long,
)

data class UserSellOffer(
    val cardId: String,
    val cardName: String,
    val cardTypeLine: String,
    val imageUrl: String?,
    val offerCount: Int,
    val fromPrice: Double?,
)
