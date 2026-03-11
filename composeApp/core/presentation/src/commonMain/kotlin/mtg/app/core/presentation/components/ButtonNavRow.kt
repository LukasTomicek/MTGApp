package mtg.app.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

@Composable
fun ButtonNavRow(
    title: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    AppButton(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        state = AppButtonState(
            title = TextState(title),
            leftIcon = rememberVectorPainter(leadingIcon),
            rightIcon = rememberVectorPainter(Icons.AutoMirrored.Filled.KeyboardArrowRight),
            buttonType = AppButtonTypes.RoundedMedium,
            isLimitedSize = false,
            enabled = enabled,
            onClick = onClick,
            customContent = null,
        ),
    )
}
