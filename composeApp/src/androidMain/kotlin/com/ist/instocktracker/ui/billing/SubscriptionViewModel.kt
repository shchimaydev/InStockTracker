package com.ist.instocktracker.ui.billing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ist.instocktracker.CustomerInfoWrapper
import com.ist.instocktracker.billing.RevenueCatManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "SubscriptionViewModel"

/**
 * Data class representing subscription state
 */
data class SubscriptionState(
    val isSubscribed: Boolean = false,
    val isPremium: Boolean = false,
    val activeSubscriptions: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for managing subscription state
 * Observes CustomerInfo updates and provides subscription status to UI
 */
class SubscriptionViewModel : ViewModel() {

    private val _subscriptionState = MutableStateFlow(SubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    init {
        // Listen to CustomerInfo updates
        viewModelScope.launch {
            RevenueCatManager.customerInfo.collectLatest { info ->
                if (info != null) {
                    updateSubscriptionState(info)
                }
            }
        }
        // Load initial subscription state
        refreshSubscriptionStatus()
    }

    /**
     * Refresh subscription status manually
     */
    fun refreshSubscriptionStatus() {
        viewModelScope.launch {
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)
            try {
                val customerInfo = RevenueCatManager.fetchCustomerInfo()
                if (customerInfo != null) {
                    updateSubscriptionState(customerInfo)
                } else {
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = "Failed to load subscription status"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing subscription status", e)
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Update subscription state from CustomerInfo
     */
    private fun updateSubscriptionState(customerInfo: CustomerInfoWrapper) {
        val hasActiveEntitlements = customerInfo.activeEntitlements.isNotEmpty()
        val isPremium = customerInfo.isPremium

        _subscriptionState.value = SubscriptionState(
            isSubscribed = hasActiveEntitlements,
            isPremium = isPremium,
            activeSubscriptions = customerInfo.activeSubscriptions,
            isLoading = false,
            error = null
        )

        Log.d(TAG, "Subscription state updated: isSubscribed=$hasActiveEntitlements, isPremium=$isPremium")
    }

    /**
     * Restore purchases
     */
    fun restorePurchases() {
        viewModelScope.launch {
            _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, error = null)
            try {
                val customerInfo = RevenueCatManager.restorePurchases()
                if (customerInfo != null) {
                    updateSubscriptionState(customerInfo)
                } else {
                    _subscriptionState.value = _subscriptionState.value.copy(
                        isLoading = false,
                        error = "No purchases to restore"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring purchases", e)
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to restore purchases"
                )
            }
        }
    }

    /**
     * Check if user has a specific entitlement
     */
    suspend fun hasEntitlement(entitlementId: String): Boolean {
        return RevenueCatManager.hasActiveEntitlement(entitlementId)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
