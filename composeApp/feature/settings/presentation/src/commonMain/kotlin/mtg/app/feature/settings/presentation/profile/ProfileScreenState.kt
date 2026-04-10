package mtg.app.feature.settings.presentation.profile

data class ProfileScreenState(
    // Static copy
    val title: String = "Profile",
    val subtitle: String = "Update your nickname visible to other users.",

    // Loaded profile
    val nickname: String = "",
    val balanceMinor: Long = 0L,

    // Validation + status
    val nicknameError: String? = null,
    val infoMessage: String = "",
    val reviewsError: String? = null,
    val orderHistoryError: String? = null,

    // User reviews
    val reviews: List<ProfileReviewItem> = emptyList(),
    val boughtOrders: List<ProfileOrderItem> = emptyList(),
    val soldOrders: List<ProfileOrderItem> = emptyList(),

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

data class ProfileOrderItem(
    val id: String,
    val cardName: String,
    val amountMinor: Long,
    val paymentStatus: String,
)
