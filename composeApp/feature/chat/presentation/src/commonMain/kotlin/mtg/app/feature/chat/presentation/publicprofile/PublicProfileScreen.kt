package mtg.app.feature.chat.presentation.publicprofile

import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.AppButtonTypes
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.presentation.utils.component.MarketCardRow

@Composable
fun PublicProfileScreen(
    uid: String,
    uiState: UiState<PublicProfileScreenState>,
    onUiEvent: (PublicProfileUiEvent) -> Unit,
) {
    LaunchedEffect(uid) {
        onUiEvent(PublicProfileUiEvent.ScreenOpened(uid))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = uiState.data.nickname.ifBlank { uiState.data.targetUid },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = buildString {
                append("Rating: ")
                if (uiState.data.ratingCount > 0) {
                    append(uiState.data.ratingAverage.toOneDecimalString())
                    append(" (")
                    append(uiState.data.ratingCount)
                    append(")")
                } else {
                    append("-")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
        )

        uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            AppButton(
                state = AppButtonState(
                    title = TextState("Retry"),
                    buttonType = AppButtonTypes.Small,
                    enabled = !uiState.isLoading,
                    onClick = { onUiEvent(PublicProfileUiEvent.RetryClicked) },
                ),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "sell_offers_header") {
                Text(
                    text = "Sell offers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (uiState.data.sellOffers.isEmpty()) {
                item(key = "sell_offers_empty") {
                    Text(
                        text = if (uiState.isLoading) "Loading offers..." else "No offers in sell list",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(uiState.data.sellOffers, key = { "${it.cardId}_${it.cardName}" }) { offer ->
                    MarketCardRow(
                        card = MarketPlaceCard(
                            cardId = offer.cardId,
                            cardName = offer.cardName,
                            cardTypeLine = offer.cardTypeLine,
                            imageUrl = offer.imageUrl,
                            offerCount = offer.offerCount,
                            fromPrice = offer.fromPrice,
                        ),
                        onClick = { },
                    )
                }
            }

            item(key = "reviews_header") {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (uiState.data.reviews.isEmpty()) {
                item(key = "reviews_empty") {
                    Text(
                        text = if (uiState.isLoading) "Loading reviews..." else "No reviews yet",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(uiState.data.reviews, key = { "${it.raterUid}_${it.createdAt}_${it.score}" }) { review ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Score: ${review.score}/5",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            if (review.comment.isNotBlank()) {
                                Text(
                                    text = review.comment,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.toOneDecimalString(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return rounded.toString()
}
