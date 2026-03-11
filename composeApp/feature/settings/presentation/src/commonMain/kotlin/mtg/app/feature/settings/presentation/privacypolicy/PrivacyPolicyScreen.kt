package mtg.app.feature.settings.presentation.privacypolicy

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
fun PrivacyPolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "This app stores account and trading data required to match users and enable chat.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Stored data may include your nickname, card lists, map pins/radius, notifications and chat messages.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Location data is used only for matching users in configured trade radius.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "You can remove account data by deleting your account in Settings.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
