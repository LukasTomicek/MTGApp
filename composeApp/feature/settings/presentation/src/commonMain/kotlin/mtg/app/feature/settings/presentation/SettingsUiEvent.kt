package mtg.app.feature.settings.presentation

import mtg.app.core.presentation.Event

sealed interface SettingsUiEvent : Event {
    data object LogoutClicked : SettingsUiEvent
    data object DeleteAccountClicked : SettingsUiEvent
    data object ProfileClicked : SettingsUiEvent
    data object OnboardingClicked : SettingsUiEvent
    data object PrivacyPolicyClicked : SettingsUiEvent
    data object TermsOfUseClicked : SettingsUiEvent
}
