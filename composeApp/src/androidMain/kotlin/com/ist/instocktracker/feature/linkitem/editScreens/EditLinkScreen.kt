package com.ist.instocktracker.feature.linkitem.editScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsUiState
import com.ist.instocktracker.feature.linkitem.LinkItemDetailsViewModel
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLinkScreen(
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

    var linkText by remember { mutableStateOf(linkItem?.link ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pre-populate the text field when data is loaded
    LaunchedEffect(linkItem) {
        linkItem?.let {
            if (linkText.isEmpty()) {
                linkText = it.link
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Link") },
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
                        OutlinedTextField(
                            value = linkText,
                            onValueChange = {
                                linkText = it
                                errorMessage = null
                            },
                            label = { Text("Product Link") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isError = errorMessage != null
                        )

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
                                viewModel.updateLink(
                                    newLink = linkText,
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
                            enabled = !isLoading && linkText.isNotBlank()
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
