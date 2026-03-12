package mtg.app.core.presentation.components

import mtg.app.core.presentation.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AppValueEditModal(
    currentValue: String = "",
    newPassword: String,
    confirmPassword: String,
    onCurrentValueChanged: (String) -> Unit = {},
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    showCurrentValue: Boolean = false,
    showConfirmPassword: Boolean = true,
    errorMessage: String? = null,
    isSaving: Boolean = false,
    title: String = "Change password",
    currentLabel: String = "Current password",
    primaryLabel: String = "New password",
    confirmLabel: String = "Confirm password",
    currentIsSecret: Boolean = true,
    primaryIsSecret: Boolean = true,
    confirmIsSecret: Boolean = true,
    cancelButtonLabel: String = "Cancel",
    saveButtonLabel: String = "Save",
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.medium)
                .background(AppTheme.colors.gray100),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )

                if (showCurrentValue) {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = onCurrentValueChanged,
                        label = { Text(currentLabel) },
                        singleLine = true,
                        visualTransformation = if (currentIsSecret) PasswordVisualTransformation() else VisualTransformation.None,
                        colors = appOutlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChanged,
                    label = { Text(primaryLabel) },
                    singleLine = true,
                    visualTransformation = if (primaryIsSecret) PasswordVisualTransformation() else VisualTransformation.None,
                    colors = appOutlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (showCurrentValue) 8.dp else 12.dp),
                )

                if (showConfirmPassword) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChanged,
                        label = { Text(confirmLabel) },
                        singleLine = true,
                        visualTransformation = if (confirmIsSecret) PasswordVisualTransformation() else VisualTransformation.None,
                        colors = appOutlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }

                errorMessage?.takeIf { it.isNotBlank() }?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState(cancelButtonLabel),
                            buttonType = AppButtonTypes.Small,
                            enabled = !isSaving,
                            onClick = onDismiss,
                        ),
                    )

                    AppButton(
                        modifier = Modifier.weight(1f),
                        state = AppButtonState(
                            title = TextState(saveButtonLabel),
                            buttonType = AppButtonTypes.Small,
                            enabled = !isSaving,
                            onClick = onSubmit,
                        ),
                    )
                }
            }
        }
    }
}
