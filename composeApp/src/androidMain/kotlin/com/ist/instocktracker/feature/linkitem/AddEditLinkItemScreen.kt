package com.ist.instocktracker.feature.linkitem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.data.DurationUnit
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.utils.LocalNavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLinkItemScreen(
    linkItemId: String? = null,
    viewModel: AddEditLinkItemViewModel = viewModel()
) {
    val navController = LocalNavController.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    LaunchedEffect(linkItemId) {
        viewModel.initialize(linkItemId)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.selectedDate.time
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (viewModel.isEditMode) "Edit Link Item" else "Add Link Item")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveItem {
                                navController.popBackStack()
                            }
                        },
                        enabled = !viewModel.isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Error message
                viewModel.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Link field
                OutlinedTextField(
                    value = viewModel.link,
                    onValueChange = viewModel::onLinkChanged,
                    label = { Text("Link *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.linkError != null,
                    supportingText = viewModel.linkError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                // Label field
                OutlinedTextField(
                    value = viewModel.label,
                    onValueChange = viewModel::onLabelChanged,
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Auto-filled from link host") }
                )

                // Mode selector
                var modeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = !modeExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.mode.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Mode") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false }
                    ) {
                        Mode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.displayName) },
                                onClick = {
                                    viewModel.onModeChanged(mode)
                                    modeExpanded = false
                                }
                            )
                        }
                    }
                }

                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Start At",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                            //.clickable { showDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = SimpleDateFormat(
                                    "MMM dd, yyyy",
                                    Locale.getDefault()
                                ).format(viewModel.selectedDate),
                                onValueChange = { },
                                label = { Text("Date") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                                readOnly = false, // Set to false to get the standard enabled appearance
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
                                                viewModel.onDateChanged(Date(millis))
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

                        // Add precise time checkbox
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = viewModel.addPreciseTime,
                                onCheckedChange = viewModel::onAddPreciseTimeChanged
                            )
                            Text(
                                text = "Add precise time",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Time picker (conditional)
                        if (viewModel.addPreciseTime) {
                            var showTimePicker by remember { mutableStateOf(false) }
                            val timePickerState = rememberTimePickerState(
                                initialHour = Calendar.getInstance().apply { time = viewModel.selectedTime }
                                    .get(Calendar.HOUR_OF_DAY),
                                initialMinute = Calendar.getInstance().apply { time = viewModel.selectedTime }
                                    .get(Calendar.MINUTE)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true }
                            ) {
                                OutlinedTextField(
                                    value = SimpleDateFormat(
                                        "HH:mm",
                                        Locale.getDefault()
                                    ).format(viewModel.selectedTime),
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
                                                val calendar = Calendar.getInstance()
                                                calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                                calendar.set(Calendar.MINUTE, timePickerState.minute)
                                                calendar.set(Calendar.SECOND, 0)
                                                calendar.set(Calendar.MILLISECOND, 0)
                                                viewModel.onTimeChanged(calendar.time)
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
                    }
                }

                // Interval compound field
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                value = viewModel.intervalUnit.toString(),
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let {
                                        viewModel.onIntervalUnitChanged(it)
                                    }
                                },
                                label = { Text("Every") },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = viewModel.intervalError != null
                            )

                            // Duration selector
                            var durationExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = durationExpanded,
                                onExpandedChange = { durationExpanded = !durationExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = viewModel.intervalDuration.displayName,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Duration") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded)
                                    },
                                    isError = viewModel.intervalError != null
                                )
                                ExposedDropdownMenu(
                                    expanded = durationExpanded,
                                    onDismissRequest = { durationExpanded = false }
                                ) {
                                    DurationUnit.entries.forEach { duration ->
                                        DropdownMenuItem(
                                            text = { Text(duration.displayName) },
                                            onClick = {
                                                viewModel.onIntervalDurationChanged(duration)
                                                durationExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Interval validation error
                        viewModel.intervalError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Is Active switch
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Enable automatic checking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = viewModel.isActive,
                            onCheckedChange = viewModel::onIsActiveChanged
                        )
                    }
                }

                // Additional Instructions textarea
                OutlinedTextField(
                    value = viewModel.additionalInstructions,
                    onValueChange = viewModel::onAdditionalInstructionsChanged,
                    label = { Text("Additional Instructions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Enter any additional instructions for checking this link...") },
                    maxLines = 5
                )
            }
        }
    }
}