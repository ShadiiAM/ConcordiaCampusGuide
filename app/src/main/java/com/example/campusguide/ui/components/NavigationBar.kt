package com.example.campusguide.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.campusguide.AppDestinations
import com.example.campusguide.AppIcon
import com.example.campusguide.UsabilityTrackerIRLUsers
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.microsoft.clarity.Clarity


@Composable
fun NavigationBar(
    currentDestination: MutableState<AppDestinations>,
    onDestinationSelected: (AppDestinations) -> Unit = { destination ->
        currentDestination.value = destination
    },
    content: (@Composable (Modifier) -> Unit)? = null
) {

    val firebaseAnalytics = Firebase.analytics
    NavigationSuiteScaffold(
        containerColor = Color.White,
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
                    label = { AccessibleText(it.label, baseFontSizeSp = 14f) },
                    selected = it == currentDestination.value,
                    onClick = { onDestinationSelected(it)
                        firebaseAnalytics.logEvent("navigation_bar_to_$it", null)
                        UsabilityTrackerIRLUsers.userInteractionRecord("navigation_bar_to_$it")
                        Clarity.setCurrentScreenName("$it Screen")
                    }
                )
            }
        }
    ) {
        content?.invoke(Modifier.fillMaxSize())
    }
}


@Preview(showBackground = true)
@Composable
@JvmSynthetic
fun NavigationBarPreview() {
    ConcordiaCampusGuideTheme {
        NavigationBar(rememberSaveable{mutableStateOf(AppDestinations.MAP)},
            content ={ SearchBarWithProfile<String>( ) })
    }
}