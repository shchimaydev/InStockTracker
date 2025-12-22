package com.ist.instocktracker.feature.linkitem

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ist.instocktracker.R
import com.ist.instocktracker.components.InfoCard
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.getDisplayName
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController
import com.ist.instocktracker.utils.capitalizeWords
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkItemDetailsScreen(
    linkItemId: String?
) {
    val navController = LocalNavController.current
    val viewModel = viewModel { LinkItemDetailsViewModel(linkItemId ?: "") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
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
            when (val state = uiState) {
                is LinkItemDetailsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is LinkItemDetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is LinkItemDetailsUiState.Success -> {
                    LinkItemDetailsContent(
                        linkItem = state.linkItem,
                        onNavigate = { route -> navController.navigate(route) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun LinkItemDetailsContent(
    linkItem: LinkItem,
    onNavigate: (String) -> Unit,
    viewModel: LinkItemDetailsViewModel = viewModel()
) {
    val clipboardManager = LocalClipboard.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val showDeleteConfirmation = remember { mutableStateOf(false) }

    println("LinkItemDetailsContent renders")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoCard(
            value = { Text(linkItem.label?.capitalizeWords() ?: "No Label") },
            //subtitle = "Nintendo Switch", // TODO: Get actual product/category
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD4B896)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.placeholder_image),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        )

        // Link
        InfoCard(
            title = "Link",
            value = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, linkItem.link.toUri())
                                context.startActivity(intent)
                            },
                        text = "Product Link",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2196F3)
                    )
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }

            },
            trailingIcon = {
                Row {
                    IconButton(onClick = { onNavigate(AppRoutes.editLink(linkItem.id)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                    }
                    IconButton(onClick = {
                        scope.launch {
                            clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("link", linkItem.link)))
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
                    }
                }
            },
            onClick = {

            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            InfoCard(
                title = "Mode",
                value = { Text(linkItem.mode.displayName) },
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppRoutes.editMode(linkItem.id)) }
            )
            InfoCard(
                title = "Start At",
                value = { Text(linkItem.startAtFormatted(TimeZone.currentSystemDefault())) },
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppRoutes.editStartAt(linkItem.id)) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            InfoCard(
                title = "Check Interval",
                value = { Text(linkItem.interval.getDisplayName()) },
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppRoutes.editInterval(linkItem.id)) }
            )
            InfoCard(
                title = "Status",
                value = { Text(text = if (linkItem.isActive) "Active" else "Inactive") },
                statusColor = if (linkItem.isActive) Color.Green else Color.Red,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.toggleStatus() }
            )
        }

        InfoCard(
            title = "Last Check",
            subtitle = linkItem.lastCheckedDateFormatted(TimeZone.currentSystemDefault()),
            value = {
                val lastCheckResultString =
                    linkItem.lastCheckResult?.let { if (it) "Success" else "Failed" } ?: "Hasn't been checked yet"
                Text(lastCheckResultString)
            },
            onClick = { }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showDeleteConfirmation.value = true },
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "Delete",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (showDeleteConfirmation.value) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation.value = false },
                title = { Text(text = "Delete item") },
                text = { Text(text = "Are you sure you want to delete this item?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation.value = false
                            viewModel.deleteLinkItem({
                                navController.navigate(AppRoutes.MAIN) {
                                    popUpTo(
                                        AppRoutes.linkItemDetails(
                                            linkItem.id
                                        )
                                    ) { inclusive = true }
                                }
                            })
                        }
                    ) {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation.value = false }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
    }
}

