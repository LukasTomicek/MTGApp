package mtg.app.feature.settings.presentation.profile

import mtg.app.feature.settings.domain.obj.CreditsProduct

data class ProfileScreenState(
    // Static copy
    val title: String = "Profile",
    val subtitle: String = "Update your nickname visible to other users.",

    // Loaded profile
    val nickname: String = "",
    val credits: Int = 0,
    val creditProducts: List<CreditsProduct> = CreditsProduct.defaultPackages,

    // Validation + status
    val nicknameError: String? = null,
    val creditsError: String? = null,
    val infoMessage: String = "",
    val reviewsError: String? = null,

    // User reviews
    val reviews: List<ProfileReviewItem> = emptyList(),

    // Change nickname modal
    val isChangeNicknameModalVisible: Boolean = false,
    val nicknameDraftInput: String = "",

    // Change password modal
    val isChangePasswordModalVisible: Boolean = false,
    val currentPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val passwordError: String? = null,
)

data class ProfileReviewItem(
    val score: Int,
    val comment: String,
    val createdAt: Long,
)
