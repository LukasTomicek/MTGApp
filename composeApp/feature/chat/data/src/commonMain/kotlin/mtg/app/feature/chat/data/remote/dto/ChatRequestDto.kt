package mtg.app.feature.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequestDto(
    @SerialName("senderDisplayName")
    val senderDisplayName: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class PatchChatDealRequestDto(
    @SerialName("dealStatus")
    val dealStatus: String,
    @SerialName("buyerConfirmed")
    val buyerConfirmed: Boolean,
    @SerialName("sellerConfirmed")
    val sellerConfirmed: Boolean,
    @SerialName("buyerCompleted")
    val buyerCompleted: Boolean,
    @SerialName("sellerCompleted")
    val sellerCompleted: Boolean,
    @SerialName("closed")
    val closed: Boolean? = null,
    @SerialName("closedAt")
    val closedAt: Long? = null,
    @SerialName("offerState")
    val offerState: String? = null,
)

@Serializable
data class SubmitRatingRequestDto(
    @SerialName("ratedUid")
    val ratedUid: String,
    @SerialName("score")
    val score: Int,
    @SerialName("comment")
    val comment: String,
)
