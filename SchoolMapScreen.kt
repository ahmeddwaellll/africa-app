package com.example.africanschools.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun SchoolMapScreen(
    schools: List<School>,
    onMapClick: (LatLng) -> Unit = {}
) {
    val defaultLocation = LatLng(-1.2921, 36.8219) // Default to Nairobi, Kenya
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick
    ) {
        // Add markers for each school
        schools.forEach { school ->
            val location = LatLng(school.latitude, school.longitude)
            SchoolMarker(location, school.name)
        }
    }
}

@Composable
fun SchoolMarker(
    position: LatLng,
    title: String
) {
    Marker(
        state = MarkerState(position = position),
        title = title,
        snippet = "Tap for details",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    )
}
