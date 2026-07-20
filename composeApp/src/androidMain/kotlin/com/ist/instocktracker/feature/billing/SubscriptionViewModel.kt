package com.ist.instocktracker.feature.billing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ist.instocktracker.CustomerInfoWrapper
import com.ist.instocktracker.billing.RevenueCatManager
import com.ist.instocktracker.data.SubscriptionTier
import com.ist.instocktracker.data.SyncLimitsResult
import com.ist.instocktracker.services.ServiceLocator
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SubscriptionViewModel"

/**
 * Data class representing subscription state
 */
data class SubscriptionState(
    val isSubscribed: Boolean = false,
    val isPremium: Boolean = false,
    val activeSubscriptions: Set<String> = emptySet(),
    val tier: SubscriptionTier = SubscriptionTier.FREE,
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

    private val _syncLimitsNotice = MutableStateFlow<SyncLimitsResult?>(null)
    /** Non-null when the most recent limit sync froze or unfroze items — shown as a one-time notice. */
    val syncLimitsNotice: StateFlow<SyncLimitsResult?> = _syncLimitsNotice.asStateFlow()

    fun dismissSyncLimitsNotice() {
        _syncLimitsNotice.value = null
    }

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
     * Set loading state
     */
    fun setLoading(isLoading: Boolean) {
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = isLoading)
    }

    /**
     * Set error message
     */
    fun setError(message: String?) {
        _subscriptionState.value = _subscriptionState.value.copy(error = message, isLoading = false)
    }

    /**
     * Clear error
     */
    fun clearError() {
        _subscriptionState.value = _subscriptionState.value.copy(error = null)
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

        Log.d(TAG, "CustomerInfo on update: ${customerInfo.activeEntitlements}, ${customerInfo.activeSubscriptions}")

        // Determine the highest tier based on active entitlements
        val currentTier = SubscriptionTier.entries
            .filter { it.entitlementId != null && it.entitlementId in customerInfo.activeSubscriptions }
            .maxByOrNull { it.maxItems }
            ?: SubscriptionTier.FREE

        val previousTier = _subscriptionState.value.tier

        _subscriptionState.value = SubscriptionState(
            isSubscribed = hasActiveEntitlements,
            isPremium = isPremium,
            activeSubscriptions = customerInfo.activeSubscriptions,
            tier = currentTier,
            isLoading = false,
            error = null
        )

        if (currentTier != previousTier) {
            syncLimitsWithServer(currentTier)
        }

        Log.d(
            TAG,
            "Subscription state updated: isSubscribed=$hasActiveEntitlements, isPremium=$isPremium, tier=$currentTier"
        )
    }

    private fun syncLimitsWithServer(tier: SubscriptionTier) {
        viewModelScope.launch {
            try {
                val result = withContext(NonCancellable) {
                    ServiceLocator.api.syncLimits(tier.maxItems)
                }
                Log.d(TAG, "Successfully synced limits with server for tier: $tier")
                if (result.frozenItems.isNotEmpty() || result.unfrozenItems.isNotEmpty()) {
                    _syncLimitsNotice.value = result
                    ServiceLocator.linkItemsChanged.value++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync limits with server", e)
            }
        }
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
