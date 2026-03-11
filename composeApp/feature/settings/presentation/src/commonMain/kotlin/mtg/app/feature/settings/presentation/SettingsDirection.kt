package mtg.app.feature.settings.presentation

import mtg.app.core.presentation.Direction

sealed interface SettingsDirection : Direction {
    data object NavigateToAuth : SettingsDirection
    data object NavigateToProfile : SettingsDirection
    data object NavigateToOnboarding : SettingsDirection
    data object NavigateToPrivacyPolicy : SettingsDirection
    data object NavigateToTermsOfUse : SettingsDirection
}
