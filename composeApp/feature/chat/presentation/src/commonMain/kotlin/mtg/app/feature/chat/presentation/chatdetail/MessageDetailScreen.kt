package mtg.app.feature.chat.presentation.chatdetail

import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.AppButtonTypes
import mtg.app.core.presentation.components.TextInputRow
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.state.UiState
import mtg.app.core.presentation.theme.AppTheme
import mtg.app.core.presentation.utils.formatEuroMinorAmount
import mtg.app.feature.chat.domain.DealStatus
import mtg.app.feature.chat.domain.TradePaymentStatus
import mtg.app.feature.chat.domain.TradePayoutStatus
import mtg.app.feature.chat.presentation.utils.ChatMessageRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.round

@Composable
fun MessageDetailScreen(
    chatId: String,
    uiState: UiState<MessageDetailScreenState>,
    onUiEvent: (MessageDetailUiEvent) -> Unit,
    onCounterpartUidResolved: (String) -> Unit = {},
) {
    DisposableEffect(chatId) {
        onUiEvent(MessageDetailUiEvent.ScreenOpened(chatId))
        onDispose {
            onUiEvent(MessageDetailUiEvent.ScreenClosed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        DealAndRatingCard(
            uiState = uiState,
            onUiEvent = onUiEvent,
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.data.messages, key = { it.id }) { message ->
                ChatMessageRow(
                    message = message,
                    isOwnMessage = message.senderUid == uiState.data.currentUserUid,
                )
            }
        }

        TextInputRow(
            query = uiState.data.input,
            label = "Message",
            buttonText = "Send",
            onQueryChange = { onUiEvent(MessageDetailUiEvent.InputChanged(it)) },
            onSearchClick = { onUiEvent(MessageDetailUiEvent.SendClicked) },
        )
    }

    if (uiState.data.isRatingModalVisible) {
        RatingDialog(
            score = uiState.data.ratingScoreDraft,
            comment = uiState.data.ratingCommentDraft,
            onScoreClick = { onUiEvent(MessageDetailUiEvent.RatingScoreChanged(it)) },
            onCommentChanged = { onUiEvent(MessageDetailUiEvent.RatingCommentChanged(it)) },
            onDismiss = { onUiEvent(MessageDetailUiEvent.RatingDismissed) },
            onSubmit = { onUiEvent(MessageDetailUiEvent.SubmitRatingClicked) },
            isSaving = uiState.isLoading,
        )
    }

    LaunchedEffect(uiState.data.counterpartUid) {
        val uid = uiState.data.counterpartUid.trim()
        if (uid.isNotBlank()) {
            onCounterpartUidResolved(uid)
        }
    }
}

@Composable
private fun DealAndRatingCard(
    uiState: UiState<MessageDetailScreenState>,
    onUiEvent: (MessageDetailUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val buyerState = uiState.data.toParticipantDealState(isBuyer = true)
            val sellerState = uiState.data.toParticipantDealState(isBuyer = false)
            val order = uiState.data.order
            Text(
                text = "Deal: ${uiState.data.dealStatus.name}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Buyer: $buyerState | Seller: $sellerState",
                style = MaterialTheme.typography.bodySmall,
            )
            if (order != null) {
                Text(
                    text = "Price: ${formatEuroMinorAmount(order.amountMinor)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "Payment: ${order.paymentStatus.name} | Payout: ${order.payoutStatus.name}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = buildString {
                    append("Counterpart rating: ")
                    if (uiState.data.counterpartRatingCount > 0) {
                        append(uiState.data.counterpartRatingAverage.formatOneDecimal())
                        append(" (")
                        append(uiState.data.counterpartRatingCount)
                        append(")")
                    } else {
                        append("-")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (uiState.data.currentUserUid == uiState.data.buyerUid) {
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState("Reserve and pay"),
                            buttonType = AppButtonTypes.Small,
                            enabled = uiState.data.dealStatus == DealStatus.OPEN,
                            onClick = { onUiEvent(MessageDetailUiEvent.ProposeDealClicked) },
                        ),
                    )
                }

                if (uiState.data.canCurrentUserRefundPayment()) {
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState("Refund payment"),
                            buttonType = AppButtonTypes.Small,
                            enabled = true,
                            onClick = { onUiEvent(MessageDetailUiEvent.RefundPaymentClicked) },
                        ),
                    )
                }

                uiState.data.currentUserActionLabel()?.let { label ->
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState(label),
                            buttonType = AppButtonTypes.Small,
                            enabled = uiState.data.canCurrentUserPerformAction(),
                            onClick = { onUiEvent(MessageDetailUiEvent.ConfirmDealClicked) },
                        ),
                    )
                }
            }
        }
    }
}

