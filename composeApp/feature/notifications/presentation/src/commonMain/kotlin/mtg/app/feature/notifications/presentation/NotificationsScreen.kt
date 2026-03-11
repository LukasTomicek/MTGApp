package mtg.app.feature.notifications.presentation

import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mtg.app.feature.notifications.domain.NotificationItem
import mtg.app.feature.notifications.domain.NotificationType
import mtg.app.feature.notifications.presentation.utils.components.NotificationOffersModal
import mtg.app.feature.notifications.presentation.utils.components.NotificationRow

@Composable
fun NotificationsScreen(
    uiState: UiState<NotificationsScreenState>,
    onUiEvent: (NotificationsUiEvent) -> Unit,
) {
    LaunchedEffect(Unit) {
        onUiEvent(NotificationsUiEvent.ScreenOpened)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (uiState.data.items.isEmpty()) {
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val messageItems = uiState.data.items.filter { it.type == NotificationType.NEW_MESSAGE }
                val matchItems = uiState.data.items.filter { it.type == NotificationType.CARD_MATCH }
                val groupedMatchItems = buildGroupedMatchItems(matchItems)

                if (messageItems.isNotEmpty()) {
                    item(key = "section_messages") {
                        SectionHeader(title = "New messages")
                    }
                    items(messageItems, key = { "msg_${it.id}" }) { item ->
                        NotificationRow(
                            item = item,
                            onClick = {
                                onUiEvent(
                                    NotificationsUiEvent.NotificationClicked(
                                        notificationId = item.id,
                                        chatId = item.chatId,
                                        cardId = item.cardId,
                                        cardName = item.cardName,
                                        type = item.type,
                                    )
                                )
                            },
                            onDeleteClick = {
                                onUiEvent(NotificationsUiEvent.DeleteNotificationClicked(item.id))
                            },
                        )
                    }
                }

                if (matchItems.isNotEmpty()) {
                    item(key = "section_matches") {
                        SectionHeader(title = "Card matches")
                    }
                    items(groupedMatchItems, key = { "match_${it.key}" }) { grouped ->
                        NotificationRow(
                            item = grouped.item,
                            offersCount = grouped.offersCount,
                            onClick = {
                                onUiEvent(
                                    NotificationsUiEvent.NotificationClicked(
                                        notificationId = grouped.item.id,
                                        chatId = grouped.item.chatId,
                                        cardId = grouped.item.cardId,
                                        cardName = grouped.item.cardName,
                                        type = grouped.item.type,
                                    )
                                )
                            },
                            onDeleteClick = {
                                onUiEvent(NotificationsUiEvent.DeleteNotificationClicked(grouped.item.id))
                            },
                        )
                    }
                }
            }
        }
    }

    if (uiState.data.isOffersDialogVisible) {
        NotificationOffersModal(
            cardName = uiState.data.selectedOfferCardName,
            offers = uiState.data.offersForSelectedCard,
            selectedNotificationId = uiState.data.selectedOfferNotificationId,
            onOfferSelected = { onUiEvent(NotificationsUiEvent.OfferSelected(it)) },
            onDismiss = { onUiEvent(NotificationsUiEvent.OfferDialogDismissed) },
            onViewProfile = { onUiEvent(NotificationsUiEvent.OfferViewProfileClicked) },
            onMessageSeller = { onUiEvent(NotificationsUiEvent.OfferMessageClicked) },
        )
    }
}

private data class GroupedMatchItem(
    val key: String,
    val item: NotificationItem,
    val offersCount: Int,
)

private fun buildGroupedMatchItems(
    matchItems: List<NotificationItem>,
): List<GroupedMatchItem> {
    if (matchItems.isEmpty()) return emptyList()

    val groups = linkedMapOf<String, MutableList<NotificationItem>>()
    matchItems.forEach { item ->
        val key = item.cardId.ifBlank { item.cardName.trim().lowercase() }
        val normalizedKey = if (key.isBlank()) item.id else key
        groups.getOrPut(normalizedKey) { mutableListOf() }.add(item)
    }

    return groups.map { (key, items) ->
        val representative = items.first()
        val count = items.size
        val allRead = items.all { it.isRead }
        val title = if (count > 1) "${representative.cardName} ($count offers)" else representative.cardName
        val message = if (count > 1) "Multiple sellers matched this card ${items.first().cardName}" else representative.message
        GroupedMatchItem(
            key = key,
            item = representative.copy(
                cardName = title,
                message = message,
                isRead = allRead,
            ),
            offersCount = count,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}
