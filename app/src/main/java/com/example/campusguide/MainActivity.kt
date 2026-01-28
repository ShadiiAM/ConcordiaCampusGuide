package com.example.campusguide

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConcordiaCampusGuideTheme {
                ConcordiaCampusGuideApp()
            }
        }
    }
}

sealed class AppIcon {
    data class Vector(val imageVector: ImageVector) : AppIcon()
    data class Drawable(@DrawableRes val resId: Int) : AppIcon()
}
@PreviewScreenSizes
@Composable
fun ConcordiaCampusGuideApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.MAP) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        when (val icon = it.icon) {
                            is AppIcon.Vector ->
                                Icon(icon.imageVector, contentDescription = it.label)
                            is AppIcon.Drawable ->
                                Icon(painterResource(icon.resId), contentDescription = it.label)
                        }
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold{ innerPadding ->
            Search_button(
                modifier = Modifier
                .padding(innerPadding)
                .padding(all = 16.dp)
            )
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
}

enum class AppDestinations(
    val label: String,
    val icon: AppIcon,
) {
    MAP("Map", AppIcon.Vector(Icons.Default.Place)),
    DIRECTIONS("Directions", AppIcon.Drawable(R.drawable.directions_icon)),
    CALENDAR("Calendar", AppIcon.Drawable(R.drawable.calendar_icon)),
    POI("POI", AppIcon.Drawable(R.drawable.poi_icon)),
}

@Composable
fun Search_button(modifier: Modifier = Modifier) {
    var location by remember{
        mutableStateOf("")
    }
    Row(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center

    ){
        TextField(
            value = location,
            onValueChange = { text ->
                location = text
            },
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.65f)
                .border(1.dp, Color.Transparent, RoundedCornerShape(12.dp)),
            placeholder  = { Text("Search Location") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.LightGray,
                focusedContainerColor = Color.LightGray
            )



        )
        IconButton(onClick = { /* action */ }) {
            
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
            )

        }
        IconButton(onClick = { /* action */ }) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile"
            )
        }
    }
}


@Preview
@Composable
fun PreviewSearch_button() {
    Search_button(modifier = Modifier
        .padding(top = 100.dp))
}