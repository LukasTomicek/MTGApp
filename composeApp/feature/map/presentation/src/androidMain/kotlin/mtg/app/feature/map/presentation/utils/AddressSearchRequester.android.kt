package mtg.app.feature.map.presentation.utils

import android.location.Geocoder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mtg.app.feature.map.presentation.map.MapCoordinate
import java.util.Locale

@Composable
actual fun rememberAddressSearchRequester(
    onResult: (MapCoordinate?) -> Unit,
): (String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return remember(context, scope, onResult) {
        { query ->
            val trimmed = query.trim()
            if (trimmed.isBlank()) {
                onResult(null)
            } else {
                scope.launch {
                    val coordinate = withContext(Dispatchers.IO) {
                        runCatching {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val results = geocoder.getFromLocationName(trimmed, 1)
                            val first = results?.firstOrNull() ?: return@runCatching null
                            MapCoordinate(latitude = first.latitude, longitude = first.longitude)
                        }.getOrNull()
                    }
                    onResult(coordinate)
                }
            }
        }
    }
}
