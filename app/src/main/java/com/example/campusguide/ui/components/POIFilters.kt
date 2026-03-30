package com.example.campusguide.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
import com.example.campusguide.data.POIFilterValues
import com.example.campusguide.data.POIType
import com.example.campusguide.ui.accessibility.AccessibleText

@Composable
fun POIFilterTags(
    modifier: Modifier = Modifier,
    poiFilters: POIFilterValues,

    onPOITagSelect: (POIType) -> Unit,
    onPOITagDismiss: (POIType) -> Unit,

    onPOIRatingClick: (Double) -> Unit,
    onPOIDistanceClick: (Float) -> Unit,

    ) {

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (type in POIType.entries) {

                val iconDetails = getPOIColorAndDrawable(type)

                Surface(
                    onClick = {
                        if (type in poiFilters.categoriesIncluded) onPOITagDismiss(type) else onPOITagSelect(
                            type
                        )
                    },
                    selected = type in poiFilters.categoriesIncluded,
                    shape = RoundedCornerShape(8.dp),
                    color = if (type in poiFilters.categoriesIncluded) iconDetails.first
                    else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Icon(
                        painter = painterResource(iconDetails.second),
                        contentDescription = type.toString() + "_icon_filter",
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                            .size(18.dp),
                        tint = Color(0xFF000000)
                    )
                }
            }
        }

        Row{
            Surface(
                onClick = {
                    onPOIRatingClick(poiFilters.rating)
                },
                shape = RoundedCornerShape(8.dp),
                color = if (poiFilters.rating > 0.0) Color.Yellow
                else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rating_star),
                        contentDescription = "rating_POI_filter",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF000000)
                    )
                    AccessibleText(
                        text = poiFilters.rating.toInt().toString() + " <",
                        baseFontSizeSp = 14f,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                onClick = {
                    onPOIDistanceClick(poiFilters.distanceLimit)
                },
                shape = RoundedCornerShape(8.dp),
                color = if (poiFilters.distanceLimit > 0.0) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_poi),
                        contentDescription = "distance_POI_filter",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF000000)
                    )
                    AccessibleText(
                        text = when {
                            poiFilters.distanceLimit < 1000.0f -> "${poiFilters.distanceLimit.toInt()} m >"
                            poiFilters.distanceLimit % 1000.0f == 0.0f -> "${(poiFilters.distanceLimit / 1000).toInt()} km >"
                            else -> "shouldn't reach here"
                        },
                        baseFontSizeSp = 14f,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

        }

    }

}