package mtg.app.feature.settings.presentation.termsofuse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TermsOfUseScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Terms of Use",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Use this application responsibly when creating offers, buy requests and messages.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Users are responsible for correctness of listed cards, prices and condition details.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Abuse, spam and misleading trade offers are not allowed.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "The app facilitates contact between users and does not guarantee trade completion.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
