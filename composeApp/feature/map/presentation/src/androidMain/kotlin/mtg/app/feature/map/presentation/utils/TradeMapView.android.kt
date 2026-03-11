package mtg.app.feature.map.presentation.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import mtg.app.feature.map.presentation.map.MapCoordinate
import mtg.app.feature.map.presentation.map.MapPin
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Composable
actual fun TradeMapView(
    modifier: Modifier,
    pins: List<MapPin>,
    selectedPinId: String?,
    radiusMeters: Float,
    centerOnUserRequestId: Int,
    centerOnPinRequestId: Int,
    onUserLocationChanged: (MapCoordinate) -> Unit,
    onPinAdded: (MapCoordinate) -> Unit,
    onPinSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    var locationPermissionGranted by remember { mutableStateOf(context.hasLocationPermission()) }
    var userLocation by remember { mutableStateOf<MapCoordinate?>(null) }
    var consumedCenterRequestId by remember { mutableIntStateOf(-1) }
    var consumedCenterPinRequestId by remember { mutableIntStateOf(-1) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        locationPermissionGranted = permissions.any { it.value }
    }

    val mapView = remember(context) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(50.0755, 14.4378))
        }
    }

    val pinMarkers = remember(mapView) { mutableMapOf<String, Marker>() }
    val pinRadius = remember(mapView) {
        Polygon(mapView).apply {
            fillColor = 0x204285F4
            strokeColor = 0xFF4285F4.toInt()
            strokeWidth = 2f
        }
    }
    val userMarker = remember(mapView) {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "You"
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    DisposableEffect(mapView) {
        val tapOverlay = MapEventsOverlay(
            object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    val point = p ?: return false
                    val coordinate = MapCoordinate(point.latitude, point.longitude)
                    onPinAdded(coordinate)
                    return true
                }
            }
        )

        mapView.overlays.add(tapOverlay)

        onDispose {
            mapView.overlays.remove(tapOverlay)
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
        }
    }

    AndroidLocationObserver(
        enabled = locationPermissionGranted,
        onLocationChanged = { coordinate ->
            userLocation = coordinate
            onUserLocationChanged(coordinate)

            userMarker.position = GeoPoint(coordinate.latitude, coordinate.longitude)
            if (!mapView.overlays.contains(userMarker)) {
                mapView.overlays.add(userMarker)
            }
            mapView.invalidate()
        },
    )

    LaunchedEffect(centerOnUserRequestId, userLocation) {
        if (centerOnUserRequestId == consumedCenterRequestId) return@LaunchedEffect
        consumedCenterRequestId = centerOnUserRequestId
        val target = userLocation ?: return@LaunchedEffect
        mapView.controller.animateTo(GeoPoint(target.latitude, target.longitude))
    }

    LaunchedEffect(centerOnPinRequestId, selectedPinId, pins) {
        if (centerOnPinRequestId == consumedCenterPinRequestId) return@LaunchedEffect
        consumedCenterPinRequestId = centerOnPinRequestId
        val target = pins.firstOrNull { it.id == selectedPinId }?.coordinate ?: return@LaunchedEffect
        mapView.controller.animateTo(GeoPoint(target.latitude, target.longitude))
    }

    LaunchedEffect(radiusMeters, pins, selectedPinId) {
        updatePinOverlays(
            mapView = mapView,
            pinMarkers = pinMarkers,
            pinRadius = pinRadius,
            pins = pins,
            selectedPinId = selectedPinId,
            radiusMeters = radiusMeters,
            onPinSelected = onPinSelected,
        )
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = {
                updatePinOverlays(
                    mapView = mapView,
                    pinMarkers = pinMarkers,
                    pinRadius = pinRadius,
                    pins = pins,
                    selectedPinId = selectedPinId,
                    radiusMeters = radiusMeters,
                    onPinSelected = onPinSelected,
                )
            },
        )

        if (!locationPermissionGranted) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                text = "Grant location permission for user position",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun AndroidLocationObserver(
    enabled: Boolean,
    onLocationChanged: (MapCoordinate) -> Unit,
) {
    val context = LocalContext.current
    val locationManager = remember(context) {
        context.getSystemService(LocationManager::class.java)
    }

    DisposableEffect(enabled, locationManager) {
        if (!enabled || locationManager == null) {
            return@DisposableEffect onDispose { }
        }

        val listener = LocationListener { location: Location ->
            onLocationChanged(MapCoordinate(location.latitude, location.longitude))
        }

        runCatching {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?.let { onLocationChanged(MapCoordinate(it.latitude, it.longitude)) }
        }
        runCatching {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?.let { onLocationChanged(MapCoordinate(it.latitude, it.longitude)) }
        }

        runCatching {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1500L,
                    5f,
                    listener,
                    Looper.getMainLooper(),
                )
            }
        }
        runCatching {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1500L,
                    5f,
                    listener,
                    Looper.getMainLooper(),
                )
            }
        }

        onDispose {
            runCatching { locationManager.removeUpdates(listener) }
        }
    }
}

private fun updatePinOverlays(
    mapView: MapView,
    pinMarkers: MutableMap<String, Marker>,
    pinRadius: Polygon,
    pins: List<MapPin>,
    selectedPinId: String?,
    radiusMeters: Float,
    onPinSelected: (String) -> Unit,
) {
    val incomingIds = pins.map { it.id }.toSet()
    val toRemove = pinMarkers.keys.filter { it !in incomingIds }
    toRemove.forEach { id ->
        pinMarkers.remove(id)?.let { mapView.overlays.remove(it) }
    }

    pins.forEachIndexed { index, pin ->
        val marker = pinMarkers.getOrPut(pin.id) {
            Marker(mapView).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    onPinSelected(pin.id)
                    true
                }
            }
        }
        marker.position = GeoPoint(pin.coordinate.latitude, pin.coordinate.longitude)
        marker.title = if (pin.id == selectedPinId) "Pin ${index + 1} (selected)" else "Pin ${index + 1}"
        if (!mapView.overlays.contains(marker)) {
            mapView.overlays.add(marker)
        }
    }

    val selectedCoordinate = pins.firstOrNull { it.id == selectedPinId }?.coordinate
    if (selectedCoordinate == null) {
        mapView.overlays.remove(pinRadius)
    } else {
        val center = GeoPoint(selectedCoordinate.latitude, selectedCoordinate.longitude)
        pinRadius.points = Polygon.pointsAsCircle(center, radiusMeters.toDouble())
        if (!mapView.overlays.contains(pinRadius)) {
            mapView.overlays.add(pinRadius)
        }
    }
    mapView.invalidate()
}

private fun android.content.Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}
