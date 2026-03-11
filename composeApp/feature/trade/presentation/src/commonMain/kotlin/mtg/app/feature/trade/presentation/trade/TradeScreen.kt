package mtg.app.feature.trade.presentation.trade

import mtg.app.core.presentation.components.ButtonNavRow
import mtg.app.core.presentation.state.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TradeScreen(
    uiState: UiState<TradeScreenState>,
    onUiEvent: (TradeUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ButtonNavRow(
                title = "MarketPlace",
                leadingIcon = Icons.Filled.Storefront,
                onClick = { onUiEvent(TradeUiEvent.MarketPlaceClicked) },
            )
            ButtonNavRow(
                title = "Collection",
                leadingIcon = Icons.Filled.Collections,
                onClick = { onUiEvent(TradeUiEvent.CollectionClicked) },
            )
            ButtonNavRow(
                title = "Buy",
                leadingIcon = Icons.Filled.ShoppingCart,
                onClick = { onUiEvent(TradeUiEvent.BuyListClicked) },
            )
            ButtonNavRow(
                title = "Sell",
                leadingIcon = Icons.Filled.LocalOffer,
                onClick = { onUiEvent(TradeUiEvent.SellListClicked) },
            )
        }
    }
}
