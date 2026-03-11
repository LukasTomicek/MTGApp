package mtg.app.feature.settings.presentation.profile

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ChangePasswordUseCase
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.chat.domain.LoadUserReviewsUseCase
import mtg.app.feature.welcome.domain.LoadWelcomeNicknameUseCase
import mtg.app.feature.welcome.domain.SaveWelcomeNicknameUseCase
import kotlinx.coroutines.flow.collect

class ProfileViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val loadWelcomeNickname: LoadWelcomeNicknameUseCase,
    private val saveWelcomeNickname: SaveWelcomeNicknameUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val loadUserReviews: LoadUserReviewsUseCase,
) : BaseViewModel<ProfileScreenState, ProfileUiEvent, ProfileDirection>(
    initialState = ProfileScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        observeUser()
    }

    override fun onUiEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.ChangeNicknameClicked -> {
                updateState {
                    it.copy(
                        isChangeNicknameModalVisible = true,
                        nicknameDraftInput = it.nickname,
                        nicknameError = null,
                        infoMessage = "",
                    )
                }
            }

            ProfileUiEvent.ChangeNicknameDismissed -> {
                updateState {
                    it.copy(
                        isChangeNicknameModalVisible = false,
                        nicknameDraftInput = "",
                        nicknameError = null,
                    )
                }
            }

            is ProfileUiEvent.NicknameDraftChanged -> {
                updateState { it.copy(nicknameDraftInput = event.value, nicknameError = null) }
            }

            ProfileUiEvent.ChangeNicknameConfirmed -> saveNickname()
            ProfileUiEvent.ChangePasswordClicked -> {
                updateState { it.copy(isChangePasswordModalVisible = true, passwordError = null) }
            }

            ProfileUiEvent.ChangePasswordDismissed -> {
                updateState {
                    it.copy(
                        isChangePasswordModalVisible = false,
                        newPasswordInput = "",
                        confirmPasswordInput = "",
                        passwordError = null,
                    )
                }
            }

            is ProfileUiEvent.NewPasswordChanged -> {
                updateState { it.copy(newPasswordInput = event.value, passwordError = null) }
            }

            is ProfileUiEvent.ConfirmPasswordChanged -> {
                updateState { it.copy(confirmPasswordInput = event.value, passwordError = null) }
            }

            ProfileUiEvent.ChangePasswordConfirmed -> savePassword()
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
                            nicknameDraftInput = "",
                            nicknameError = "Sign in required",
                            infoMessage = "",
                            reviews = emptyList(),
                            reviewsError = null,
                        )
                    }
                    return@collect
                }

                runCatching {
                    loadWelcomeNickname(uid = user.uid, idToken = user.idToken)
                }.onSuccess { nickname ->
                    updateState {
                        it.copy(
                            nickname = nickname?.trim().orEmpty(),
                            nicknameDraftInput = "",
                            nicknameError = null,
                            infoMessage = "",
                            reviewsError = null,
                        )
                    }
                }.onFailure { throwable ->
                    updateState {
                        it.copy(
                            nicknameError = throwable.message ?: "Failed to load nickname",
                            infoMessage = "",
                        )
                    }
                }

                runCatching {
                    loadUserReviews(uid = user.uid, idToken = user.idToken)
                }.onSuccess { reviews ->
                    updateState {
                        it.copy(
                            reviews = reviews.map { review ->
                                ProfileReviewItem(
                                    score = review.score,
                                    comment = review.comment,
                                    createdAt = review.createdAt,
                                )
                            },
                            reviewsError = null,
                        )
                    }
                }.onFailure { throwable ->
                    updateState {
                        it.copy(
                            reviews = emptyList(),
                            reviewsError = throwable.message ?: "Failed to load reviews",
                        )
                    }
                }
            }
        }
    }

    private fun saveNickname() {
        val uid = currentUid
        val idToken = currentIdToken
        val nickname = state.value.data.nicknameDraftInput.trim()

        if (nickname.isBlank()) {
            updateState { it.copy(nicknameError = "Nickname is required", infoMessage = "") }
            return
        }

        if (uid == null || idToken == null) {
            updateState { it.copy(nicknameError = "Sign in required", infoMessage = "") }
            return
        }

        launch {
            setLoading(true)
            setError(null)
            updateState { it.copy(nicknameError = null, infoMessage = "") }

            runCatching {
                saveWelcomeNickname(uid = uid, idToken = idToken, nickname = nickname)
            }.onSuccess {
                updateState {
                    it.copy(
                        nickname = nickname,
                        nicknameDraftInput = "",
                        isChangeNicknameModalVisible = false,
                        infoMessage = "Nickname updated",
                    )
                }
            }.onFailure { throwable ->
                updateState {
                    it.copy(
                        nicknameError = throwable.message ?: "Failed to save nickname",
                        infoMessage = "",
                    )
                }
            }

            setLoading(false)
        }
    }

    private fun savePassword() {
        val uid = currentUid
        if (uid == null) {
            updateState { it.copy(passwordError = "Sign in required") }
            return
        }

        val stateData = state.value.data
        val newPassword = stateData.newPasswordInput.trim()
        val confirmPassword = stateData.confirmPasswordInput.trim()

        if (newPassword.isBlank()) {
            updateState { it.copy(passwordError = "Password is required") }
            return
        }
        if (newPassword.length < 6) {
            updateState { it.copy(passwordError = "Password must have at least 6 characters") }
            return
        }
        if (confirmPassword.isNotBlank() && confirmPassword != newPassword) {
            updateState { it.copy(passwordError = "Passwords do not match") }
            return
        }

        launch {
            setLoading(true)
            setError(null)
            updateState { it.copy(passwordError = null, infoMessage = "") }

            runCatching {
                changePasswordUseCase(newPassword = newPassword)
            }.onSuccess {
                updateState {
                    it.copy(
                        isChangePasswordModalVisible = false,
                        newPasswordInput = "",
                        confirmPasswordInput = "",
                        passwordError = null,
                        infoMessage = "Password updated",
                    )
                }
            }.onFailure { throwable ->
                updateState {
                    it.copy(passwordError = throwable.message ?: "Failed to change password")
                }
            }

            setLoading(false)
        }
    }
}
