package com.example.campusguide.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusguide.Greeting
import com.example.campusguide.MapsActivity
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen() {
    val context = LocalContext.current


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(45.4972, -73.5789),
            15f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    )
//---------------------------------------------------------------------TO DELETE
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 25.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(
                name = "Android",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = {
                val intent = Intent(context, MapsActivity::class.java)
                context.startActivity(intent)
            }) {
                Text("Open Campus Map")
            }
        }
    }
//---------------------------------------------------------------------TO DELETE

}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    ConcordiaCampusGuideTheme {
        MapScreen()
    }
}