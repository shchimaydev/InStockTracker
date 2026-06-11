package com.ist.instocktracker.feature.billing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Route level composable for the Paywall feature.
 * Manages the ViewModel, state collection, and event mapping.
 */
@Composable
fun PaywallRoute(
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    onRestoreSuccess: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val state by viewModel.subscriptionState.collectAsState()

    PaywallScreen(
        state = state,
        onLoadingChanged = viewModel::setLoading,
        onError = viewModel::setError,
        onDismiss = onDismiss,
        onPurchaseSuccess = onPurchaseSuccess,
        onRestoreSuccess = onRestoreSuccess,
        onRetry = viewModel::refreshSubscriptionStatus
    )
}
