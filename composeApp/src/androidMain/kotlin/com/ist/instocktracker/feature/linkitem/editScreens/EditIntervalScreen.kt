package com.ist.instocktracker.feature.linkitem.editScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ist.instocktracker.AppBackgroundGradient
import com.ist.instocktracker.BorderHairline
import com.ist.instocktracker.Cobalt
import com.ist.instocktracker.InkMuted
import com.ist.instocktracker.NavDark
import com.ist.instocktracker.White
import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Interval
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsUiState
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsViewModel
import com.ist.instocktracker.feature.linkitem.components.NumberWheelPickerDialog
import com.ist.instocktracker.navigation.Route
import com.ist.instocktracker.utils.LocalNavController

private fun DurationUnit.wheelRange(): IntRange = when (this) {
    DurationUnit.MINUTES -> 15..60
    DurationUnit.HOURS -> 1..24
    // TODO: bound by actual days-in-month once scheduling is calendar-aware.
    DurationUnit.DAYS -> 1..30
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIntervalScreen(
    linkItemId: String?
) {
    val navController = LocalNavController.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val parentEntry = remember(currentBackStackEntry) {
        navController.getBackStackEntry(Route.LinkItemDetails(linkItemId ?: ""))
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
    var showNumberPicker by remember { mutableStateOf(false) }

    // Pre-populate interval values when data is loaded
    LaunchedEffect(linkItem) {
        linkItem?.let { item ->
            intervalUnit = item.interval.unit
            intervalDuration = item.interval.duration
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(AppBackgroundGradient)),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Edit Check Interval") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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

                        // "Every" field — tapping it opens the number-wheel picker.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(White, RoundedCornerShape(24.dp))
                                .border(1.dp, BorderHairline, RoundedCornerShape(24.dp))
                                .clickable { showNumberPicker = true }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Every",
                                color = InkMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = intervalUnit.toString(),
                                color = Cobalt,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Duration selector
                        var durationExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = durationExpanded,
                            onExpandedChange = { durationExpanded = !durationExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = intervalDuration.displayName,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Duration") },
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderHairline,
                                    focusedBorderColor = Cobalt
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded)
                                }
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
                                            intervalUnit = intervalUnit.coerceIn(duration.wheelRange())
                                            durationExpanded = false
                                        }
                                    )
                                }
                            }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavDark,
                                contentColor = White
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = White
                                )
                            } else {
                                Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                        }
                    }

                    if (showNumberPicker) {
                        NumberWheelPickerDialog(
                            range = intervalDuration.wheelRange(),
                            initialValue = intervalUnit,
                            onDismissRequest = { showNumberPicker = false },
                            onDone = { newValue ->
                                intervalUnit = newValue
                                showNumberPicker = false
                            }
                        )
                    }
                }
            }
        }
    }
}
