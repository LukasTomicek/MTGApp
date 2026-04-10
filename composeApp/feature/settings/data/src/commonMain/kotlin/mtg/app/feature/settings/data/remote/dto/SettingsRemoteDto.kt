package mtg.app.feature.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwnProfileResponseDto(
    @SerialName("nickname")
    val nickname: String? = null,
    @SerialName("balanceMinor")
    val balanceMinor: Long = 0L,
)
