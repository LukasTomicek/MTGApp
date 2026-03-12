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
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.TextState
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
    val displayMode = uiState.data.displayMode
    val visibleSearchResults = when (displayMode) {
        MarketPlaceDisplayMode.SELL -> uiState.data.sellSearchResults
        MarketPlaceDisplayMode.BUY -> uiState.data.buySearchResults
    }
    val visibleRecentCards = when (displayMode) {
        MarketPlaceDisplayMode.SELL -> uiState.data.recentSellCards
        MarketPlaceDisplayMode.BUY -> uiState.data.recentBuyCards
    }
    val searchEmptyText = when (displayMode) {
        MarketPlaceDisplayMode.SELL -> "No marketplace offers found"
        MarketPlaceDisplayMode.BUY -> "No wanted cards found"
    }
    val recentTitle = when (displayMode) {
        MarketPlaceDisplayMode.SELL -> "Recent sell list additions"
        MarketPlaceDisplayMode.BUY -> "Recent buy list additions"
    }
    val switchButtonTitle = when (displayMode) {
        MarketPlaceDisplayMode.SELL -> "See wanted cards"
        MarketPlaceDisplayMode.BUY -> "See sell offers"
    }
    val pricePrefix = when (displayMode) {
        MarketPlaceDisplayMode.SELL -> "From"
        MarketPlaceDisplayMode.BUY -> "Up to"
    }

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
            label = "Search card",
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
                        if (visibleSearchResults.isEmpty()) {
                            item {
                                Text(
                                    text = searchEmptyText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }

                        items(
                            items = visibleSearchResults,
                            key = {
                                val prefix = if (displayMode == MarketPlaceDisplayMode.SELL) "sell" else "buy"
                                "$prefix-${it.cardId}"
                            },
                        ) { card ->
                            MarketCardRow(
                                card = card,
                                onClick = if (displayMode == MarketPlaceDisplayMode.SELL) {
                                    {
                                        onUiEvent(
                                            MarketPlaceUiEvent.MarketCardClicked(
                                                cardId = card.cardId,
                                                cardName = card.cardName,
                                            )
                                        )
                                    }
                                } else {
                                    null
                                },
                                pricePrefix = pricePrefix,
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = recentTitle,
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
                items = visibleRecentCards,
                key = { index, card ->
                    val prefix = if (displayMode == MarketPlaceDisplayMode.SELL) "sell" else "buy"
                    "$prefix-${card.cardId}-${card.cardName}-$index"
                },
            ) { _, card ->
                MarketCardColumn(
                    card = card,
                    onClick = if (displayMode == MarketPlaceDisplayMode.SELL) {
                        {
                            onUiEvent(
                                MarketPlaceUiEvent.MarketCardClicked(
                                    cardId = card.cardId,
                                    cardName = card.cardName,
                                )
                            )
                        }
                    } else {
                        null
                    },
                    pricePrefix = pricePrefix,
                )
            }
        }

        AppButton(
            modifier = Modifier.fillMaxWidth(),
            state = AppButtonState(
                title = TextState(switchButtonTitle),
                enabled = !uiState.isLoading,
                onClick = { onUiEvent(MarketPlaceUiEvent.ToggleDisplayModeClicked) },
            ),
        )

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
