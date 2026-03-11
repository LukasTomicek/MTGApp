package mtg.app.feature.welcome.presentation.setupprofile

data class SetupProfileScreenState(
    // Static screen copy
    val title: String = "Setup profile",
    val subtitle: String = "Choose a nickname that other players will see when trading, chatting, and receiving notifications.",

    // Form input
    val nickname: String = "",

    // Validation + submit status
    val nicknameError: String? = null,
    val isSavingNickname: Boolean = false,
)
