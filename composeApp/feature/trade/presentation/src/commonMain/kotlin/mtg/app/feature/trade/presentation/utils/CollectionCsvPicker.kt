package mtg.app.feature.trade.presentation.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCollectionCsvPicker(
    onFileLoaded: (fileName: String, content: String) -> Unit,
    onProgress: (Float) -> Unit,
    onFailure: () -> Unit,
): () -> Unit
