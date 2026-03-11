package mtg.app.core.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class Colors(
    val primary100: Color = Color(0xFFD8E4FF), //Royal blue
    val primary200: Color = Color(0xFFADC8FF),
    val primary300: Color = Color(0xFF84A9FF),
    val primary400: Color = Color(0xFF6690FF),
    val primary500: Color = Color(0xFF3366FF),
    val primary600: Color = Color(0xFF254EDB),
    val primary700: Color = Color(0xFF1939B7),
    val primary800: Color = Color(0xFF102693),
    val primary900: Color = Color(0xFF091A7A),

    val gray100: Color = Color(0xFFF5F6F8),
    val gray200: Color = Color(0xFFE6E8EC),
    val gray300: Color = Color(0xFFD1D5DB),
    val gray400: Color = Color(0xFF9CA3AF),
    val gray500: Color = Color(0xFF6B7280),
    val gray600: Color = Color(0xFF4B5563),
    val gray700: Color = Color(0xFF374151),
    val gray800: Color = Color(0xFF1F2937),
    val gray900: Color = Color(0xFF111827),

    val red100: Color = Color(0xFFFFE5E5),
    val red200: Color = Color(0xFFFFBDBD),
    val red300: Color = Color(0xFFFF8A8A),
    val red400: Color = Color(0xFFFF5C5C),
    val red500: Color = Color(0xFFD32F2F),
    val red600: Color = Color(0xFFB71C1C),
    val red700: Color = Color(0xFF991B1B),
    val red800: Color = Color(0xFF7F1D1D),
    val red900: Color = Color(0xFF5F1515),


    val secondary: Color = Color(0xFF00B8D9),
    val success: Color = Color(0xFF1B5E20),
    val warning: Color = Color(0xFFE65100),
    val error: Color = Color(0xFFD32F2F),


    val white: Color = Color(0xFFFFFFFF),
    val black: Color = Color(0xFF000000),
    val transparent: Color = Color(0x00000000),
)

internal val LocalColors = staticCompositionLocalOf<Colors> { error("No colors defined") }
