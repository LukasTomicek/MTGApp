package mtg.app.feature.map.presentation.utils.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import mtg.app.core.presentation.theme.AppTheme
import mtg.app.feature.map.presentation.map.MapCoordinate

@Composable
fun MapRadiusAndLocationSection(
    radiusMeters: Float,
    userLocation: MapCoordinate?,
    selectedPinLocation: MapCoordinate?,
    onRadiusChanged: (Float) -> Unit,
) {
    Text(
        text = "Radius: ${radiusMeters.toInt()} m"
    )

    Slider(
        value = radiusMeters,
        onValueChange = onRadiusChanged,
        valueRange = 1000f..20_000f,
        colors = SliderDefaults.colors(
            thumbColor = AppTheme.colors.primary500,
            activeTrackColor = AppTheme.colors.primary500,
            inactiveTrackColor = AppTheme.colors.primary200,
            activeTickColor = AppTheme.colors.primary700,
            inactiveTickColor = AppTheme.colors.gray300,
        ),
    )

    Text(
        text = "My location: ${userLocation.toUiText()}",
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        text = "Selected pin: ${selectedPinLocation.toUiText()}",
        style = MaterialTheme.typography.bodySmall,
    )
}

private fun MapCoordinate?.toUiText(): String {
    if (this == null) return "not available"
    return "${latitude.formatCoordinate()}, ${longitude.formatCoordinate()}"
}

private fun Double.formatCoordinate(): String {
    return kotlin.math.round(this * 100000.0).div(100000.0).toString()
}
