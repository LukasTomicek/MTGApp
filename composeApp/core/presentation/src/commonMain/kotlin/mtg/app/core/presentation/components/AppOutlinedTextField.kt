package mtg.app.core.presentation.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mtg.app.core.presentation.theme.AppTheme

@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        enabled = enabled,
        label = { Text(label) },
        colors = appOutlinedTextFieldColors(),
    )
}

@Composable
fun appOutlinedTextFieldColors(): TextFieldColors {
    val colors = AppTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.black,
        unfocusedTextColor = colors.black,
        disabledTextColor = colors.gray600,
        focusedBorderColor = colors.black,
        unfocusedBorderColor = colors.gray500,
        disabledBorderColor = colors.gray400.copy(alpha = 0.5f),
        focusedLabelColor = colors.black,
        unfocusedLabelColor = colors.gray700,
        disabledLabelColor = colors.gray500,
        cursorColor = colors.black,
        focusedContainerColor = colors.white,
        unfocusedContainerColor = colors.white,
        disabledContainerColor = colors.gray100,
        errorBorderColor = colors.black,
        errorLabelColor = colors.black,
        errorCursorColor = colors.black,
    )
}
