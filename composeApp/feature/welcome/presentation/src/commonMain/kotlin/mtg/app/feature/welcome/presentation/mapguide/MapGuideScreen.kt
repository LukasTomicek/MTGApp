package mtg.app.feature.welcome.presentation.mapguide

import mtg.app.core.presentation.state.UiState
import mtg.app.feature.welcome.presentation.utils.WelcomeNavButtons
import mtg.app.feature.welcome.presentation.rememberCurrentLocationRequester
import mtg.app.feature.welcome.presentation.utils.GuideCard
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.TextState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MapGuideScreen(
    uiState: UiState<MapGuideScreenState>,
    onUiEvent: (MapGuideUiEvent) -> Unit,
) {
    val requestCurrentLocation = rememberCurrentLocationRequester { location ->
        onUiEvent(MapGuideUiEvent.CurrentLocationResolved(location))
    }

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
                text = "Map Guide",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Trade locally",
                body = "This system helps you find players nearby and avoid shipping. Meet locally and trade cards faster.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Add a location",
                body = "Long press anywhere on the map to place a pin. A pin marks a location where you want to trade cards.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Use multiple locations",
                body = "You can add multiple pins for different places like home, work, or your local game store. Pins can also be removed anytime.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Why radius is important",
                body = "You will only see offers from players whose trading zones overlap with yours. When zones intersect, you'll get notifications about their offers.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "No pin selected",
                body = "If you don’t place any pins, your location is considered everywhere, so you can see all offers.",
            )

            GuideCard(
                modifier = Modifier.padding(bottom = 12.dp),
                title = "Add a location now",
                body = "Use the button below to quickly create a pin at your current location. You can also skip this and add locations later.",
            )


            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (uiState.data.infoMessage.isNotBlank()) { 4.dp } else { 24.dp }),
                state = AppButtonState(
                    title = TextState("Create a pin at my current location"),
                    onClick = {
                        onUiEvent(MapGuideUiEvent.SetCurrentLocationAsDefaultPinClicked)
                        requestCurrentLocation()
                    },
                ),
            )

            if (uiState.data.infoMessage.isNotBlank()) {
                Text(
                    text = uiState.data.infoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
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
            onBackClicked = { onUiEvent(MapGuideUiEvent.BackClicked) },
            onContinueClicked = { onUiEvent(MapGuideUiEvent.ContinueClicked) },
        )
    }
}
