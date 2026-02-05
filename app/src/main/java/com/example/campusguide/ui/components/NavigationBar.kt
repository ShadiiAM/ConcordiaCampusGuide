package com.example.campusguide.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusguide.AppDestinations
import com.example.campusguide.AppIcon
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme




@Composable
fun NavigationBar(
    currentDestination: MutableState<AppDestinations>,
    content: @Composable (Modifier) -> Unit
) {

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        when (val icon = it.icon) {
                            is AppIcon.Vector -> Icon(
                                icon.imageVector,
                                contentDescription = it.label
                            )

                            is AppIcon.Drawable -> Icon(
                                painter = painterResource(id = icon.resId),
                                contentDescription = it.label
                            )
                        }
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination.value,
                    onClick = { currentDestination.value = it }
                )
            }
        }
    )
    content(Modifier
        .fillMaxSize()
        .padding(top = 100.dp)
    )
}


@Composable
internal fun NavigationBarPreviewContent() {
    ConcordiaCampusGuideTheme {
        NavigationBar(
            rememberSaveable { mutableStateOf(AppDestinations.MAP) }
        ) {
            SearchBarWithProfile()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationBarPreview() {
    NavigationBarPreviewContent()
}