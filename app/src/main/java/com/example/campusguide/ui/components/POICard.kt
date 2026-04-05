package com.example.campusguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
import com.example.campusguide.data.OutsidePOI
import com.example.campusguide.data.POIType
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.shuttle.DepartureResult


private val CafePink = Color(0xFFFF7FF4)
private val MetroBlue = Color(0xFF64B5F6)
private val RestaurantRed = Color(0xFFEF5350)

private val MuseumYellow = Color(0xFFFFF27F)
private val GroceryStoreOrange = Color(0xFFFF9800)
private val ParkGreen = Color(0xFF4CAF50)

@Composable
fun POICard(
    poi: OutsidePOI,
    onDismiss: () -> Unit,
    onDirectionsClick: (() -> Unit)? = null
) {

    val iconDetails = getPOIColorAndDrawable(poi.category)

    val openingHour = poi.workingHours.openingHour.toInt()
    val openingMinute = ((poi.workingHours.openingHour - openingHour) * 60).toInt()

    val closingHour = poi.workingHours.closingHour.toInt()
    val closingMinute = ((poi.workingHours.closingHour - closingHour) * 60).toInt()

    val openingHour12 = when (openingHour) {
        0 -> 12
        in 1..12 -> openingHour
        else -> openingHour - 12
    }

    val startingHoursText =
        if(openingHour < 12) {
            "${openingHour12}:%02d am".format(openingMinute)
        } else {
            "${openingHour12}:%02d pm".format(openingMinute)
        }


    val closingHour12 = when (closingHour) {
        0 -> 12
        in 1..12 -> closingHour
        else -> closingHour - 12
    }

    val closingHoursText =
        if(closingHour < 12) {
            "${closingHour12}:%02d am".format(closingMinute)
        } else {
            "${closingHour12}:%02d pm".format(closingMinute)
        }

    val workingHoursText = if(poi.workingHours.closedDays.isNotEmpty()){
        "Working Hours: $startingHoursText-$closingHoursText. \nClosed on: ${poi.workingHours.closedDays.joinToString(", ")}"
    }else{
        "Working Hours: $startingHoursText-$closingHoursText. \nOpen all week"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(iconDetails.first),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconDetails.second),
                        contentDescription = "poiCardIcon",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(0.dp)){
                    AccessibleText(
                        text = poi.category.toString(),
                        baseFontSizeSp = 16f,
                        forceFontWeight = FontWeight.Medium
                    )
                    AccessibleText(
                        text = poi.name,
                        baseFontSizeSp = 16f,
                        forceFontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .border(1.5.dp, iconDetails.first, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row{
                            Icon(
                                painter = painterResource(R.drawable.rating_star),
                                contentDescription = "ratingStar",
                                modifier = Modifier.size(18.dp)
                            )
                            AccessibleText(
                                text = poi.rating.toString(),
                                baseFontSizeSp = 16f,
                                forceFontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AccessibleText(
                    text = poi.description,
                    baseFontSizeSp = 14f,
                    fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AccessibleText(
                    text = poi.address,
                    baseFontSizeSp = 14f,
                    forceFontWeight = FontWeight.SemiBold
                )

                AccessibleText(
                    text = workingHoursText,
                    baseFontSizeSp = 14f,
                    forceFontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDirectionsClick != null) {
                    Button(
                        modifier = Modifier.semantics { contentDescription = "POICardDirections" }
                            .testTag("POICardDirections"),
                        onClick = {
                            onDismiss()
                            onDirectionsClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iconDetails.first,
                            contentColor = Color.Black
                        )

                    ) {
                        Text("Directions")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}



fun getPOIColorAndDrawable(type: POIType): Pair<Color, Int>{

    val (colorToUse, iconToUse) = when(type){
        POIType.Cafe -> Pair(CafePink, R.drawable.cafe_icon)
        POIType.Metro -> Pair(MetroBlue, R.drawable.metro_icon)
        POIType.Restaurant -> Pair(RestaurantRed, R.drawable.restaurant_icon)
        POIType.Museum -> Pair(MuseumYellow, R.drawable.museum_icon)
        POIType.Grocery -> Pair(GroceryStoreOrange, R.drawable.grocery_icon)
        POIType.Park -> Pair(ParkGreen, R.drawable.park_icon)
    }

    return Pair(colorToUse, iconToUse)

}