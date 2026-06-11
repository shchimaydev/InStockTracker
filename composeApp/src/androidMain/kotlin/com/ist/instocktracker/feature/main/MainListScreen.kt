package com.ist.instocktracker.feature.main

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.components.LinkItemCard
import com.ist.instocktracker.feature.main.components.LinkFilterTabs
import com.ist.instocktracker.services.ServiceLocator

/**
 * Main screen that displays the Google ID Token
 * @param tokenDataStore DataStore for retrieving the Google ID Token
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainListScreen(paddingValues: PaddingValues) {
    Log.d("MainScreen", "MainScreen called")
    val mainVm = viewModel<MainVIewModel> { MainVIewModel(ServiceLocator.api) }

    val visibleItems by mainVm.visibleItems.collectAsState()
    val filter by mainVm.filter.collectAsState()

    LaunchedEffect(visibleItems) {
        Log.d("MainScreen", "visibleItems: $visibleItems")
    }

    LaunchedEffect(mainVm) {
        mainVm.getLinkItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LinkFilterTabs(
            selected = filter,
            onSelect = mainVm::setFilter,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(visibleItems) { linkItem ->
                LinkItemCard(linkItem)
            }
        }
    }
}