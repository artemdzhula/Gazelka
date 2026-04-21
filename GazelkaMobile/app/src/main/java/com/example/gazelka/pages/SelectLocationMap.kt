package com.example.gazelka.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SelectLocationMap(
    modifier: Modifier = Modifier,
    onLocationSelected: (LatLng) -> Unit,
    onInteractionChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)


    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    // Используем Варшаву, пока геопозиция ещё не получена
    val defaultLocation = currentLocation ?: LatLng(52.2297, 21.0122)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var markerPosition by remember { mutableStateOf<LatLng?>(null) }

    Box(
        modifier = modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val pressed = event.changes.any { it.pressed }
                    onInteractionChanged(pressed)
                }
            }
        }
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markerPosition = latLng
                onLocationSelected(latLng)
            },
            properties = MapProperties(isMyLocationEnabled = permissionState.status.isGranted)
        ) {
            markerPosition?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Selected location"
                )
            }
        }
    }
}
