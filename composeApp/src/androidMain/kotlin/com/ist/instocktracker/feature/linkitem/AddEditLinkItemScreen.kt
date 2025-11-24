package com.ist.instocktracker.feature.linkitem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.utils.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLinkItemScreen(
    linkItemId: String? = null,
    viewModel: AddEditLinkItemViewModel = viewModel()
) {
    val navController = LocalNavController.current

    LaunchedEffect(linkItemId) {
        viewModel.initialize(linkItemId)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                title = {
                    Text(if (viewModel.isEditMode) "Edit" else "Add an item")
                },
                navigationIcon = {
                    IconButton(onClick = {
//                        navController.previousBackStackEntry
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

                StartAtCard(
                    selectedDate = viewModel.selectedDate,
                    onDateChanged = viewModel::onDateChanged,
                    selectedTime = viewModel.selectedTime,
                    onTimeChanged = viewModel::onTimeChanged,
                    addPreciseTime = viewModel.addPreciseTime,
                    onAddPreciseTimeChanged = viewModel::onAddPreciseTimeChanged
                )

                CheckIntervalCard(
                    intervalUnit = viewModel.intervalUnit,
                    onIntervalUnitChanged = viewModel::onIntervalUnitChanged,
                    intervalDuration = viewModel.intervalDuration,
                    onIntervalDurationChanged = viewModel::onIntervalDurationChanged,
                    intervalError = viewModel.intervalError
                )
                //}


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
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

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        viewModel.saveItem {
                            navController.popBackStack()
                        }
                    },
                    enabled = !viewModel.isLoading
                ) {
                    Text(text = "Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Preview(
    showBackground = true,
    name = "Add Link Item (Dark)"
)
@Composable
fun AddEditLinkItemScreenPreview() {
    val mockNavController = rememberNavController()

    MaterialTheme {
        CompositionLocalProvider(LocalNavController provides mockNavController) {
            // Pass `null` for `linkItemId` to simulate adding a new item.
            AddEditLinkItemScreen(linkItemId = null)
        }
    }
}