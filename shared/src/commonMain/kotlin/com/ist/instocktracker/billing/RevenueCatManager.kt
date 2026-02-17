package com.ist.instocktracker.billing

import com.ist.instocktracker.CustomerInfoWrapper
import kotlinx.coroutines.flow.StateFlow

expect object RevenueCatManager {
    val customerInfo: StateFlow<CustomerInfoWrapper?>

    fun initialize(apiKey: String)
    suspend fun identifyUser(userId: String)
    suspend fun logoutUser()
    suspend fun fetchCustomerInfo(): CustomerInfoWrapper?
    suspend fun hasActiveEntitlement(entitlementId: String): Boolean
    suspend fun restorePurchases(): CustomerInfoWrapper?
}
