package com.example.campusguide.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {

    val context = LocalContext.current

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

}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    ConcordiaCampusGuideTheme {
        MapScreen()
    }
}