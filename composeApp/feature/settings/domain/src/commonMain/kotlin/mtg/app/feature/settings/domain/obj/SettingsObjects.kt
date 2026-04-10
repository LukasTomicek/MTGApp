package mtg.app.feature.settings.domain.obj

data class SettingsProfile(
    val nickname: String,
    val balanceMinor: Long = 0L,
)
