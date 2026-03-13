package mtg.app.feature.settings.presentation.profile

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.chat.domain.ChatService
import mtg.app.feature.welcome.domain.WelcomeService
import mtg.app.core.domain.obj.AuthContext
import kotlinx.coroutines.flow.collect

class ProfileViewModel(
    private val authService: AuthDomainService,
    private val welcomeService: WelcomeService,
    private val chatService: ChatService,
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
                        currentPasswordInput = "",
                        newPasswordInput = "",
                        confirmPasswordInput = "",
                        passwordError = null,
                    )
                }
            }

            is ProfileUiEvent.CurrentPasswordChanged -> {
                updateState { it.copy(currentPasswordInput = event.value, passwordError = null) }
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
            authService.currentUser.collect { user ->
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

                domainCall(
                    loading = null,
                    clearErrorOnStart = false,
                    action = { welcomeService.loadNickname(uid = user.uid) },
                    onError = { throwable ->
                        updateState {
                            it.copy(
                                nicknameError = throwable.message ?: "Failed to load nickname",
                                infoMessage = "",
                            )
                        }
                    },
                ) { nickname ->
                    updateState {
                        it.copy(
                            nickname = nickname?.trim().orEmpty(),
                            nicknameDraftInput = "",
                            nicknameError = null,
                            infoMessage = "",
                            reviewsError = null,
                        )
                    }
                }

                domainCall(
                    loading = null,
                    clearErrorOnStart = false,
                    action = {
                        chatService.loadUserReviews(
                        context = AuthContext(uid = user.uid, idToken = user.idToken),
                        uid = user.uid,
                    )
                    },
                    onError = { throwable ->
                        updateState {
                            it.copy(
                                reviews = emptyList(),
                                reviewsError = throwable.message ?: "Failed to load reviews",
                            )
                        }
                    },
                ) { reviews ->
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

        domainCall(
            clearErrorOnStart = true,
            onError = { throwable ->
                updateState {
                    it.copy(
                        nicknameError = throwable.message ?: "Failed to save nickname",
                        infoMessage = "",
                    )
                }
            },
            action = {
                updateState { it.copy(nicknameError = null, infoMessage = "") }
                welcomeService.saveNickname(
                    context = AuthContext(uid = uid, idToken = idToken),
                    nickname = nickname,
                )
            },
        ) {
                updateState {
                    it.copy(
                        nickname = nickname,
                        nicknameDraftInput = "",
                        isChangeNicknameModalVisible = false,
                        infoMessage = "Nickname updated",
                    )
                }
        }
    }

    private fun savePassword() {
        val uid = currentUid
        if (uid == null) {
            updateState { it.copy(passwordError = "Sign in required") }
            return
        }

        val stateData = state.value.data
        val currentPassword = stateData.currentPasswordInput
        val newPassword = stateData.newPasswordInput.trim()
        val confirmPassword = stateData.confirmPasswordInput.trim()

        if (currentPassword.isBlank()) {
            updateState { it.copy(passwordError = "Current password is required") }
            return
        }
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

        domainCall(
            clearErrorOnStart = true,
            onError = { throwable ->
                updateState {
                    it.copy(passwordError = throwable.message ?: "Failed to change password")
                }
            },
            action = {
                updateState { it.copy(passwordError = null, infoMessage = "") }
                authService.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                )
            },
        ) {
                updateState {
                    it.copy(
                        isChangePasswordModalVisible = false,
                        currentPasswordInput = "",
                        newPasswordInput = "",
                        confirmPasswordInput = "",
                        passwordError = null,
                        infoMessage = "Password updated",
                    )
                }
        }
    }
}
