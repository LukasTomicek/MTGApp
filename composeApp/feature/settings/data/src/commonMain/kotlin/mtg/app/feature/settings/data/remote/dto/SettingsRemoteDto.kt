package mtg.app.feature.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwnProfileResponseDto(
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("credits")
    val credits: Int = 0,
)

@Serializable
data class ConfirmCreditsPurchaseRequestDto(
    @SerialName("platform")
    val platform: String,
    @SerialName("productId")
    val productId: String,
    @SerialName("storeTransactionId")
    val storeTransactionId: String,
    @SerialName("purchaseToken")
    val purchaseToken: String? = null,
)

@Serializable
data class WalletBalanceResponseDto(
    @SerialName("credits")
    val credits: Int = 0,
)
