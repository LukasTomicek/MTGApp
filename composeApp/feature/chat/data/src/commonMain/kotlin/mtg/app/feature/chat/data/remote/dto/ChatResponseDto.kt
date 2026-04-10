package mtg.app.feature.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.DealStatus
import mtg.app.feature.chat.domain.MessageThread
import mtg.app.feature.chat.domain.SellerPayoutStatus
import mtg.app.feature.chat.domain.TradeOrderSummary
import mtg.app.feature.chat.domain.TradePaymentStatus
import mtg.app.feature.chat.domain.TradePayoutStatus
import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer
import mtg.app.feature.chat.domain.obj.DeleteChatThreadRequest

@Serializable
data class NicknameResponseDto(
    @SerialName("nickname")
    val nickname: String? = null,
)

@Serializable
data class UserMatchThreadDto(
    @SerialName("chatId")
    val chatId: String? = null,
    @SerialName("counterpartUid")
    val counterpartUid: String = "",
    @SerialName("counterpartEmail")
    val counterpartEmail: String = "",
    @SerialName("counterpartNickname")
    val counterpartNickname: String = "",
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("updatedAt")
    val updatedAt: Long = 0L,
)

@Serializable
data class ChatNodeDto(
    @SerialName("meta")
    val meta: ChatMetaDto? = null,
)

@Serializable
data class ChatMetaDto(
    @SerialName("chatId")
    val chatId: String? = null,
    @SerialName("buyerUid")
    val buyerUid: String = "",
    @SerialName("buyerEmail")
    val buyerEmail: String = "",
    @SerialName("sellerUid")
    val sellerUid: String = "",
    @SerialName("sellerEmail")
    val sellerEmail: String = "",
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String = "",
    @SerialName("createdAt")
    val createdAt: Long = 0L,
    @SerialName("lastMessage")
    val lastMessage: String? = null,
    @SerialName("lastMessageAt")
    val lastMessageAt: Long? = null,
    @SerialName("dealStatus")
    val dealStatus: String? = null,
    @SerialName("buyerConfirmed")
    val buyerConfirmed: Boolean = false,
    @SerialName("sellerConfirmed")
    val sellerConfirmed: Boolean = false,
    @SerialName("buyerCompleted")
    val buyerCompleted: Boolean = false,
    @SerialName("sellerCompleted")
    val sellerCompleted: Boolean = false,
    @SerialName("closedAt")
    val closedAt: Long? = null,
)

@Serializable
data class ChatMessageDto(
    @SerialName("messageId")
    val messageId: String? = null,
    @SerialName("senderUid")
    val senderUid: String = "",
    @SerialName("senderEmail")
    val senderEmail: String = "",
    @SerialName("text")
    val text: String? = null,
    @SerialName("createdAt")
    val createdAt: Long = 0L,
)

@Serializable
data class TradeOrderResponseDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("chatId")
    val chatId: String = "",
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String = "",
    @SerialName("buyerUserId")
    val buyerUserId: String = "",
    @SerialName("sellerUserId")
    val sellerUserId: String = "",
    @SerialName("amountMinor")
    val amountMinor: Long = 0L,
    @SerialName("currency")
    val currency: String = "",
    @SerialName("platformFeeMinor")
    val platformFeeMinor: Long = 0L,
    @SerialName("sellerAmountMinor")
    val sellerAmountMinor: Long = 0L,
    @SerialName("paymentStatus")
    val paymentStatus: String = "PENDING",
    @SerialName("payoutStatus")
    val payoutStatus: String = "NOT_READY",
    @SerialName("paidAt")
    val paidAt: Long? = null,
    @SerialName("paidOutAt")
    val paidOutAt: Long? = null,
    @SerialName("createdAt")
    val createdAt: Long = 0L,
    @SerialName("updatedAt")
    val updatedAt: Long = 0L,
)

@Serializable
data class SellerPayoutStatusResponseDto(
    @SerialName("accountId")
    val accountId: String? = null,
    @SerialName("detailsSubmitted")
    val detailsSubmitted: Boolean = false,
    @SerialName("chargesEnabled")
    val chargesEnabled: Boolean = false,
    @SerialName("payoutsEnabled")
    val payoutsEnabled: Boolean = false,
)

@Serializable
data class ExternalLinkResponseDto(
    @SerialName("url")
    val url: String = "",
)

@Serializable
data class RatingExistsResponseDto(
    @SerialName("exists")
    val exists: Boolean = false,
)

@Serializable
data class UserReviewDto(
    @SerialName("score")
    val score: Int? = null,
    @SerialName("comment")
    val comment: String = "",
    @SerialName("createdAt")
    val createdAt: Long = 0L,
    @SerialName("raterUid")
    val raterUid: String = "",
)

@Serializable
data class UserSellOfferDto(
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String = "",
    @SerialName("cardTypeLine")
    val cardTypeLine: String? = null,
    @SerialName("artImageUrl")
    val artImageUrl: String? = null,
    @SerialName("cardImageUrl")
    val cardImageUrl: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("price")
    val price: Double? = null,
)

internal fun Map<String, UserMatchThreadDto>.toMessageThreads(): List<MessageThread> {
    return entries.map { (chatId, dto) ->
        MessageThread(
            chatId = dto.chatId?.trim().takeUnless { it.isNullOrBlank() } ?: chatId,
            counterpartUid = dto.counterpartUid.trim(),
            counterpartEmail = dto.counterpartEmail.trim(),
            counterpartNickname = dto.counterpartNickname.trim(),
            cardId = dto.cardId.trim(),
            cardName = dto.cardName.trim(),
            role = dto.role.trim(),
            lastMessage = null,
            lastMessageAt = dto.updatedAt,
        )
    }
}

