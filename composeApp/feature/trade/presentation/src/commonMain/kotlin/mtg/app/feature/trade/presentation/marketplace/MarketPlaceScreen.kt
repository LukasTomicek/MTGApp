package mtg.app.feature.trade.presentation.marketplace

import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.TextInputRow
import mtg.app.feature.map.presentation.utils.rememberCurrentLocationRequester
import mtg.app.feature.trade.presentation.utils.component.MarketCardRow
import mtg.app.feature.trade.presentation.utils.component.MarketCardColumn
import mtg.app.feature.trade.presentation.utils.component.ChooseSellerModal
import mtg.app.feature.trade.presentation.utils.component.PinRecommendationDialog

@Composable
fun MarketPlaceScreen(
    uiState: UiState<MarketPlaceScreenState>,
    onUiEvent: (MarketPlaceUiEvent) -> Unit,
) {
    val requestCurrentLocation = rememberCurrentLocationRequester { location ->
        onUiEvent(MarketPlaceUiEvent.CurrentLocationResolved(location))
    }

    LaunchedEffect(Unit) {
        onUiEvent(MarketPlaceUiEvent.ScreenOpened)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextInputRow(
            query = uiState.data.searchQuery,
            label = "Search card in marketplace",
            onQueryChange = { onUiEvent(MarketPlaceUiEvent.SearchChanged(it)) },
            onSearchClick = { onUiEvent(MarketPlaceUiEvent.SearchSubmitted) },
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (uiState.data.searchResults.isEmpty()) {
                            item {
                                Text(
                                    text = "No marketplace offers found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }

                        items(uiState.data.searchResults, key = { it.cardId }) { card ->
                            MarketCardRow(
                                card = card,
                                onClick = {
                                    onUiEvent(
                                        MarketPlaceUiEvent.MarketCardClicked(
                                            cardId = card.cardId,
                                            cardName = card.cardName,
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Recent sell list additions",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(
                items = uiState.data.recentCards,
                key = { index, card -> "${card.cardId}-${card.cardName}-$index" },
            ) { _, card ->
                MarketCardColumn(
                    card = card,
                    onClick = {
                        onUiEvent(
                            MarketPlaceUiEvent.MarketCardClicked(
                                cardId = card.cardId,
                                cardName = card.cardName,
                            )
                        )
                    },
                )
            }
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (uiState.data.showSellersDialog) {
        ChooseSellerModal(
            selectedCardName = uiState.data.selectedCardName,
            sellers = uiState.data.sellersForSelectedCard,
            selectedSellerUid = uiState.data.selectedSellerUid,
            onSellerSelected = { onUiEvent(MarketPlaceUiEvent.SellerSelected(it)) },
            onDismiss = { onUiEvent(MarketPlaceUiEvent.SellerDialogDismissed) },
            onViewProfile = { onUiEvent(MarketPlaceUiEvent.ViewSellerProfileClicked) },
            onMessageSeller = { onUiEvent(MarketPlaceUiEvent.MessageSellerClicked) },
        )
    }

    if (uiState.data.showPinRecommendationDialog) {
        PinRecommendationDialog(
            onDismiss = { onUiEvent(MarketPlaceUiEvent.DismissPinRecommendationClicked) },
            onAddPinNow = requestCurrentLocation,
        )
    }
}
