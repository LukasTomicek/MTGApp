package mtg.app.feature.notifications.presentation.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonColors
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.AppButtonTypes
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.theme.AppTheme
import mtg.app.feature.notifications.domain.NotificationItem
import mtg.app.feature.notifications.domain.NotificationType

@Composable
fun NotificationRow(
    item: NotificationItem,
    offersCount: Int = 1,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val isGroupedMatch = item.type == NotificationType.CARD_MATCH && offersCount > 1
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isGroupedMatch) {
                    Modifier.border(
                        width = 1.dp,
                        color = AppTheme.colors.primary600,
                        shape = AppTheme.shapes.small,
                    )
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = notificationBackgroundColor(item, isGroupedMatch),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "IMG", style = MaterialTheme.typography.labelSmall)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isGroupedMatch) "Grouped offer" else item.sellerEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                                        text = item.message ?: defaultMessageFor(item, offersCount),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AppButton(
                modifier = Modifier.width(74.dp),
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

private fun defaultMessageFor(item: NotificationItem, offersCount: Int): String {
    return when (item.type) {
        NotificationType.NEW_MESSAGE -> "New message about ${item.cardName}"
        NotificationType.CARD_MATCH -> {
            if (offersCount > 1) {
                "${item.cardName} has $offersCount offers"
            } else {
                "${item.cardName} has a new trade match"
            }
        }
    }
}

@Composable
private fun notificationBackgroundColor(item: NotificationItem, isGroupedMatch: Boolean) = when (item.type) {
    NotificationType.NEW_MESSAGE -> {
        if (item.isRead) AppTheme.colors.primary100 else AppTheme.colors.primary200
    }

    NotificationType.CARD_MATCH -> {
        if (isGroupedMatch) {
            if (item.isRead) AppTheme.colors.gray200 else AppTheme.colors.gray300
        } else {
            if (item.isRead) AppTheme.colors.gray100 else AppTheme.colors.gray200
        }
    }
}
