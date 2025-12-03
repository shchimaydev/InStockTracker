package com.ist.instocktracker.feature.linkitem.editScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsUiState
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsViewModel
import com.ist.instocktracker.feature.linkitem.components.TimePickerDialog
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController
import kotlinx.datetime.*
import kotlinx.datetime.format.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStartAtScreen(
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

    var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.UTC).date) }
    var selectedTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.UTC).time) }
    var addPreciseTime by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Pre-populate the date when data is loaded
    LaunchedEffect(linkItem) {
        linkItem?.let { item ->
            val startAtStr = item.startAt
            val dateTime = if (!startAtStr.isNullOrEmpty()) {
                try {
                    LocalDateTime.parse(startAtStr)
                } catch (e: Exception) {
                    try {
                         Instant.parse(startAtStr).toLocalDateTime(TimeZone.UTC)
                    } catch (e2: Exception) {
                         Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    }
                }
            } else {
                Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
            selectedDate = dateTime.date
            selectedTime = dateTime.time
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    )

    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Start At") },
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
                        // Date Picker
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            val dateText = remember(selectedDate) {
                                val format = LocalDate.Format {
                                    monthName(MonthNames.ENGLISH_ABBREVIATED)
                                    char(' ')
                                    dayOfMonth()
                                    chars(", ")
                                    year()
                                }
                                selectedDate.format(format)
                            }

                            OutlinedTextField(
                                value = dateText,
                                onValueChange = { },
                                label = { Text("Start At") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                                readOnly = false,
                                trailingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showDatePicker = true }
                            )
                        }

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                selectedDate = Instant.fromEpochMilliseconds(millis)
                                                    .toLocalDateTime(TimeZone.UTC).date
                                            }
                                            showDatePicker = false
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) {
                                        Text("Cancel")
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        // Add precise time switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add precise time",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = addPreciseTime,
                                onCheckedChange = { addPreciseTime = it }
                            )
                        }

                        // Time picker (conditional)
                        if (addPreciseTime) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true }
                            ) {
                                val timeText = remember(selectedTime) {
                                    val format = LocalTime.Format {
                                        hour()
                                        char(':')
                                        minute()
                                    }
                                    selectedTime.format(format)
                                }

                                OutlinedTextField(
                                    value = timeText,
                                    onValueChange = { },
                                    label = { Text("Time") },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    trailingIcon = {
                                        Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
                                    }
                                )
                            }

                            if (showTimePicker) {
                                TimePickerDialog(
                                    title = { Text("Select Time") },
                                    onDismissRequest = { showTimePicker = false },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)
                                                showTimePicker = false
                                            }
                                        ) {
                                            Text("OK")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showTimePicker = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                ) {
                                    TimePicker(state = timePickerState)
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

                                val finalDateTime = if (addPreciseTime) {
                                    LocalDateTime(selectedDate, selectedTime)
                                } else {
                                    LocalDateTime(selectedDate, LocalTime(0, 0))
                                }

                                viewModel.updateStartAt(
                                    newStartAt = finalDateTime.toString(),
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
                            enabled = !isLoading
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
