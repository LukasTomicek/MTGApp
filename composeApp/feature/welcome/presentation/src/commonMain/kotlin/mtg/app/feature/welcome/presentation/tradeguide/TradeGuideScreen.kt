package mtg.app.feature.welcome.presentation.tradeguide

import mtg.app.core.presentation.state.UiState
import mtg.app.feature.welcome.presentation.utils.GuideCard
import mtg.app.feature.welcome.presentation.utils.WelcomeNavButtons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TradeGuideScreen(
    uiState: UiState<TradeGuideScreenState>,
    onUiEvent: (TradeGuideUiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 44.dp)
                .padding(bottom = 110.dp),
        ) {
            Text(
                modifier = Modifier.padding(bottom = 24.dp),
                text = uiState.data.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                modifier = Modifier.padding(bottom = 12.dp),
                text = uiState.data.subtitle,
                style = MaterialTheme.typography.bodyLarge,
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Your collection",
                body = "Add the cards you own to your collection. You can also import or export your collection, and quickly move cards to your sell list.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Buy cards",
                body = "Add cards you want to buy. When you add a card, players nearby who have it in their sell list will get a notification.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Sell cards",
                body = "Add cards you want to sell. Players nearby who are looking for those cards in their buy list will be notified.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Notifications",
                body = "You'll get a notification whenever a nearby player wants a card you sell, or is selling a card you want in your selected distance. This helps you trade faster.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Marketplace",
                body = "Browse all sell offers from other players. You can filter by distance or other parameters to find the best deals.",
            )
        }

        WelcomeNavButtons(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
                .padding(vertical = 16.dp),
            isBackHidden = false,
            continueLabel = "Continue",
            onBackClicked = { onUiEvent(TradeGuideUiEvent.BackClicked) },
            onContinueClicked = { onUiEvent(TradeGuideUiEvent.ContinueClicked) },
        )
    }
}
