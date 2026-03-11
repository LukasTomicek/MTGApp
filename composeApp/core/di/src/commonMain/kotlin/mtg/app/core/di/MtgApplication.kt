package mtg.app.core.di

import mtg.app.core.presentation.MainApplication
import androidx.compose.runtime.Composable

@Composable
fun MtgApplication() {
    MainApplication(
        defaultApplication = { DefaultApplication() },
    )
}
