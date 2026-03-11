package mtg.app.feature.trade.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberCollectionCsvPicker(
    onFileLoaded: (fileName: String, content: String) -> Unit,
    onProgress: (Float) -> Unit,
    onFailure: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope, onFileLoaded, onProgress, onFailure) {
        {
            scope.launch {
                val file = FileKit.openFilePicker(
                    type = FileKitType.File(extensions = listOf("csv")),
                    title = "Select CSV file",
                )
                if (file == null) {
                    onFailure()
                    return@launch
                }

                onProgress(0f)
                val content = runCatching { file.readUtf8WithProgress(context, onProgress) }.getOrNull()
                if (content.isNullOrBlank()) {
                    onFailure()
                    return@launch
                }
                onProgress(1f)
                onFileLoaded(file.name.ifBlank { "selected.csv" }, content)
            }
        }
    }
}

private fun io.github.vinceglb.filekit.PlatformFile.readUtf8WithProgress(
    context: android.content.Context,
    onProgress: (Float) -> Unit,
): String {
    val total = size().takeIf { it > 0L }
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var loaded = 0L

    val inputStream = when (val wrapped = androidFile) {
        is AndroidFile.FileWrapper -> wrapped.file.inputStream()
        is AndroidFile.UriWrapper -> context.contentResolver.openInputStream(wrapped.uri)
    } ?: return ""

    inputStream.use { input ->
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            output.write(buffer, 0, read)
            loaded += read
            if (total != null) {
                onProgress((loaded.toFloat() / total.toFloat()).coerceIn(0f, 1f))
            }
        }
    }

    // Always finish at 100% even when reported file size is inaccurate.
    onProgress(1f)
    return output.toByteArray().decodeToString()
}
