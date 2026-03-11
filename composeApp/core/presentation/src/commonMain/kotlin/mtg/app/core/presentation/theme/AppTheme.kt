package mtg.app.core.presentation.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = remember { Colors() }
    val shapes = remember { Shapes() }
    val typography = remember { appTypography }

    val textSelectionColors = TextSelectionColors(
        handleColor = colors.primary500,
        backgroundColor = colors.primary100,
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = colors.primary500,
            secondary = colors.secondary,
            error = colors.error,
            background = colors.white,
            surface = colors.white,
            onPrimary = colors.white,
            onSecondary = colors.black,
            onError = colors.white,
            onBackground = colors.black,
            onSurface = colors.black,
        ),
        typography = MaterialTheme.typography,
        shapes = androidx.compose.material3.Shapes(
            extraSmall = shapes.extraSmall,
            small = shapes.small,
            medium = shapes.medium,
            large = shapes.large,
            extraLarge = shapes.extraLarge,
        ),
    ) {
        CompositionLocalProvider(
            LocalColors provides colors,
            LocalTypography provides typography,
            LocalShapes provides shapes,
            LocalTextSelectionColors provides textSelectionColors,
            LocalIndication provides ripple(color = colors.primary500),
        ) {
            content()
        }
    }
}
