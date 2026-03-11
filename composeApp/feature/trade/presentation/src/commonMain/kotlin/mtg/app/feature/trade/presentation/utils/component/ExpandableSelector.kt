package mtg.app.feature.trade.presentation.utils.component

import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.CardImage
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.components.appOutlinedTextFieldColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableSelector(
    modifier: Modifier = Modifier,
    label: String,
    currentId: String,
    options: List<ExpandableSelectorOption>,
    largeImageMode: Boolean = false,
    onSelect: (String) -> Unit,
) {
    val currentLabel = options.firstOrNull { it.id == currentId }?.label ?: options.firstOrNull()?.label.orEmpty()

    if (options.size <= 1) {
        OutlinedTextField(
            value = "$label: $currentLabel",
            onValueChange = {},
            enabled = false,
            modifier = modifier,
            singleLine = true,
            colors = appOutlinedTextFieldColors(),
        )
        return
    }

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        AppButton(
            modifier = Modifier.fillMaxWidth(),
            state = AppButtonState(
                title = TextState("$label: $currentLabel"),
                onClick = { expanded = true },
            ),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = if (largeImageMode) {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            } else {
                Modifier
            },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            option.imageUrl?.let {
                                CardImage(
                                    imageUrl = it,
                                    heightDp = if (largeImageMode) 126 else 46,
                                )
                            }
                            Text(option.label)
                        }
                    },
                    modifier = Modifier
                        .background(
                            if (option.id == currentId) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ),
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

data class ExpandableSelectorOption(
    val id: String,
    val label: String,
    val imageUrl: String? = null,
)
