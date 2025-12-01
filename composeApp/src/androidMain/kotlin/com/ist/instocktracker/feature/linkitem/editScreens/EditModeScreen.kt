package com.ist.instocktracker.feature.linkitem.editScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsUiState
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsViewModel
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModeScreen(
    linkItemId: String?
) {
    val navController = LocalNavController.current
    val parentEntry = remember {
        navController.getBackStackEntry(AppRoutes.linkItemDetails(linkItemId ?: ""))
    }
    val viewModel: LinkItemDetailsViewModel = viewModel(viewModelStoreOwner = parentEntry)


    val uiState by viewModel.uiState.collectAsState()

    val linkItem = when (uiState) {
        is LinkItemDetailsUiState.Success -> (uiState as LinkItemDetailsUiState.Success).linkItem
        else -> null
    }

    var selectedMode by remember { mutableStateOf(linkItem?.mode) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pre-select the current mode when data is loaded
    LaunchedEffect(linkItem) {
        linkItem?.let {
            if (selectedMode == null) {
                selectedMode = it.mode
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Mode") },
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
                            text = "Select tracking mode",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            Mode.entries.forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (mode == selectedMode),
                                            onClick = {
                                                selectedMode = mode
                                                errorMessage = null
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (mode == selectedMode),
                                        onClick = null,
                                        enabled = !isLoading
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.bodyLarge
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
                                selectedMode?.let { mode ->
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.updateMode(
                                        newMode = mode,
                                        onSuccess = {
                                            isLoading = false
                                            navController.popBackStack()
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            errorMessage = error
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && selectedMode != null
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