private fun MessageDetailScreenState.toParticipantDealState(isBuyer: Boolean): String {
    return when (dealStatus) {
        DealStatus.OPEN -> "pending"
        DealStatus.PROPOSED -> {
            if (isBuyer) {
                when {
                    buyerCompleted -> "complete"
                    buyerConfirmed -> "reserved"
                    else -> "pending"
                }
            } else {
                when {
                    sellerCompleted -> "complete"
                    sellerConfirmed -> "reserved"
                    else -> "pending"
                }
            }
        }
        DealStatus.COMPLETED -> "complete"
        DealStatus.CANCELED -> "canceled"
    }
}

private fun MessageDetailScreenState.currentUserActionLabel(): String? {
    if (dealStatus != DealStatus.PROPOSED) return null
    if (order?.paymentStatus != TradePaymentStatus.PAID) return null

    val isBuyer = currentUserUid == buyerUid
    val isSeller = currentUserUid == sellerUid

    if (buyerConfirmed && sellerConfirmed) {
        if (isBuyer && !buyerCompleted) return "Complete deal"
        if (isSeller && !sellerCompleted) return "Complete deal"
    }
    return null
}

private fun MessageDetailScreenState.canCurrentUserPerformAction(): Boolean {
    if (dealStatus != DealStatus.PROPOSED) return false
    if (order?.paymentStatus != TradePaymentStatus.PAID) return false
    val isBuyer = currentUserUid == buyerUid
    val isSeller = currentUserUid == sellerUid

    if (buyerConfirmed && sellerConfirmed) {
        if (isBuyer && !buyerCompleted) return true
        if (isSeller && !sellerCompleted) return true
    }
    return false
}

private fun MessageDetailScreenState.canCurrentUserRefundPayment(): Boolean {
    val orderState = order ?: return false
    if (currentUserUid != buyerUid) return false
    if (dealStatus == DealStatus.COMPLETED || dealStatus == DealStatus.CANCELED) return false
    if (orderState.paymentStatus != TradePaymentStatus.PAID) return false
    if (orderState.payoutStatus == TradePayoutStatus.PAID_OUT) return false
    return true
}

@Composable
private fun RatingDialog(
    score: Int,
    comment: String,
    onScoreClick: (Int) -> Unit,
    onCommentChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    isSaving: Boolean,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.gray100)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Rate user",
                style = MaterialTheme.typography.titleMedium,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                (1..5).forEach { value ->
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState(if (value == score) "[$value]" else value.toString()),
                            buttonType = AppButtonTypes.Small,
                            enabled = !isSaving,
                            onClick = { onScoreClick(value) },
                        ),
                    )
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                label = { Text("Comment (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = mtg.app.core.presentation.components.appOutlinedTextFieldColors(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    modifier = Modifier.weight(1f),
                    state = AppButtonState(
                        title = TextState("Cancel"),
                        buttonType = AppButtonTypes.Small,
                        enabled = !isSaving,
                        onClick = onDismiss,
                    ),
                )
                AppButton(
                    modifier = Modifier.weight(1f),
                    state = AppButtonState(
                        title = TextState("Submit"),
                        buttonType = AppButtonTypes.Small,
                        enabled = !isSaving,
                        onClick = onSubmit,
                    ),
                )
            }
        }
    }
}


private fun Double.formatOneDecimal(): String {
    val rounded = round(this * 10.0) / 10.0
    val whole = rounded.toLong()
    return if (rounded == whole.toDouble()) {
        "$whole.0"
    } else {
        rounded.toString()
    }
}
