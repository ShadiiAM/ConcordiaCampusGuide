package com.example.campusguide.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

enum class Campus {
    SGW,
    LOYOLA
}

@Composable
fun CampusToggle(
    selectedCampus: Campus,
    onCampusSelected: (Campus) -> Unit,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .requiredWidth(width = 259.dp)
            .requiredHeight(height = 48.dp)
            .clip(shape = RoundedCornerShape(16.dp))
    ) {
        // SGW Chip
        InputChip(
            label = {
                Text(
                    text = "SGW",
                    color = if (selectedCampus == Campus.SGW)
                        MaterialTheme.colorScheme.onSecondary
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 1.43.em,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            },
            leadingIcon = if (showIcon) {
                {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "SGW Campus",
                        tint = if (selectedCampus == Campus.SGW)
                            MaterialTheme.colorScheme.onSecondary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            shape = RoundedCornerShape(24.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = if (selectedCampus == Campus.SGW)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.secondaryContainer,
                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                labelColor = if (selectedCampus == Campus.SGW)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            ),
            selected = selectedCampus == Campus.SGW,
            onClick = { onCampusSelected(Campus.SGW) },
            modifier = Modifier
                .weight(weight = 0.5f)
                .testTag("SGW_Button")
        )

        // Loyola Chip
        InputChip(
            label = {
                Text(
                    text = "Loyola",
                    color = if (selectedCampus == Campus.LOYOLA)
                        MaterialTheme.colorScheme.onSecondary
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 1.43.em,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            },
            leadingIcon = if (showIcon) {
                {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Loyola Campus",
                        tint = if (selectedCampus == Campus.LOYOLA)
                            MaterialTheme.colorScheme.onSecondary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            shape = RoundedCornerShape(24.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = if (selectedCampus == Campus.LOYOLA)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.secondaryContainer,
                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                labelColor = if (selectedCampus == Campus.LOYOLA)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            ),
            selected = selectedCampus == Campus.LOYOLA,
            onClick = { onCampusSelected(Campus.LOYOLA) },
            modifier = Modifier
                .weight(weight = 0.5f)
                .testTag("Loyola_Button")
        )
    }
}

@Preview(widthDp = 259, heightDp = 48)
@Composable
internal fun CampusTogglePreview() {
    var selectedCampus by remember { mutableStateOf(Campus.SGW) }
    MaterialTheme {
        CampusToggle(
            selectedCampus = selectedCampus,
            onCampusSelected = { selectedCampus = it },
            showIcon = true
        )
    }
}
