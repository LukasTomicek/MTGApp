package mtg.app.feature.map.presentation.utils.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.TextState
import mtg.app.feature.map.presentation.map.MapPin

@Composable
fun MapPinsRow(
    pins: List<MapPin>,
    selectedPinId: String?,
    onPinClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        pins.forEachIndexed { index, pin ->
            val isSelected = pin.id == selectedPinId
            AppButton(
                state = AppButtonState(
                    title = TextState(if (isSelected) "Pin ${index + 1} *" else "Pin ${index + 1}"),
                    onClick = { onPinClick(pin.id) },
                ),
            )
        }
    }
}
