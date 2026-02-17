package com.ist.instocktracker.ui.billing

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

private const val TAG = "PaywallScreen"

/**
 * Full-screen Paywall using RevenueCat Paywall UI
 * @param onDismiss Callback when user dismisses the paywall
 * @param onPurchaseSuccess Callback when purchase is successful
 * @param onRestoreSuccess Callback when restore is successful
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit = {},
    onRestoreSuccess: () -> Unit = {}
) {
    var showPaywallDialog by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Subscription") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage?.let { error ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error loading paywall",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        errorMessage = null
                        showPaywallDialog = true
                    }) {
                        Text("Retry")
                    }
                }
            }

            if (showPaywallDialog && errorMessage == null) {
                PaywallDialog(
                    paywallDialogOptions = PaywallDialogOptions.Builder()
                        .setListener(object : PaywallListener {
                            override fun onPurchaseStarted(rcPackage: com.revenuecat.purchases.Package) {
                                Log.d(TAG, "Purchase started")
                                isLoading = true
                            }

                            override fun onPurchaseCompleted(
                                customerInfo: CustomerInfo,
                                storeTransaction: StoreTransaction
                            ) {
                                Log.d(TAG, "Purchase completed")
                                isLoading = false
                                showPaywallDialog = false
                                onPurchaseSuccess()
                            }

                            override fun onPurchaseError(error: PurchasesError) {
                                Log.e(TAG, "Purchase error: ${error.message}")
                                isLoading = false
                                errorMessage = error.message
                            }

                            override fun onRestoreStarted() {
                                Log.d(TAG, "Restore started")
                                isLoading = true
                            }

                            override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                                Log.d(TAG, "Restore completed")
                                isLoading = false
                                showPaywallDialog = false
                                onRestoreSuccess()
                            }

                            override fun onRestoreError(error: PurchasesError) {
                                Log.e(TAG, "Restore error: ${error.message}")
                                isLoading = false
                                errorMessage = error.message
                            }
                        })
                        .build()
                )
            }
        }
    }
}

/**
 * Simple composable to show paywall as a dialog
 * This uses the RevenueCat Paywall Dialog directly
 */
@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit = {},
    onRestoreSuccess: () -> Unit = {}
) {
    PaywallDialog(
        paywallDialogOptions = PaywallDialogOptions.Builder()
            .setListener(object : PaywallListener {
                override fun onPurchaseStarted(rcPackage: com.revenuecat.purchases.Package) {
                    Log.d(TAG, "Purchase started")
                }

                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: StoreTransaction
                ) {
                    Log.d(TAG, "Purchase completed")
                    onPurchaseSuccess()
                }

                override fun onPurchaseError(error: PurchasesError) {
                    Log.e(TAG, "Purchase error: ${error.message}")
                }

                override fun onRestoreStarted() {
                    Log.d(TAG, "Restore started")
                }

                override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                    Log.d(TAG, "Restore completed")
                    onRestoreSuccess()
                }

                override fun onRestoreError(error: PurchasesError) {
                    Log.e(TAG, "Restore error: ${error.message}")
                }
            })
            .build()
    )
}
