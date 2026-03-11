package mtg.app.feature.welcome.presentation.welcome

import mtg.app.core.presentation.state.UiState
import mtg.app.feature.welcome.presentation.utils.WelcomeNavButtons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    uiState: UiState<WelcomeScreenState>,
    onUiEvent: (WelcomeUiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "In next steps you will see:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(text = "1. Map guide with pins and radius where you can setup your location")
                    Text(text = "2. Trade guide for buy/sell lists and notifications")
                    Text(text = "3. Setup profile with your nickname")
                }
            }
        }

        WelcomeNavButtons(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
                .padding(vertical = 16.dp),
            isBackHidden = true,
            continueLabel = "Continue",
            continueEnabled = true,
            onBackClicked = {},
            onContinueClicked = { onUiEvent(WelcomeUiEvent.ContinueClicked) },
        )
    }
}
