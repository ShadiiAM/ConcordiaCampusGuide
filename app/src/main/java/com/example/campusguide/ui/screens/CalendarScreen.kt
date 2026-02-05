package com.example.campusguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.AppDestinations
import com.example.campusguide.ui.components.NavigationBar
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.example.campusguide.ui.theme.Pink80
import com.example.campusguide.ui.theme.Purple80
import com.example.campusguide.ui.theme.PurpleGrey80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var currentDestination = rememberSaveable { mutableStateOf(AppDestinations.CALENDAR) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showAccessibility by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().padding(top =25.dp)) {


        SearchBarWithProfile(
            onSearchQueryChange = { /* TODO: Handle search query */ },
            onProfileClick = { showProfile = true }
        )
        // 1️⃣ Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        )

        // 2️⃣ Calendar content (drawn above background)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=620.dp), //look for Better way soon
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            // Example calendar box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(
                        width = 6.dp,
                        color = PurpleGrey80,
                        shape = RoundedCornerShape(60.dp)
                    )
                    .background(Color.White, shape = RoundedCornerShape(60.dp))
                    .padding(top = 20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                //Row 1
                Row(modifier = Modifier .fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top ) {
                    Text("< ", fontSize = 35.sp)
                    Text("5 Feb", fontSize = 35.sp)
                    Text(" >", fontSize = 35.sp)
                }
                //Row 2
                Row(modifier = Modifier .fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { /* handle click */ },
                        modifier = Modifier
                            .height(24.dp)
                            .defaultMinSize(minHeight = 1.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            bottomStart = 16.dp,
                            topEnd = 5.dp,
                            bottomEnd = 5.dp ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleGrey80, // background color
                            contentColor = Color.Black)
                    )
                    {
                        Text(text = "Daily", fontSize = 15.sp)
                    }

                    Button( modifier = Modifier
                        .height(24.dp)
                        .defaultMinSize(minHeight = 1.dp)
                        .padding(horizontal = 2.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        onClick = { /* handle click */ },
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors( containerColor = PurpleGrey80,
                            contentColor = Color.Black ) )
                    {
                        Text(
                            text = "Weekly",
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = { /* handle click */ },
                        modifier = Modifier
                            .height(24.dp)
                            .defaultMinSize(minHeight = 1.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(
                            topStart = 5.dp,
                            bottomStart = 5.dp,
                            topEnd = 16.dp,
                            bottomEnd = 16.dp ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleGrey80, // background color
                            contentColor = Color.Black)
                    )
                    {
                        Text(text = "Monthly", fontSize = 15.sp)
                    }
                }}
            }
        }

        // 3️⃣ Bottom navigation (drawn last → on top)
        NavigationBar(currentDestination)
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    ConcordiaCampusGuideTheme {
        CalendarScreen()
    }
}
