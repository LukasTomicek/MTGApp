package mtg.app.feature.map.presentation.utils.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.TextState

@Composable
fun MapActionButtonsRow(
    isPinSelected: Boolean,
    onCenterOnUserClick: () -> Unit,
    onCenterOnPinClick: () -> Unit,
    onRemovePinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppButton(
            modifier = Modifier.weight(1f),
            state = AppButtonState(
                title = TextState("Center on me"),
                onClick = onCenterOnUserClick,
            ),
        )

        AppButton(
            modifier = Modifier.weight(1f),
            state = AppButtonState(
                title = TextState("Center on pin"),
                enabled = isPinSelected,
                onClick = onCenterOnPinClick,
            ),
        )

        AppButton(
            modifier = Modifier.weight(1f),
            state = AppButtonState(
                title = TextState("Remove pin"),
                enabled = isPinSelected,
                onClick = onRemovePinClick,
            ),
        )
    }
}
