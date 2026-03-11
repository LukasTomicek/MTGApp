package mtg.app.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextInputRow(
    modifier: Modifier = Modifier,
    query: String,
    label: String,
    buttonText: String = "Search",
    enabled: Boolean = true,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        AppOutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = label,
            modifier = Modifier.weight(1f),
        )

        AppButton(
            modifier = Modifier.height(60.dp),
            state = AppButtonState(
                title = TextState(buttonText),
                enabled = enabled,
                onClick = onSearchClick,
            ),
        )
    }
}
