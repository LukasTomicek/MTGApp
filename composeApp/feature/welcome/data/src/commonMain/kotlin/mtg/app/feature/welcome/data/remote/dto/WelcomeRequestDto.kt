package mtg.app.feature.welcome.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateNicknameRequestDto(
    @SerialName("nickname")
    val nickname: String,
)

@Serializable
data class UpdateOnboardingRequestDto(
    @SerialName("completed")
    val completed: Boolean,
)

@Serializable
data class NicknameResponseDto(
    @SerialName("nickname")
    val nickname: String? = null,
)

@Serializable
data class OnboardingCompletedResponseDto(
    @SerialName("completed")
    val completed: Boolean = false,
)
