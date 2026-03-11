package mtg.app.feature.trade.presentation.utils.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

@Composable
fun ImportModal(
    csvFileLoadProgress: Float?,
    importProgress: Float?,
    syncProgress: Float?,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()

                val fileProgress = csvFileLoadProgress?.coerceIn(0f, 1f)
                val impProgress = importProgress?.coerceIn(0f, 1f)
                val sync = syncProgress?.coerceIn(0f, 1f)
                val progress = fileProgress ?: impProgress ?: sync

                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val label = when {
                        fileProgress != null -> "File load"
                        impProgress != null -> "Import progress"
                        else -> "Delete progress"
                    }
                    Text(
                        text = "$label ${formatProgressPercent(progress)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (impProgress != null) {
                        Text(
                            text = "This can take a while",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun formatProgressPercent(progress: Float): String {
    val oneDecimal = ((progress.coerceIn(0f, 1f) * 1000f).roundToInt()) / 10f
    return if (oneDecimal % 1f == 0f) {
        oneDecimal.toInt().toString()
    } else {
        oneDecimal.toString()
    }
}
