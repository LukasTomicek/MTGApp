package mtg.app.feature.welcome.presentation.setupprofile

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.welcome.domain.LoadWelcomeNicknameUseCase
import mtg.app.feature.welcome.domain.SaveOnboardingCompletedUseCase
import mtg.app.feature.welcome.domain.SaveWelcomeNicknameUseCase
import kotlinx.coroutines.flow.collect

class SetupProfileViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val loadWelcomeNickname: LoadWelcomeNicknameUseCase,
    private val saveWelcomeNickname: SaveWelcomeNicknameUseCase,
    private val saveOnboardingCompleted: SaveOnboardingCompletedUseCase,
) : BaseViewModel<SetupProfileScreenState, SetupProfileUiEvent, SetupProfileDirection>(
    initialState = SetupProfileScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        observeUser()
    }

    override fun onUiEvent(event: SetupProfileUiEvent) {
        when (event) {
            is SetupProfileUiEvent.NicknameChanged -> {
                updateState {
                    it.copy(
                        nickname = event.value,
                        nicknameError = null,
                    )
                }
            }

            SetupProfileUiEvent.BackClicked -> navigate(SetupProfileDirection.NavigateBack)
            SetupProfileUiEvent.ContinueClicked -> onContinueClicked()
        }
    }

    private fun observeUser() {
        launch {
            observeAuthState().collect { user ->
                currentUid = user?.uid
                currentIdToken = user?.idToken

                if (user == null) {
                    updateState {
                        it.copy(
                            nickname = "",
                            nicknameError = "Sign in required",
                        )
                    }
                    return@collect
                }

                runCatching {
                    loadWelcomeNickname(uid = user.uid, idToken = user.idToken)
                }.onSuccess { nickname ->
                    if (!nickname.isNullOrBlank()) {
                        updateState {
                            it.copy(
                                nickname = nickname,
                                nicknameError = null,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onContinueClicked() {
        val uid = currentUid
        val idToken = currentIdToken
        val nickname = state.value.data.nickname.trim()

        if (nickname.isBlank()) {
            updateState { it.copy(nicknameError = "Nickname is required") }
            return
        }

        if (uid == null || idToken == null) {
            updateState { it.copy(nicknameError = "Sign in required") }
            return
        }

        launch {
            updateState { it.copy(isSavingNickname = true, nicknameError = null) }
            runCatching {
                saveWelcomeNickname(uid = uid, idToken = idToken, nickname = nickname)
                saveOnboardingCompleted(uid = uid, idToken = idToken, completed = true)
            }.onSuccess {
                updateState { it.copy(isSavingNickname = false) }
                navigate(SetupProfileDirection.NavigateToHome)
            }.onFailure { throwable ->
                updateState {
                    it.copy(
                        isSavingNickname = false,
                        nicknameError = throwable.message ?: "Failed to save nickname",
                    )
                }
            }
        }
    }
}
