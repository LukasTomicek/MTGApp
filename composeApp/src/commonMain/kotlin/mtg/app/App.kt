package mtg.app

import mtg.app.core.di.MtgApplication
import mtg.app.core.di.initKoin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import mtg.app.core.presentation.theme.AppTheme

@Composable
@Preview
fun App() {
    remember { initKoin() }

    AppTheme {
        MtgApplication()
    }
}
