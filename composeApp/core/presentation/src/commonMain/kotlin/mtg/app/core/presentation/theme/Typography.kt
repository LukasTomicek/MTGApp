package mtg.app.core.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class Typography(
    val bodySmall: TextStyle,
    val bodyRegular: TextStyle,
    val bodyBold: TextStyle,
    val heading1: TextStyle,
    val heading2: TextStyle,
    val heading3: TextStyle,
    val heading4: TextStyle,
    val heading5: TextStyle,
    val heading6: TextStyle,
    val buttonSmall: TextStyle,
    val buttonMedium: TextStyle,
    val buttonLarge: TextStyle,
)

val appTypography = Typography(
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        fontSize = 13.sp,
    ),
    bodyRegular = TextStyle(
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        fontSize = 16.sp,
    ),
    bodyBold = TextStyle(
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp,
        fontSize = 16.sp,
    ),
    heading1 = TextStyle(
        fontWeight = FontWeight.Bold,
        lineHeight = 46.sp,
        fontSize = 38.sp,
    ),
    heading2 = TextStyle(
        fontWeight = FontWeight.Bold,
        lineHeight = 38.sp,
        fontSize = 32.sp,
    ),
    heading3 = TextStyle(
        fontWeight = FontWeight.Bold,
        lineHeight = 30.sp,
        fontSize = 24.sp,
    ),
    heading4 = TextStyle(
        fontWeight = FontWeight.Bold,
        lineHeight = 26.sp,
        fontSize = 20.sp,
    ),
    heading5 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        fontSize = 18.sp,
    ),
    heading6 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        fontSize = 16.sp,
    ),
    buttonSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
        fontSize = 12.sp,
    ),
    buttonMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        fontSize = 14.sp,
    ),
    buttonLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp,
        fontSize = 16.sp,
    ),
)

internal val LocalTypography = staticCompositionLocalOf<Typography> { error("No typography defined") }
