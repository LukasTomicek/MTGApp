package mtg.app.feature.welcome.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberCurrentLocationRequester(
    onLocationResult: (WelcomeLocation?) -> Unit,
): () -> Unit {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.any { it.value }
        if (granted) {
            onLocationResult(context.readLastKnownLocation())
        } else {
            onLocationResult(null)
        }
    }

    return remember(context, permissionLauncher, onLocationResult) {
        {
            if (context.hasLocationPermission()) {
                onLocationResult(context.readLastKnownLocation())
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        }
    }
}

private fun Context.readLastKnownLocation(): WelcomeLocation? {
    val locationManager = getSystemService(LocationManager::class.java) ?: return null

    val gps = runCatching {
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }.getOrNull()
    if (gps != null) {
        return WelcomeLocation(latitude = gps.latitude, longitude = gps.longitude)
    }

    val network = runCatching {
        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }.getOrNull()
    return network?.let { WelcomeLocation(latitude = it.latitude, longitude = it.longitude) }
}

private fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}
