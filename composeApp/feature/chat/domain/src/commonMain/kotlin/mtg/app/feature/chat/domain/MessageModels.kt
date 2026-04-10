package mtg.app.feature.chat.domain

enum class DealStatus {
    OPEN,
    PROPOSED,
    COMPLETED,
    CANCELED,
}

enum class TradePaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED,
}

enum class TradePayoutStatus {
    NOT_READY,
    PAID_OUT,
    FAILED,
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

data class TradeOrderSummary(
    val id: String,
    val chatId: String = "",
    val cardId: String = "",
    val cardName: String = "",
    val buyerUserId: String = "",
    val sellerUserId: String = "",
    val amountMinor: Long,
    val currency: String,
    val platformFeeMinor: Long,
    val sellerAmountMinor: Long,
    val paymentStatus: TradePaymentStatus,
    val payoutStatus: TradePayoutStatus,
    val paidAt: Long? = null,
    val paidOutAt: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

data class SellerPayoutStatus(
    val accountId: String? = null,
    val detailsSubmitted: Boolean = false,
    val chargesEnabled: Boolean = false,
    val payoutsEnabled: Boolean = false,
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
