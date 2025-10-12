package com.ist.instocktracker.feature.linkitem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartAtCard(
    selectedDate: Date,
    onDateChanged: (Date) -> Unit,
    selectedTime: Date,
    onTimeChanged: (Date) -> Unit,
    addPreciseTime: Boolean,
    onAddPreciseTimeChanged: (Boolean) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {

            OutlinedTextField(
                value = SimpleDateFormat(
                    "MMM dd, yyyy",
                    Locale.getDefault()
                ).format(selectedDate),
                onValueChange = { },
                label = { Text("Start At") },
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
                                onDateChanged(Date(millis))
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
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add precise time",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = addPreciseTime,
                onCheckedChange = onAddPreciseTimeChanged
            )
        }

        // Time picker (conditional)
        if (addPreciseTime) {
            var showTimePicker by remember { mutableStateOf(false) }
            val timePickerState = rememberTimePickerState(
                initialHour = Calendar.getInstance().apply { time = selectedTime }
                    .get(Calendar.HOUR_OF_DAY),
                initialMinute = Calendar.getInstance().apply { time = selectedTime }
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
                    ).format(selectedTime),
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
                                onTimeChanged(calendar.time)
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