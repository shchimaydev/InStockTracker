package com.ist.instocktracker.billing

import com.ist.instocktracker.CustomerInfoWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual object RevenueCatManager {
    private val _customerInfo = MutableStateFlow<CustomerInfoWrapper?>(null)
    actual val customerInfo: StateFlow<CustomerInfoWrapper?> = _customerInfo.asStateFlow()

    actual fun initialize(apiKey: String) {}
    actual suspend fun identifyUser(userId: String) {}
    actual suspend fun logoutUser() {}
    actual suspend fun fetchCustomerInfo(): CustomerInfoWrapper? = null
    actual suspend fun hasActiveEntitlement(entitlementId: String): Boolean = false
    actual suspend fun restorePurchases(): CustomerInfoWrapper? = null
}
