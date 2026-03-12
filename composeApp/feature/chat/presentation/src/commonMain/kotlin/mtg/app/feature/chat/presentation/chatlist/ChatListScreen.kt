package mtg.app.feature.chat.presentation.chatlist

import mtg.app.core.presentation.state.UiState
import mtg.app.feature.chat.domain.MessageThread
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonColors
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.AppButtonTypes
import mtg.app.core.presentation.components.TextState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChatListScreen(
    uiState: UiState<ChatListScreenState>,
    onUiEvent: (ChatListUiEvent) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        onUiEvent(ChatListUiEvent.ScreenOpened)
        onDispose {
            onUiEvent(ChatListUiEvent.ScreenClosed)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onUiEvent(ChatListUiEvent.ReloadClicked)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (uiState.data.items.isEmpty()) {
            Text(
                text = uiState.data.infoMessage.ifBlank { "No chats yet" },
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.data.items, key = { it.chatId }) { item ->
                    MessageThreadRow(
                        item = item,
                        onClick = { onUiEvent(ChatListUiEvent.ThreadClicked(item.chatId)) },
                        onDeleteClick = {
                            onUiEvent(
                                ChatListUiEvent.DeleteThreadClicked(
                                    chatId = item.chatId,
                                    counterpartUid = item.counterpartUid,
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageThreadRow(
    item: MessageThread,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.counterpartNickname.ifBlank {
                        item.counterpartEmail
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "You are ${item.role}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Text(
                text = item.cardName,
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.lastMessage ?: "No Message Yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.weight(1f))

                AppButton(
                    state = AppButtonState(
                        title = TextState("Delete"),
                        colors = AppButtonColors.ErrorSecondary,
                        buttonType = AppButtonTypes.Small,
                        onClick = onDeleteClick,
                    ),
                )
            }
        }
    }
}
