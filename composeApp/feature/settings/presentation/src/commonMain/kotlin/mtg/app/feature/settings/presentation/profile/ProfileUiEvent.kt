package mtg.app.feature.settings.presentation.profile

import mtg.app.core.presentation.Event

sealed interface ProfileUiEvent : Event {
    data object ChangeNicknameClicked : ProfileUiEvent
    data object ChangeNicknameDismissed : ProfileUiEvent
    data class NicknameDraftChanged(val value: String) : ProfileUiEvent
    data object ChangeNicknameConfirmed : ProfileUiEvent
    data object ChangePasswordClicked : ProfileUiEvent
    data object ChangePasswordDismissed : ProfileUiEvent
    data class NewPasswordChanged(val value: String) : ProfileUiEvent
    data class ConfirmPasswordChanged(val value: String) : ProfileUiEvent
    data object ChangePasswordConfirmed : ProfileUiEvent
}
