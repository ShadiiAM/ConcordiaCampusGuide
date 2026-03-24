package com.example.campusguide.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import com.example.campusguide.ui.accessibility.AccessibleText

@Composable
fun <T> BuildingAutocompleteField(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    suggestions: List<T> = emptyList(),

    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSuggestionSelected: (T) -> Unit = {},

    suggestionKey: ((T) -> Any)? = null,
    suggestionContent: @Composable (T) -> Unit = {},
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
                            .testTag("autocompleteField")
                            .focusRequester(textFocusRequester)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
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