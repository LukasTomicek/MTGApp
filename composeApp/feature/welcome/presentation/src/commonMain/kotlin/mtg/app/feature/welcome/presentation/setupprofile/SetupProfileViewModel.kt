package mtg.app.feature.welcome.presentation.setupprofile

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.welcome.domain.WelcomeService
import mtg.app.core.domain.obj.AuthContext
import kotlinx.coroutines.flow.collect

class SetupProfileViewModel(
    private val authService: AuthDomainService,
    private val welcomeService: WelcomeService,
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
            authService.currentUser.collect { user ->
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

                domainCall(
                    loading = null,
                    clearErrorOnStart = false,
                    onError = {},
                    action = { welcomeService.loadNickname(uid = user.uid) },
                ) { nickname ->
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

        domainCall(
            loading = { isLoading ->
                updateState { it.copy(isSavingNickname = isLoading, nicknameError = null) }
            },
            clearErrorOnStart = false,
            onError = { throwable ->
                updateState {
                    it.copy(
                        nicknameError = throwable.message ?: "Failed to save nickname",
                    )
                }
            },
            action = {
                val context = AuthContext(uid = uid, idToken = idToken)
                welcomeService.saveNickname(
                    context = context,
                    nickname = nickname,
                )
                welcomeService.saveOnboardingCompleted(
                    context = context,
                    completed = true,
                )
            },
        ) {
                navigate(SetupProfileDirection.NavigateToHome)
        }
    }
}
