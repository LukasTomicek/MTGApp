package mtg.app.feature.trade.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.launch

@Composable
actual fun rememberCollectionCsvPicker(
    onFileLoaded: (fileName: String, content: String) -> Unit,
    onProgress: (Float) -> Unit,
    onFailure: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    return remember(scope, onFileLoaded, onProgress, onFailure) {
        {
            scope.launch {
                onProgress(0f)
                val file = FileKit.openFilePicker(
                    type = FileKitType.File(extensions = listOf("csv")),
                    title = "Select CSV file",
                )
                val content = runCatching { file?.readString() }.getOrNull()
                if (content.isNullOrBlank()) {
                    onFailure()
                    return@launch
                }
                onProgress(1f)
                onFileLoaded(file?.name ?: "selected.csv", content)
            }
        }
    }
}