internal fun ChatMetaDto.toDomain(fallbackChatId: String): ChatMeta {
    return ChatMeta(
        chatId = chatId?.trim().takeUnless { it.isNullOrBlank() } ?: fallbackChatId,
        buyerUid = buyerUid.trim(),
        buyerEmail = buyerEmail.trim(),
        sellerUid = sellerUid.trim(),
        sellerEmail = sellerEmail.trim(),
        cardId = cardId.trim(),
        cardName = cardName.trim(),
        createdAt = createdAt,
        lastMessage = lastMessage?.trim()?.takeUnless { it.isBlank() },
        lastMessageAt = lastMessageAt,
        dealStatus = dealStatus.toDealStatus(),
        buyerConfirmed = buyerConfirmed,
        sellerConfirmed = sellerConfirmed,
        buyerCompleted = buyerCompleted,
        sellerCompleted = sellerCompleted,
    )
}

internal fun TradeOrderResponseDto.toDomain(): TradeOrderSummary {
    return TradeOrderSummary(
        id = id,
        chatId = chatId,
        cardId = cardId,
        cardName = cardName,
        buyerUserId = buyerUserId,
        sellerUserId = sellerUserId,
        amountMinor = amountMinor,
        currency = currency,
        platformFeeMinor = platformFeeMinor,
        sellerAmountMinor = sellerAmountMinor,
        paymentStatus = paymentStatus.toTradePaymentStatus(),
        payoutStatus = payoutStatus.toTradePayoutStatus(),
        paidAt = paidAt,
        paidOutAt = paidOutAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

internal fun SellerPayoutStatusResponseDto.toDomain(): SellerPayoutStatus {
    return SellerPayoutStatus(
        accountId = accountId?.trim()?.takeUnless { it.isBlank() },
        detailsSubmitted = detailsSubmitted,
        chargesEnabled = chargesEnabled,
        payoutsEnabled = payoutsEnabled,
    )
}

internal fun Map<String, ChatMessageDto>.toChatMessages(): List<ChatMessage> {
    return entries.mapNotNull { (messageId, dto) ->
        val text = dto.text?.trim().takeUnless { it.isNullOrBlank() } ?: return@mapNotNull null
        ChatMessage(
            id = dto.messageId?.trim().takeUnless { it.isNullOrBlank() } ?: messageId,
            senderUid = dto.senderUid.trim(),
            senderEmail = dto.senderEmail.trim(),
            text = text,
            createdAt = dto.createdAt,
        )
    }.sortedBy { it.createdAt }
}

internal fun Map<String, UserReviewDto>.toUserReviews(): List<UserReview> {
    return values.mapNotNull { dto ->
        val score = dto.score ?: return@mapNotNull null
        UserReview(
            raterUid = dto.raterUid.trim(),
            score = score,
            comment = dto.comment,
            createdAt = dto.createdAt,
        )
    }.sortedByDescending { it.createdAt }
}

internal fun Map<String, UserSellOfferDto>.toUserSellOffers(): List<UserSellOffer> {
    return values.groupBy { dto ->
        val cardId = dto.cardId.trim()
        val cardName = dto.cardName.trim()
        cardId.ifBlank { cardName.lowercase() }
    }.values.mapNotNull { group ->
        val first = group.firstOrNull() ?: return@mapNotNull null
        val cardName = first.cardName.trim()
        if (cardName.isBlank()) return@mapNotNull null
        val cardId = first.cardId.trim().ifBlank { cardName }
        val imageUrl = first.artImageUrl?.trim()?.takeUnless { it.isBlank() }
            ?: first.cardImageUrl?.trim()?.takeUnless { it.isBlank() }
            ?: first.imageUrl?.trim()?.takeUnless { it.isBlank() }
        UserSellOffer(
            cardId = cardId,
            cardName = cardName,
            cardTypeLine = first.cardTypeLine?.trim()?.takeUnless { it.isBlank() } ?: "",
            imageUrl = imageUrl,
            offerCount = group.size,
            fromPrice = group.mapNotNull { it.price }.minOrNull(),
        )
    }.sortedBy { it.cardName.lowercase() }
}

internal fun ChatMetaDto.buildTradeRatingKey(chatId: String): String {
    val resolvedChatId = this.chatId?.trim().takeUnless { it.isNullOrBlank() } ?: chatId
    val resolvedClosedAt = closedAt
    if (resolvedClosedAt != null && resolvedClosedAt > 0L) return "${resolvedChatId}_$resolvedClosedAt"
    if (createdAt > 0L) return "${resolvedChatId}_$createdAt"
    return resolvedChatId
}

private fun String?.toDealStatus(): DealStatus {
    return when (this?.trim()?.uppercase()) {
        "PROPOSED" -> DealStatus.PROPOSED
        "COMPLETED" -> DealStatus.COMPLETED
        "CANCELED" -> DealStatus.CANCELED
        else -> DealStatus.OPEN
    }
}

private fun String?.toTradePaymentStatus(): TradePaymentStatus {
    return when (this?.trim()?.uppercase()) {
        "PAID" -> TradePaymentStatus.PAID
        "FAILED" -> TradePaymentStatus.FAILED
        "REFUNDED" -> TradePaymentStatus.REFUNDED
        else -> TradePaymentStatus.PENDING
    }
}

private fun String?.toTradePayoutStatus(): TradePayoutStatus {
    return when (this?.trim()?.uppercase()) {
        "PAID_OUT" -> TradePayoutStatus.PAID_OUT
        "FAILED" -> TradePayoutStatus.FAILED
        else -> TradePayoutStatus.NOT_READY
    }
}
