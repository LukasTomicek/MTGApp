package mtg.app.feature.chat.presentation.publicprofile

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.chat.domain.LoadUserNicknameUseCase
import mtg.app.feature.chat.domain.LoadUserReviewsUseCase
import mtg.app.feature.chat.domain.LoadUserRatingSummaryUseCase
import mtg.app.feature.chat.domain.LoadUserSellOffersUseCase
import mtg.app.feature.chat.domain.UserRatingSummary
import kotlinx.coroutines.flow.collect

class PublicProfileViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val loadUserNickname: LoadUserNicknameUseCase,
    private val loadUserRatingSummary: LoadUserRatingSummaryUseCase,
    private val loadUserReviews: LoadUserReviewsUseCase,
    private val loadUserSellOffers: LoadUserSellOffersUseCase,
) : BaseViewModel<PublicProfileScreenState, PublicProfileUiEvent, PublicProfileDirection>(
    initialState = PublicProfileScreenState(),
) {
    private var currentIdToken: String? = null

    init {
        launch {
            observeAuthState().collect { user ->
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

            var hasError = false

            val nickname = runCatching {
                loadUserNickname(uid = targetUid, idToken = idToken)
                    ?.trim()
                    .orEmpty()
            }.getOrElse {
                hasError = true
                ""
            }.ifBlank { targetUid }

            val summary = runCatching {
                loadUserRatingSummary(uid = targetUid, idToken = idToken)
            }.getOrElse {
                hasError = true
                UserRatingSummary(average = 0.0, count = 0)
            }

            val reviews = runCatching {
                loadUserReviews(uid = targetUid, idToken = idToken)
            }.getOrElse {
                hasError = true
                emptyList()
            }

            val sellOffers = runCatching {
                loadUserSellOffers(uid = targetUid, idToken = idToken)
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
