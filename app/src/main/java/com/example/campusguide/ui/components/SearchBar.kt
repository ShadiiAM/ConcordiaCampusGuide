package com.example.campusguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.campusguide.data.CampusBuilding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import com.example.campusguide.data.Indoor
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder
import com.google.android.gms.maps.model.LatLng

@Composable
fun <T> SearchBarWithProfile(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    suggestions: List<T> = emptyList(),

    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSuggestionSelected: (T) -> Unit = {},
    onProfileClick: () -> Unit = {},

    suggestionKey: ((T) -> Any)? = null,
    suggestionContent: @Composable (T) -> Unit = {},
    showProfile: Boolean = true,
    onFocusChange: (Boolean) -> Unit = {},
) {
    val textFocusRequester = focusRequester ?: remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val showNoResults = searchQuery.isNotBlank() && suggestions.isEmpty()
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onSearchSubmit(searchQuery) }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        AccessibleText(
                            text = "Search...",
                            fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            baseFontSizeSp = 16f
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchQueryChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("searchBar")
                            .focusRequester(textFocusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                onFocusChange(isFocused)
                            },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { onSearchSubmit(searchQuery) },
                            onDone = { onSearchSubmit(searchQuery) }
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Profile avatar
                if (showProfile){
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4C4E8))
                            .clickable(onClick = onProfileClick)
                            .testTag("UserProfile"),
                        contentAlignment = Alignment.Center
                    ) {
                        AccessibleText(
                            text = "A",
                            fallbackColor = Color(0xFF6B4D8A),
                            baseFontSizeSp = 14f,
                            forceFontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if ((suggestions.isNotEmpty()) && isFocused) {


            val listState = rememberLazyListState()

            LaunchedEffect(suggestions) {
                listState.scrollToItem(0)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(max = 260.dp)
                    .zIndex(1f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
            ) {
                LazyColumn(state = listState) {
                    items(
                        items = suggestions,
                        key = { suggestionKey?.invoke(it) ?: it.hashCode() },
                        contentType = { it!!::class }
                    ) { suggestion ->
                        Box(modifier = Modifier.clickable { onSuggestionSelected(suggestion) }) {
                            suggestionContent(suggestion)
                        }
                    }
                }
            }
        }
        else if (showNoResults && isFocused) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(max = 260.dp)
                    .zIndex(1f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
            ) {
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .semantics { contentDescription = "No results found" },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "No results found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarWithProfilePreview() {
    ConcordiaCampusGuideTheme {
        SearchBarWithProfile<String>()
    }
}
