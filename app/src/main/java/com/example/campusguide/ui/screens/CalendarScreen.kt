package com.example.campusguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.example.campusguide.ui.theme.PurpleGrey80
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {

    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showAccessibility by rememberSaveable { mutableStateOf(false) }


    if (showAccessibility) {
        AccessibilityScreen(
            onBackClick = { showAccessibility = false }
        )
    } else if (showProfile) {
        ProfileScreen(
            onBackClick = { showProfile = false },
            onProfileClick = { /* TODO: Navigate to profile details */ },
            onAccessibilityClick = { showAccessibility = true }
        )
    }

    var date by remember { mutableStateOf(Calendar.getInstance()) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.DAILY) }

    fun formattedDate(date: Calendar, viewMode: CalendarViewMode): String {
        return when (viewMode) {
            CalendarViewMode.DAILY -> {
                val month = date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                val day = date.get(Calendar.DAY_OF_MONTH)
                "$month $day"
            }
            CalendarViewMode.WEEKLY -> {
                val month = date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                val start = date.get(Calendar.DAY_OF_MONTH)
                val endCal = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }
                val end = endCal.get(Calendar.DAY_OF_MONTH)
                "$month $start–$end"
            }
            CalendarViewMode.MONTHLY -> {
                date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(top =25.dp)) {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=620.dp), //look for Better way soon
            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            val radius = 60.dp
            val stroke = 6.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .drawBehind {
                        val r = radius.toPx()
                        val s = stroke.toPx()

                        drawLine(PurpleGrey80, Offset(r, 0f), Offset(size.width - r, 0f), s)
                        drawLine(PurpleGrey80, Offset(0f, r), Offset(0f, size.height / 2), s)
                        drawLine(PurpleGrey80, Offset(size.width, r), Offset(size.width, size.height / 2), s)

                        drawArc(
                            color = PurpleGrey80,
                            startAngle = 180f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(0f, 0f),
                            size = Size(r * 2, r * 2),
                            style = Stroke(s)
                        )
                        drawArc(
                            color = PurpleGrey80,
                            startAngle = 270f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(size.width - 2 * r, 0f),
                            size = Size(r * 2, r * 2),
                            style = Stroke(s)
                        )
                    }
                    .padding(top = 20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(modifier = Modifier .fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Go back in date",
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                date = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
                            }
                    )

                    Text(formattedDate(date, viewMode), fontSize = 35.sp)

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go ahead in date",
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                date = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
                            }
                    )                }

                Row(modifier = Modifier .fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = {
                            viewMode = CalendarViewMode.DAILY
                                  },
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
                            containerColor = Color.LightGray, // background color
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
                        onClick = { viewMode = CalendarViewMode.WEEKLY },
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
                        onClick = { viewMode = CalendarViewMode.MONTHLY },
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

    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    ConcordiaCampusGuideTheme {
        CalendarScreen()
    }
}

enum class CalendarViewMode {
    DAILY, WEEKLY, MONTHLY
}
