package mtg.app.feature.settings.presentation

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService

class SettingsViewModel(
    private val authService: AuthDomainService,
) : BaseViewModel<SettingsScreenState, SettingsUiEvent, SettingsDirection>(
    initialState = SettingsScreenState(),
) {

    init {

    }

    override fun onUiEvent(event: SettingsUiEvent) {
        when (event) {
            SettingsUiEvent.LogoutClicked -> logout()
            SettingsUiEvent.DeleteAccountClicked -> deleteAccount()
            SettingsUiEvent.ProfileClicked -> navigate(SettingsDirection.NavigateToProfile)
            SettingsUiEvent.OnboardingClicked -> navigate(SettingsDirection.NavigateToOnboarding)
            SettingsUiEvent.PrivacyPolicyClicked -> navigate(SettingsDirection.NavigateToPrivacyPolicy)
            SettingsUiEvent.TermsOfUseClicked -> navigate(SettingsDirection.NavigateToTermsOfUse)
        }
    }

    private fun logout() {
        launch {
            setLoading(true)
            setError(null)

            try {
                authService.signOut()
                updateState { it.copy(actionMessage = "Logged out") }
                navigate(SettingsDirection.NavigateToAuth)
            } catch (e: Throwable) {
                setError(e.message ?: "Logout failed")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun deleteAccount() {
        launch {
            setLoading(true)
            setError(null)

            try {
                authService.deleteAccount()
                updateState { it.copy(actionMessage = "Account deleted") }
                navigate(SettingsDirection.NavigateToAuth)
            } catch (e: Throwable) {
                setError(e.message ?: "Delete account failed")
            } finally {
                setLoading(false)
            }
        }
    }
}
