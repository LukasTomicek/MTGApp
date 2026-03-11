package mtg.app.feature.chat.presentation.chatdetail

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.ObserveAuthStateUseCase
import mtg.app.feature.chat.domain.ConfirmDealUseCase
import mtg.app.feature.chat.domain.DealStatus
import mtg.app.feature.chat.domain.DeleteChatThreadUseCase
import mtg.app.feature.chat.domain.HasRatedChatUseCase
import mtg.app.feature.chat.domain.LoadChatMessagesUseCase
import mtg.app.feature.chat.domain.LoadChatMetaUseCase
import mtg.app.feature.chat.domain.LoadUserRatingSummaryUseCase
import mtg.app.feature.chat.domain.ProposeDealUseCase
import mtg.app.feature.chat.domain.SendChatMessageUseCase
import mtg.app.feature.chat.domain.SubmitUserRatingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class MessageDetailViewModel(
    private val observeAuthState: ObserveAuthStateUseCase,
    private val loadChatMeta: LoadChatMetaUseCase,
    private val loadChatMessages: LoadChatMessagesUseCase,
    private val sendChatMessage: SendChatMessageUseCase,
    private val proposeDealUseCase: ProposeDealUseCase,
    private val confirmDealUseCase: ConfirmDealUseCase,
    private val deleteChatThreadUseCase: DeleteChatThreadUseCase,
    private val loadUserRatingSummary: LoadUserRatingSummaryUseCase,
    private val hasRatedChat: HasRatedChatUseCase,
    private val submitUserRating: SubmitUserRatingUseCase,
) : BaseViewModel<MessageDetailScreenState, MessageDetailUiEvent, MessageDetailDirection>(
    initialState = MessageDetailScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null
    private var loadedChatId: String? = null
    private var pollingJob: Job? = null

    init {
        launch {
            observeAuthState().collect { user ->
                currentUid = user?.uid
                currentIdToken = user?.idToken
                updateState { it.copy(currentUserUid = user?.uid.orEmpty()) }
            }
        }
    }

    override fun onUiEvent(event: MessageDetailUiEvent) {
        when (event) {
            is MessageDetailUiEvent.ScreenOpened -> openChat(event.chatId)
            MessageDetailUiEvent.ScreenClosed -> stopPolling()
            is MessageDetailUiEvent.InputChanged -> updateState { it.copy(input = event.value) }
            MessageDetailUiEvent.SendClicked -> send()
            MessageDetailUiEvent.ReloadClicked -> reload()
            MessageDetailUiEvent.ProposeDealClicked -> proposeDeal()
            MessageDetailUiEvent.ConfirmDealClicked -> confirmDeal()
            MessageDetailUiEvent.RatingDismissed -> {
                updateState { it.copy(isRatingModalVisible = false, ratingCommentDraft = "") }
                closeCurrentThreadAndExit()
            }
            is MessageDetailUiEvent.RatingScoreChanged -> {
                updateState { it.copy(ratingScoreDraft = event.value.coerceIn(1, 5)) }
            }
            is MessageDetailUiEvent.RatingCommentChanged -> {
                updateState { it.copy(ratingCommentDraft = event.value) }
            }
            MessageDetailUiEvent.SubmitRatingClicked -> submitRating()
        }
    }

    private fun openChat(chatId: String) {
        if (loadedChatId == chatId) return
        loadedChatId = chatId
        updateState {
            it.copy(
                chatId = chatId,
                isRatingModalVisible = false,
                hasSeenCompletedPrompt = false,
            )
        }
        reload()
        startPolling()
    }

    private fun reload() {
        val chatId = state.value.data.chatId
        val idToken = currentIdToken ?: return
        val currentUser = currentUid
        if (chatId.isBlank()) return

        launch {
            setLoading(true)
            setError(null)
            runCatching {
                val meta = loadChatMeta(chatId = chatId, idToken = idToken)
                val messages = loadChatMessages(chatId = chatId, idToken = idToken)
                val counterpartUid = when (currentUser) {
                    meta?.buyerUid -> meta?.sellerUid.orEmpty()
                    meta?.sellerUid -> meta?.buyerUid.orEmpty()
                    else -> ""
                }
                val summary = if (counterpartUid.isNotBlank()) {
                    runCatching { loadUserRatingSummary(uid = counterpartUid, idToken = idToken) }.getOrNull()
                } else {
                    null
                }
                val alreadyRated = if (!currentUser.isNullOrBlank()) {
                    runCatching {
                        hasRatedChat(uid = currentUser, idToken = idToken, chatId = chatId)
                    }.getOrDefault(false)
                } else {
                    false
                }
                ReloadSnapshot(meta, messages, summary, counterpartUid, alreadyRated)
            }.onSuccess { snapshot ->
                updateState { current ->
                    val shouldAutoOpenRating =
                        snapshot.meta?.dealStatus == DealStatus.COMPLETED &&
                            !current.hasSeenCompletedPrompt

                    current.copy(
                        buyerUid = snapshot.meta?.buyerUid.orEmpty(),
                        sellerUid = snapshot.meta?.sellerUid.orEmpty(),
                        cardName = snapshot.meta?.cardName.orEmpty(),
                        counterpartEmail = when (currentUser) {
                            snapshot.meta?.buyerUid -> snapshot.meta?.sellerEmail.orEmpty()
                            snapshot.meta?.sellerUid -> snapshot.meta?.buyerEmail.orEmpty()
                            else -> snapshot.meta?.sellerEmail.orEmpty()
                        },
                        counterpartUid = snapshot.counterpartUid,
                        isCurrentUserBuyer = currentUser == snapshot.meta?.buyerUid,
                        dealStatus = snapshot.meta?.dealStatus ?: DealStatus.OPEN,
                        buyerConfirmed = snapshot.meta?.buyerConfirmed ?: false,
                        sellerConfirmed = snapshot.meta?.sellerConfirmed ?: false,
                        buyerCompleted = snapshot.meta?.buyerCompleted ?: false,
                        sellerCompleted = snapshot.meta?.sellerCompleted ?: false,
                        counterpartRatingAverage = snapshot.summary?.average ?: 0.0,
                        counterpartRatingCount = snapshot.summary?.count ?: 0,
                        alreadyRatedByCurrentUser = snapshot.alreadyRated,
                        messages = snapshot.messages,
                        isRatingModalVisible = shouldAutoOpenRating || current.isRatingModalVisible,
                        hasSeenCompletedPrompt = current.hasSeenCompletedPrompt || shouldAutoOpenRating,
                    )
                }
                if (snapshot.meta?.dealStatus == DealStatus.COMPLETED) {
                    stopPolling()
                }
            }.onFailure {
                setError(it.message ?: "Failed to load chat")
            }
            setLoading(false)
        }
    }

    private fun send() {
        val uid = currentUid
        val idToken = currentIdToken
        if (uid == null || idToken == null) {
            setError("Session expired. Please sign in again.")
            return
        }
        val senderEmail = uid
        val chatId = state.value.data.chatId
        val text = state.value.data.input.trim()
        if (chatId.isBlank() || text.isBlank()) return

        launch {
            updateState { current ->
                current.copy(
                    input = "",
                    messages = current.messages + mtg.app.feature.chat.domain.ChatMessage(
                        id = "local-${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                        senderUid = uid,
                        senderEmail = senderEmail,
                        text = text,
                        createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
            runCatching {
                sendChatMessage(
                    uid = uid,
                    idToken = idToken,
                    chatId = chatId,
                    senderEmail = senderEmail,
                    text = text,
                )
            }.onSuccess {
                setError(null)
                reload()
            }.onFailure {
                setError(it.message ?: "Failed to send message")
                reload()
            }
        }
    }

    private fun proposeDeal() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return
        val chatId = state.value.data.chatId
        if (chatId.isBlank()) return

        launch {
            runCatching {
                proposeDealUseCase(uid = uid, idToken = idToken, chatId = chatId)
            }.onSuccess {
                reload()
            }.onFailure {
                setError(it.message ?: "Failed to propose deal")
            }
        }
    }

    private fun confirmDeal() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return
        val chatId = state.value.data.chatId
        if (chatId.isBlank()) return

        launch {
            runCatching {
                confirmDealUseCase(uid = uid, idToken = idToken, chatId = chatId)
            }.onSuccess {
                reload()
            }.onFailure {
                setError(it.message ?: "Failed to confirm deal")
            }
        }
    }

    private fun submitRating() {
        val raterUid = currentUid ?: return
        val idToken = currentIdToken ?: return
        val current = state.value.data
        if (current.alreadyRatedByCurrentUser) {
            setError("You already rated this chat")
            return
        }
        if (current.dealStatus != DealStatus.COMPLETED) {
            setError("Trade must be completed before rating")
            return
        }
        if (current.chatId.isBlank() || current.counterpartUid.isBlank()) return

        launch {
            runCatching {
                submitUserRating(
                    raterUid = raterUid,
                    ratedUid = current.counterpartUid,
                    chatId = current.chatId,
                    idToken = idToken,
                    score = current.ratingScoreDraft,
                    comment = current.ratingCommentDraft.trim(),
                )
            }.onSuccess {
                updateState {
                    it.copy(
                        isRatingModalVisible = false,
                        ratingCommentDraft = "",
                    )
                }
                closeCurrentThreadAndExit()
            }.onFailure {
                setError(it.message ?: "Failed to submit rating")
            }
        }
    }

    private fun closeCurrentThreadAndExit() {
        val uid = currentUid
        val idToken = currentIdToken
        val current = state.value.data
        val chatId = current.chatId
        val counterpartUid = current.counterpartUid

        if (uid == null || idToken == null || chatId.isBlank() || counterpartUid.isBlank()) {
            navigate(MessageDetailDirection.CloseChat)
            return
        }

        launch {
            runCatching {
                deleteChatThreadUseCase(
                    uid = uid,
                    idToken = idToken,
                    chatId = chatId,
                    counterpartUid = counterpartUid,
                )
            }.onFailure {
                setError(it.message ?: "Failed to close chat")
            }
            navigate(MessageDetailDirection.CloseChat)
        }
    }

    private fun startPolling() {
        stopPolling()
        pollingJob = launch {
            while (true) {
                delay(3_000)
                if (state.value.data.isRatingModalVisible) {
                    continue
                }
                reload()
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
}

private data class ReloadSnapshot(
    val meta: mtg.app.feature.chat.domain.ChatMeta?,
    val messages: List<mtg.app.feature.chat.domain.ChatMessage>,
    val summary: mtg.app.feature.chat.domain.UserRatingSummary?,
    val counterpartUid: String,
    val alreadyRated: Boolean,
)
