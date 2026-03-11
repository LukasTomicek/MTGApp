package mtg.app.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.theme.AppTheme

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    state: AppButtonState,
) {
    ButtonContent(
        state = state,
        modifier = modifier,
    )
}

@Composable
private fun ButtonContent(
    modifier: Modifier = Modifier,
    state: AppButtonState,
) {
    val cornerShape: Dp = when (state.buttonType) {
        is AppButtonTypes.Small -> 10.dp
        is AppButtonTypes.Medium -> 12.dp
        is AppButtonTypes.Large -> 16.dp
        is AppButtonTypes.RoundedSmall -> 16.dp
        is AppButtonTypes.RoundedMedium -> 20.dp
        is AppButtonTypes.RoundedLarge -> 24.dp
    }

    val imageSize: Dp = when (state.buttonType) {
        is AppButtonTypes.Small, AppButtonTypes.RoundedSmall -> 16.dp
        is AppButtonTypes.Medium, AppButtonTypes.RoundedMedium, AppButtonTypes.Large, AppButtonTypes.RoundedLarge -> 20.dp
    }

    var textWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val maxRowWidth = with(density) {
        val centerWidth = if (state.centerIcon != null) imageSize else textWidth.toDp()
        centerWidth +
                (if (state.leftIcon != null) imageSize else 0.dp) +
                (if (state.rightIcon != null) imageSize else 0.dp) +
                (32 * 2).dp +
                (8 * 2).dp // mezery mezi elementy
    }

    val verticalPadding: Dp = when (state.buttonType) {
        is AppButtonTypes.Small, AppButtonTypes.RoundedSmall -> 8.dp
        is AppButtonTypes.Medium, AppButtonTypes.RoundedMedium -> 9.5.dp
        is AppButtonTypes.Large, AppButtonTypes.RoundedLarge -> 12.dp
    }
    val horizontalPadding: Dp = when (state.buttonType) {
        is AppButtonTypes.Small, AppButtonTypes.RoundedSmall -> 16.dp
        is AppButtonTypes.Medium, AppButtonTypes.RoundedMedium -> 24.dp
        is AppButtonTypes.Large, AppButtonTypes.RoundedLarge -> 32.dp
    }


    Row(
        modifier = modifier
            .testTag(state.title.testTag)
            .clip(shape = RoundedCornerShape(size = cornerShape))
            .clickable(
                interactionSource = null,
                enabled = state.enabled,
                onClick = state.onClick,
            )
            .border(
                BorderStroke(width = 1.dp, color =state.buttonColors.border),
                shape = RoundedCornerShape(size = cornerShape)
            )
            .background(color = state.buttonColors.background)
            .then(
                if (state.isLimitedSize) {
                    Modifier.widthIn(max = maxRowWidth)
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.customContent?.let { customContent ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = customContent,
            )
            return@Row
        }

        if (state.leftIcon != null) {
            Spacer(
                modifier = Modifier
                    .weight(1f),
            )
            Image(
                modifier = Modifier
                    .size(imageSize),
                painter = state.leftIcon,
                colorFilter = ColorFilter.tint(state.buttonColors.text),
                contentDescription = null,
            )
        } else {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = horizontalPadding
                    ),
            )
        }

        if (state.centerIcon != null) {
            Image(
                modifier = Modifier
                    .padding(
                        vertical = verticalPadding,
                        horizontal = 8.dp,
                    )
                    .size(imageSize),
                painter = state.centerIcon,
                colorFilter = ColorFilter.tint(state.buttonColors.text),
                contentDescription = null,
            )
        } else {
            Text(
                modifier = Modifier
                    .padding(
                        vertical = verticalPadding,
                        horizontal = 8.dp,
                    )
                    .onGloballyPositioned { coordinates ->
                        textWidth = coordinates.size.width
                    },
                text = state.title.text,
                color = state.buttonColors.text,
                style = state.textStyle,
                maxLines = state.maxLineSize,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        if (state.rightIcon != null) {
            Image(
                modifier = Modifier
                    .size(imageSize),
                painter = state.rightIcon,
                colorFilter = ColorFilter.tint(state.buttonColors.text),
                contentDescription = null,
            )
            Spacer(
                modifier = Modifier
                    .weight(1f),
            )
        } else {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        end = horizontalPadding
                    ),
            )
        }
    }
}

