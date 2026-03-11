package mtg.app.core.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject

@Composable
fun MainApplication(
    defaultApplication: @Composable () -> Unit,
) {
    val viewModel = koinInject<MainViewModel>()
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onUiEvent(MainUiEvent.Initialize)
    }

    if (!uiState.data.initialized) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    defaultApplication()
}
