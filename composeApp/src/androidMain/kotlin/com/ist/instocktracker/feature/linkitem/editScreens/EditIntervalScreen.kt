package com.ist.instocktracker.feature.linkitem.editScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Interval
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsUiState
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsViewModel
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIntervalScreen(
    linkItemId: String?
) {
    val navController = LocalNavController.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val parentEntry = remember(currentBackStackEntry) {
        navController.getBackStackEntry(AppRoutes.linkItemDetails(linkItemId ?: ""))
    }
    val viewModel: LinkItemDetailsViewModel = viewModel(viewModelStoreOwner = parentEntry)

    val uiState by viewModel.uiState.collectAsState()

    val linkItem = when (uiState) {
        is LinkItemDetailsUiState.Success -> (uiState as LinkItemDetailsUiState.Success).linkItem
        else -> null
    }

    var intervalUnit by remember { mutableStateOf(1) }
    var intervalDuration by remember { mutableStateOf(DurationUnit.HOURS) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var intervalError by remember { mutableStateOf<String?>(null) }

    // Pre-populate interval values when data is loaded
    LaunchedEffect(linkItem) {
        linkItem?.let { item ->
            intervalUnit = item.interval.unit
            intervalDuration = item.interval.duration
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Check Interval") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is LinkItemDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is LinkItemDetailsUiState.Error -> {
                    Text(
                        text = (uiState as LinkItemDetailsUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is LinkItemDetailsUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Check Interval",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Unit number field
                            OutlinedTextField(
                                value = intervalUnit.toString(),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let {
                                        intervalUnit = it
                                        intervalError = if (it < 1) "Must be at least 1" else null
                                    }
                                },
                                label = { Text("Every") },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = intervalError != null
                            )

                            // Duration selector
                            var durationExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = durationExpanded,
                                onExpandedChange = { durationExpanded = !durationExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = intervalDuration.displayName,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Duration") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded)
                                    },
                                    isError = intervalError != null
                                )
                                ExposedDropdownMenu(
                                    expanded = durationExpanded,
                                    onDismissRequest = { durationExpanded = false }
                                ) {
                                    DurationUnit.entries.forEach { duration ->
                                        DropdownMenuItem(
                                            text = { Text(duration.displayName) },
                                            onClick = {
                                                intervalDuration = duration
                                                durationExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Interval validation error
                        intervalError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        errorMessage?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (intervalUnit < 1) {
                                    intervalError = "Must be at least 1"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null

                                val newInterval = Interval(
                                    unit = intervalUnit,
                                    duration = intervalDuration
                                )

                                viewModel.updateInterval(
                                    newInterval = newInterval,
                                    onSuccess = {
                                        isLoading = false
                                        navController.popBackStack()
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = error
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && intervalError == null
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}
