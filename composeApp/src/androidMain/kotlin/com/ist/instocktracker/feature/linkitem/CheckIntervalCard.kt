package com.ist.instocktracker.feature.linkitem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ist.instocktracker.data.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckIntervalCard(
    intervalUnit: Int,
    onIntervalUnitChanged: (Int) -> Unit,
    intervalDuration: DurationUnit,
    onIntervalDurationChanged: (DurationUnit) -> Unit,
    intervalError: String?
) {
    Column(
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
                value = intervalUnit.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let {
                        onIntervalUnitChanged(it)
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
                                onIntervalDurationChanged(duration)
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
    }
}