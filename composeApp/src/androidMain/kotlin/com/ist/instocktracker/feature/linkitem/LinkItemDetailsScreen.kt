package com.ist.instocktracker.feature.linkitem

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ist.instocktracker.R
import com.ist.instocktracker.components.InfoCard
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.getDisplayName
import com.ist.instocktracker.navigation.AppRoutes
import com.ist.instocktracker.utils.LocalNavController
import com.ist.instocktracker.utils.capitalizeWords

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkItemDetailsScreen(
    linkItemId: String?
) {
    val navController = LocalNavController.current

//    val viewModel: LinkItemDetailsViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return LinkItemDetailsViewModel(linkItemId ?: "") as T
//            }
//        }
//    )

    val viewModel: LinkItemDetailsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                LinkItemDetailsViewModel(linkItemId ?: "")
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

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
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }

                is LinkItemDetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }

                is LinkItemDetailsUiState.Success -> {
                    LinkItemDetailsContent(
                        linkItem = state.linkItem,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
            }
        }
    }
}

@Composable
fun LinkItemDetailsContent(
    linkItem: LinkItem,
    onNavigate: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header (Label)
        InfoCard(
            value = { Text(linkItem.label?.capitalizeWords() ?: "No Label") },
            subtitle = "Nintendo Switch", // TODO: Get actual product/category
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
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkItem.link))
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
                    IconButton(onClick = { onNavigate("${AppRoutes.EDIT_LINK}?linkItemId=${linkItem.id}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                    }
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(linkItem.link)) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
                    }
                }
            },
            onClick = {

            }
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                title = "Mode",
                value = { Text(linkItem.mode.displayName) },
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("${AppRoutes.EDIT_MODE}?linkItemId=${linkItem.id}") }
            )
            InfoCard(
                title = "Start At",
                value = { Text(linkItem.startAt ?: "Now") },
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("${AppRoutes.EDIT_START_AT}?linkItemId=${linkItem.id}") }
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                title = "Check Interval",
                value = { Text(linkItem.interval.getDisplayName()) },
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("${AppRoutes.EDIT_INTERVAL}?linkItemId=${linkItem.id}") }
            )
            InfoCard(
                title = "Status",
                value = { Text(text = if (linkItem.isActive) "Active" else "Inactive") },
                statusColor = if (linkItem.isActive) Color.Green else Color.Gray,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("${AppRoutes.EDIT_STATUS}?linkItemId=${linkItem.id}") }
            )
        }

        InfoCard(
            title = "Last Check",
            subtitle = linkItem.lastCheckedDateFormatted(),
            value = { Text(if (linkItem.lastCheckResult == true) "Success" else "Failed") },
            onClick = { onNavigate("${AppRoutes.VIEW_LAST_CHECK}?linkItemId=${linkItem.id}") }
        )

        InfoCard(
            title = "Additional Instructions",
            value = { Text(linkItem.additionalInstructions ?: "None") },
            onClick = { onNavigate("${AppRoutes.EDIT_INSTRUCTIONS}?linkItemId=${linkItem.id}") }
        )
    }
}