data class AppButtonState(
    val title: TextState = TextState(""),
    val leftIcon: Painter? = null,
    val rightIcon: Painter? = null,
    val centerIcon: Painter? = null,
    val colors: AppButtonColors = AppButtonColors.Primary,
    val buttonType: AppButtonTypes = AppButtonTypes.Medium,
    val enabled: Boolean = true,
    val isLimitedSize: Boolean = true,
    val maxLineSize: Int = 1,
    val onClick: () -> Unit = {},
    val customContent: (@Composable RowScope.() -> Unit)? = null,
) {

    val buttonColors: ButtonColors
        @Composable
        get() = resolveAppButtonColors(colors, enabled)

    val textStyle: TextStyle
        @Composable
        get() = when (buttonType) {
            is AppButtonTypes.Small, AppButtonTypes.RoundedSmall -> MaterialTheme.typography.labelMedium
            is AppButtonTypes.Medium, AppButtonTypes.RoundedMedium -> MaterialTheme.typography.labelLarge
            is AppButtonTypes.Large, AppButtonTypes.RoundedLarge -> MaterialTheme.typography.titleSmall
        }

}

@Composable
private fun resolveAppButtonColors(
    appButtonColors: AppButtonColors,
    enabled: Boolean,
): ButtonColors {
    val colors = AppTheme.colors
    val baseColors = when (appButtonColors) {
        AppButtonColors.Primary -> ButtonColors(colors.gray600, colors.black, colors.white)
        AppButtonColors.Secondary -> ButtonColors(colors.white, colors.primary500, colors.primary500)
        AppButtonColors.Tertiary -> ButtonColors(colors.white, colors.white, colors.primary500)
        AppButtonColors.ErrorPrimary -> ButtonColors(colors.red500, colors.red500, colors.white)
        AppButtonColors.ErrorSecondary -> ButtonColors(colors.white, colors.red500, colors.red500)
        AppButtonColors.ErrorTertiary -> ButtonColors(colors.white, colors.white, colors.red500)
        AppButtonColors.SuccessPrimary -> ButtonColors(colors.success, colors.success, colors.white)
        AppButtonColors.SuccessSecondary -> ButtonColors(colors.white, colors.success, colors.success)
        AppButtonColors.SuccessTertiary -> ButtonColors(colors.white, colors.white, colors.success)
        is AppButtonColors.Custom -> ButtonColors(
            background = appButtonColors.background,
            border = appButtonColors.border,
            text = appButtonColors.text,
        )
    }
    if (enabled) return baseColors
    return baseColors.copy(
        background = baseColors.background.copy(alpha = 0.5f),
        border = baseColors.border.copy(alpha = 0.5f),
        text = baseColors.text.copy(alpha = 0.5f),
    )
}

data class TextState(
    val text: String,
    val testTag: String = "",
)

data class ButtonColors(
    val background: Color,
    val border: Color,
    val text: Color
)

sealed class AppButtonColors {
    object Primary : AppButtonColors()
    object Secondary : AppButtonColors()
    object Tertiary : AppButtonColors()
    object ErrorPrimary : AppButtonColors()
    object ErrorSecondary : AppButtonColors()
    object ErrorTertiary : AppButtonColors()
    object SuccessPrimary : AppButtonColors()
    object SuccessSecondary : AppButtonColors()
    object SuccessTertiary : AppButtonColors()

    data class Custom(
        val background: Color,
        val border: Color,
        val text: Color,
    ) : AppButtonColors()
}

sealed class AppButtonTypes {
    object Small : AppButtonTypes()
    object Medium : AppButtonTypes()
    object Large : AppButtonTypes()
    object RoundedSmall : AppButtonTypes()
    object RoundedMedium : AppButtonTypes()
    object RoundedLarge : AppButtonTypes()
}
