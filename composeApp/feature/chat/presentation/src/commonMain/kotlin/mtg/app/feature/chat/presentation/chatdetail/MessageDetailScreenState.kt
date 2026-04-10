package mtg.app.feature.chat.presentation.chatdetail

import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.DealStatus
import mtg.app.feature.chat.domain.SellerPayoutStatus
import mtg.app.feature.chat.domain.TradeOrderSummary

data class MessageDetailScreenState(
    // Chat context
    val chatId: String = "",
    val currentUserUid: String = "",
    val buyerUid: String = "",
    val sellerUid: String = "",
    val cardName: String = "",
    val counterpartEmail: String = "",
    val counterpartUid: String = "",
    val isCurrentUserBuyer: Boolean = false,
    val dealStatus: DealStatus = DealStatus.OPEN,
    val buyerConfirmed: Boolean = false,
    val sellerConfirmed: Boolean = false,
    val buyerCompleted: Boolean = false,
    val sellerCompleted: Boolean = false,
    val order: TradeOrderSummary? = null,
    val sellerPayoutStatus: SellerPayoutStatus = SellerPayoutStatus(),
    val counterpartRatingAverage: Double = 0.0,
    val counterpartRatingCount: Int = 0,
    val alreadyRatedByCurrentUser: Boolean = false,
    val hasSeenCompletedPrompt: Boolean = false,

    // Message history
    val messages: List<ChatMessage> = emptyList(),

    // Composer input
    val input: String = "",

    // Rating modal
    val isRatingModalVisible: Boolean = false,
    val ratingScoreDraft: Int = 5,
    val ratingCommentDraft: String = "",
)
