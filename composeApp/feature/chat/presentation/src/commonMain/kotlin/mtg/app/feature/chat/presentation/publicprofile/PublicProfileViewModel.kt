package mtg.app.feature.chat.presentation.publicprofile

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.chat.domain.ChatService
import mtg.app.feature.chat.domain.UserRatingSummary
import mtg.app.core.domain.obj.AuthContext
import kotlinx.coroutines.flow.collect

class PublicProfileViewModel(
    private val authService: AuthDomainService,
    private val chatService: ChatService,
) : BaseViewModel<PublicProfileScreenState, PublicProfileUiEvent, PublicProfileDirection>(
    initialState = PublicProfileScreenState(),
) {
    private var currentIdToken: String? = null

    init {
        launch {
            authService.currentUser.collect { user ->
                currentIdToken = user?.idToken
                val targetUid = state.value.data.targetUid
                if (!targetUid.isBlank() && !currentIdToken.isNullOrBlank()) {
                    load(targetUid)
                }
            }
        }
    }

    override fun onUiEvent(event: PublicProfileUiEvent) {
        when (event) {
            is PublicProfileUiEvent.ScreenOpened -> {
                val uid = event.uid.trim()
                if (uid.isBlank()) return
                if (state.value.data.targetUid != uid) {
                    updateState { it.copy(targetUid = uid) }
                }
                load(uid)
            }

            PublicProfileUiEvent.RetryClicked -> {
                val uid = state.value.data.targetUid
                if (uid.isNotBlank()) {
                    load(uid)
                }
            }
        }
    }

    private fun load(targetUid: String) {
        val idToken = currentIdToken ?: return

        launch {
            setLoading(true)
            setError(null)
            val context = AuthContext(uid = "", idToken = idToken)

            var hasError = false

            val nickname = runCatching {
                chatService.loadUserNickname(uid = targetUid)
                    ?.trim()
                    .orEmpty()
            }.getOrElse {
                hasError = true
                ""
            }.ifBlank { targetUid }

            val summary = runCatching {
                chatService.loadUserRatingSummary(context = context, uid = targetUid)
            }.getOrElse {
                hasError = true
                UserRatingSummary(average = 0.0, count = 0)
            }

            val reviews = runCatching {
                chatService.loadUserReviews(context = context, uid = targetUid)
            }.getOrElse {
                hasError = true
                emptyList()
            }

            val sellOffers = runCatching {
                chatService.loadUserSellOffers(context = context, uid = targetUid)
            }.getOrElse {
                hasError = true
                emptyList()
            }

            updateState {
                it.copy(
                    targetUid = targetUid,
                    nickname = nickname,
                    ratingAverage = summary.average,
                    ratingCount = summary.count,
                    reviews = reviews,
                    sellOffers = sellOffers,
                )
            }
            if (hasError) {
                setError("Some profile data could not be loaded")
            }
            setLoading(false)
        }
    }
}
