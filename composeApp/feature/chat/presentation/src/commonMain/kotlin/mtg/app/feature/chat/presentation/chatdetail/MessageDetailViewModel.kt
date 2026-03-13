package mtg.app.feature.chat.presentation.chatdetail

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.chat.domain.ChatService
import mtg.app.feature.chat.domain.DealStatus
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.chat.domain.obj.SendChatMessageRequest
import mtg.app.feature.chat.domain.obj.SubmitChatRatingRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class MessageDetailViewModel(
    private val authService: AuthDomainService,
    private val chatService: ChatService,
) : BaseViewModel<MessageDetailScreenState, MessageDetailUiEvent, MessageDetailDirection>(
    initialState = MessageDetailScreenState(),
) {
    private var currentUid: String? = null
    private var currentIdToken: String? = null
    private var loadedChatId: String? = null
    private var pollingJob: Job? = null

    init {
        launch {
            authService.currentUser.collect { user ->
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
                navigate(MessageDetailDirection.CloseChat)
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

        domainCall(
            action = {
                val authContext = AuthContext(uid = currentUser.orEmpty(), idToken = idToken)
                val meta = chatService.loadChatMeta(context = authContext, chatId = chatId)
                val messages = chatService.loadChatMessages(
                    context = authContext,
                    chatId = chatId,
                )
                val counterpartUid = when (currentUser) {
                    meta?.buyerUid -> meta?.sellerUid.orEmpty()
                    meta?.sellerUid -> meta?.buyerUid.orEmpty()
                    else -> ""
                }
                val summary = if (counterpartUid.isNotBlank()) {
                    runCatching {
                        chatService.loadUserRatingSummary(
                            context = authContext,
                            uid = counterpartUid,
                        )
                    }.getOrNull()
                } else {
                    null
                }
                val alreadyRated = if (!currentUser.isNullOrBlank()) {
                    runCatching {
                        chatService.hasRatedChat(
                            context = authContext,
                            chatId = chatId,
                        )
                    }.getOrDefault(false)
                } else {
                    false
                }
                ReloadSnapshot(meta, messages, summary, counterpartUid, alreadyRated)
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to load chat")
            },
        ) { snapshot ->
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

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            action = {
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
                chatService.sendMessage(
                    context = AuthContext(uid = uid, idToken = idToken),
                    request = SendChatMessageRequest(chatId = chatId, senderEmail = senderEmail, text = text),
                )
            },
            onError = {
                setError(it.message ?: "Failed to send message")
                reload()
            },
        ) {
                setError(null)
                reload()
        }
    }

    private fun proposeDeal() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return
        val chatId = state.value.data.chatId
        if (chatId.isBlank()) return

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            action = {
                chatService.proposeDeal(
                    context = AuthContext(uid = uid, idToken = idToken),
                    chatId = chatId,
                )
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to propose deal")
            },
        ) {
                reload()
        }
    }

    private fun confirmDeal() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return
        val chatId = state.value.data.chatId
        if (chatId.isBlank()) return

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            action = {
                chatService.confirmDeal(
                    context = AuthContext(uid = uid, idToken = idToken),
                    chatId = chatId,
                )
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to confirm deal")
            },
        ) {
                reload()
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

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            action = {
                chatService.submitRating(
                    context = AuthContext(uid = raterUid, idToken = idToken),
                    request = SubmitChatRatingRequest(
                        ratedUid = current.counterpartUid,
                        chatId = current.chatId,
                        score = current.ratingScoreDraft,
                        comment = current.ratingCommentDraft.trim(),
                    ),
                )
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to submit rating")
            },
        ) {
                updateState {
                    it.copy(
                        isRatingModalVisible = false,
                        ratingCommentDraft = "",
                    )
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
