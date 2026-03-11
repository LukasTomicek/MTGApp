package mtg.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.theme.AppTheme

@Composable
fun AppAlertDialog(
    confirmText: String,
    dismissText: String,
    title: String? = null,
    message: String? = null,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.medium)
                .background(color = AppTheme.colors.gray100)
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.black,
                    shape = AppTheme.shapes.small
                ),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState(dismissText),
                            buttonType = AppButtonTypes.Small,
                            onClick = onDismissClick,
                        ),
                    )
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState(confirmText),
                            buttonType = AppButtonTypes.Small,
                            onClick = onConfirmClick,
                        ),
                    )
                }
            }
        }
    }
}
