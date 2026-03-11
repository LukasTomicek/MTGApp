package mtg.app.feature.welcome.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.TextState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeNavButtons(
    modifier: Modifier = Modifier,
    isBackHidden: Boolean,
    continueLabel: String,
    continueEnabled: Boolean = true,
    onBackClicked: () -> Unit,
    onContinueClicked: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isBackHidden) {
            Spacer(modifier = Modifier.size(1.dp))
        } else {
            AppButton(
                state = AppButtonState(
                    title = TextState("Back"),
                    onClick = onBackClicked,
                ),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        AppButton(
            state = AppButtonState(
                title = TextState(continueLabel),
                enabled = continueEnabled,
                onClick = onContinueClicked,
            ),
        )
    }
}
