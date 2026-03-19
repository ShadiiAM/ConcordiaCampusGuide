package com.example.campusguide.ui.components
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.testTag
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.campusguide.data.CampusBuilding
//import androidx.compose.foundation.border
//
//@Composable
//fun BuildingAutocompleteField(
//    label: String,
//    value: String,
//    suggestions: List<CampusBuilding>,
//    onQueryChange: (String) -> Unit,
//    onSelected: (CampusBuilding) -> Unit,
//    modifier: Modifier = Modifier,
//    placeholder: String = "Building name or code…",
//    enabled: Boolean = true,
//    testTag: String = "",
//    shuttleStops: List<com.example.campusguide.data.ShuttleStop> = emptyList(),
//    onShuttleStopSelected: (com.example.campusguide.data.ShuttleStop) -> Unit = {},
//) {
//    var query by remember(value) { mutableStateOf(value) }
//    var isFocused by remember { mutableStateOf(false) }
//    val focusManager = LocalFocusManager.current
//
//    val showDropdown = suggestions.isNotEmpty()
//
//    Column(modifier = modifier.fillMaxWidth()) {
//
//        // Label and text input
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxWidth(),
//        ) {
//            Text(
//                text = label,
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.width(48.dp),
//            )
//
//            Surface(
//                shape = RoundedCornerShape(20.dp),
//                color = MaterialTheme.colorScheme.surfaceVariant,
//                modifier = Modifier.weight(1f),
//            ) {
//                Box(
//                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
//                    contentAlignment = Alignment.CenterStart,
//                ) {
//                    if (query.isEmpty()) {
//                        Text(
//                            text = placeholder,
//                            style = TextStyle(
//                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
//                                fontSize = 14.sp,
//                            ),
//                        )
//                    }
//                    BasicTextField(
//                        value = query,
//                        onValueChange = { new ->
//                            query = new
//                            onQueryChange(new)
//                        },
//                        enabled = enabled,
//                        singleLine = true,
//                        textStyle = TextStyle(
//                            color = MaterialTheme.colorScheme.onSurface,
//                            fontSize = 14.sp,
//                        ),
//                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
//                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                        keyboardActions = KeyboardActions(
//                            onDone = { focusManager.clearFocus() }
//                        ),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .onFocusChanged { isFocused = it.isFocused }
//                            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
//                    )
//                }
//            }
//        }
//
//        //  Dropdown suggestions
//        if (showDropdown) {
//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 48.dp)
//                    .heightIn(max = 230.dp),
//                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
//                color = MaterialTheme.colorScheme.surface,
//                tonalElevation = 6.dp,
//                shadowElevation = 6.dp,
//            ) {
//                LazyColumn {
//                    items(suggestions, key = { it.buildingCode }) { building ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {
//                                    query = building.displayName
//                                    onSelected(building)
//                                    focusManager.clearFocus()
//                                }
//                                .padding(horizontal = 14.dp, vertical = 10.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            // Building code badge
//                            Surface(
//                                shape = RoundedCornerShape(6.dp),
//                                color = MaterialTheme.colorScheme.primaryContainer,
//                            ) {
//                                Text(
//                                    text = building.buildingCode,
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
//                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
//                                )
//                            }
//
//                            Spacer(Modifier.width(10.dp))
//
//                            Column {
//                                Text(
//                                    text = building.buildingName,
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = MaterialTheme.colorScheme.onSurface,
//                                )
//                                if (building.address.isNotEmpty()) {
//                                    Text(
//                                        text = building.address,
//                                        style = MaterialTheme.typography.labelSmall,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                    )
//                                }
//                            }
//                        }
//                        HorizontalDivider(thickness = 0.5.dp)
//                    }
//                }
//            }
//        }
//        if (shuttleStops.isNotEmpty()) {
//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 48.dp)
//                    .heightIn(max = 230.dp),
//                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
//                color = MaterialTheme.colorScheme.surface,
//                tonalElevation = 6.dp,
//                shadowElevation = 6.dp,
//            ) {
//                LazyColumn {
//                    items(shuttleStops, key = { it.id }) { stop ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {
//                                    query = stop.name
//                                    onShuttleStopSelected(stop)
//                                    focusManager.clearFocus()
//                                }
//                                .padding(horizontal = 14.dp, vertical = 10.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            Surface(
//                                shape = RoundedCornerShape(6.dp),
//                                color = MaterialTheme.colorScheme.surface,
//                                modifier = Modifier.border(
//                                    1.5.dp,
//                                    MaterialTheme.colorScheme.primary,
//                                    RoundedCornerShape(6.dp)
//                                )
//                            ) {
//                                Text(
//                                    text = "Shuttle",
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.primary,
//                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
//                                )
//                            }
//                            Spacer(Modifier.width(10.dp))
//                            Column {
//                                Text(stop.name, style = MaterialTheme.typography.bodySmall)
//                                Text(stop.description, style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
//                            }
//                        }
//                        HorizontalDivider(thickness = 0.5.dp)
//                    }
//                }
//            }
//        }
//    }
//}