package mtg.app.feature.notifications.presentation.utils.components

import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.AppButtonTypes
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.theme.AppTheme
import mtg.app.core.presentation.utils.formatEuroPrice
import mtg.app.feature.notifications.domain.NotificationItem
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun NotificationOffersModal(
    cardName: String,
    offers: List<NotificationItem>,
    selectedNotificationId: String?,
    onOfferSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onViewProfile: () -> Unit,
    onMessageSeller: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = AppTheme.shapes.medium)
                .background(color = AppTheme.colors.gray100),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = "Offers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = cardName,
                    style = MaterialTheme.typography.titleSmall,
                )

                if (offers.isEmpty()) {
                    Text(
                        text = "No offers available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .border(
                                width = 1.dp,
                                color = AppTheme.colors.black,
                                shape = AppTheme.shapes.small,
                            ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(offers, key = { it.id }) { offer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                RadioButton(
                                    selected = offer.id == selectedNotificationId,
                                    onClick = { onOfferSelected(offer.id) },
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = offer.sellerEmail,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = buildString {
                                            append(offer.message ?: "${offer.sellerEmail} has this card")
                                            append(" | Price: ")
                                            append(formatEuroPrice(offer.price))
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState("Close"),
                            buttonType = AppButtonTypes.Small,
                            onClick = onDismiss,
                        ),
                    )
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState("View profile"),
                            buttonType = AppButtonTypes.Small,
                            enabled = selectedNotificationId != null,
                            onClick = onViewProfile,
                        ),
                    )
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState("Write message"),
                            buttonType = AppButtonTypes.Small,
                            enabled = selectedNotificationId != null,
                            onClick = onMessageSeller,
                        ),
                    )
                }
            }
        }
    }
}
