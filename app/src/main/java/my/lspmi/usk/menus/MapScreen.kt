package my.lspmi.usk.menus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen() {
    val context = LocalContext.current

    // Track permission state for both FINE and COARSE location
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (!hasLocationPermission) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Akses Lokasi Dibutuhkan!",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Berikan askses untuk menggunakan fitur peta",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Text("Beri Akses")
                    }
                }
            } else {
                OsmdroidAutoCenteredMap(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun OsmdroidAutoCenteredMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setCenter(GeoPoint(-6.1754, 106.8272)) //Titik jakarta
            controller.setZoom(15.0)
        }
    }

    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
            isDrawAccuracyEnabled = true
            runOnFirstFix {
                mapView.post {
                    myLocation?.let { geoPoint ->
                        mapView.controller.animateTo(geoPoint)
                        mapView.controller.setZoom(16.0)
                    }
                }
            }
        }
    }

    LaunchedEffect(mapView, locationOverlay) {
        if (!mapView.overlays.contains(locationOverlay)) {
            mapView.overlays.add(locationOverlay)
        }
        centerOnLastKnownLocation(context, mapView)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, mapView) {
        mapView.onResume()
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    locationOverlay.disableMyLocation()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationOverlay.disableMyLocation()
            mapView.onPause()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView }
        )

        // Tombol disini
        FloatingActionButton(
            onClick = {
                val loc = locationOverlay.myLocation
                if (loc != null) {
                    mapView.controller.animateTo(loc)
                    mapView.controller.setZoom(16.0)
                    locationOverlay.enableFollowLocation()
                } else {
                    centerOnLastKnownLocation(context, mapView)
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "My Location"
            )
        }
    }
}

private fun centerOnLastKnownLocation(context: Context, mapView: MapView) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    ) {
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || (l.hasAccuracy() && bestLocation.hasAccuracy() && l.accuracy < bestLocation.accuracy)) {
                bestLocation = l
            }
        }
        if (bestLocation != null) {
            val geoPoint = GeoPoint(bestLocation.latitude, bestLocation.longitude)
            mapView.controller.setCenter(geoPoint)
            mapView.controller.setZoom(16.0)
        } else {
            // Default center if no last known location is available
            mapView.controller.setCenter(GeoPoint(-6.1754, 106.8272))
            mapView.controller.setZoom(15.0)
        }
    }
}