package com.ist.instocktracker.feature.billing

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

private const val TAG = "PaywallScreen"

/**
 * Full-screen Paywall using RevenueCat Paywall UI
 * @param state Current subscription state from ViewModel
 * @param onLoadingChanged Callback to update loading state
 * @param onError Callback to update error state
 * @param onDismiss Callback when user dismisses the paywall
 * @param onPurchaseSuccess Callback when purchase is successful
 * @param onRestoreSuccess Callback when restore is successful
 * @param onRetry Callback to retry loading
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    state: SubscriptionState,
    onLoadingChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit = {},
    onRestoreSuccess: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val errorMessage = state.error
    val isLoading = state.isLoading

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
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            if (errorMessage == null) {
                Paywall(
                    options = PaywallOptions.Builder(dismissRequest = onDismiss)
                        .setShouldDisplayDismissButton(true)
                        .setListener(object : PaywallListener {
                            override fun onPurchaseStarted(rcPackage: com.revenuecat.purchases.Package) {
                                Log.d(TAG, "Purchase started")
                                onLoadingChanged(true)
                                onError(null)
                            }

                            override fun onPurchaseCompleted(
                                customerInfo: CustomerInfo,
                                storeTransaction: StoreTransaction
                            ) {
                                Log.d(TAG, "Purchase completed")
                                onLoadingChanged(false)
                                onPurchaseSuccess()
                            }

                            override fun onPurchaseError(error: PurchasesError) {
                                Log.e(TAG, "Purchase error: ${error.message}")
                                onLoadingChanged(false)
                                onError(error.message)
                            }

                            override fun onRestoreStarted() {
                                Log.d(TAG, "Restore started")
                                onLoadingChanged(true)
                                onError(null)
                            }

                            override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                                Log.d(TAG, "Restore completed")
                                onLoadingChanged(false)
                                onRestoreSuccess()
                            }

                            override fun onRestoreError(error: PurchasesError) {
                                Log.e(TAG, "Restore error: ${error.message}")
                                onLoadingChanged(false)
                                onError(error.message)
                            }
                        })
                        .build()
                )
            }
        }
    }
}
