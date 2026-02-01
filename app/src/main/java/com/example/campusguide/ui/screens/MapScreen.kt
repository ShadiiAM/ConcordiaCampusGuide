package com.example.campusguide.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.campusguide.ui.components.SearchBarWithProfile

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun MapScreen(){
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBarWithProfile(
                onSearchQueryChange = { /* TODO: Handle search query */ }
            )
        }
        }
}