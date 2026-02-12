package com.example.campusguide.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusguide.Greeting
import com.example.campusguide.MapsActivity
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme

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
                AccessibleText("Open Campus Map",
                    baseFontSizeSp = 16f,
                    fallbackColor = Color.White
                )
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