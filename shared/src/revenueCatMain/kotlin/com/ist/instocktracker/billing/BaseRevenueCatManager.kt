package com.ist.instocktracker.billing

import com.ist.instocktracker.CustomerInfoWrapper
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RevenueCatCustomerInfoWrapper(private val info: CustomerInfo) : CustomerInfoWrapper {
    override val activeEntitlements: Set<String> get() = info.entitlements.active.keys
    override val isPremium: Boolean get() = info.entitlements.active.containsKey("premium")
    override val activeSubscriptions: Set<String> get() = info.activeSubscriptions
}

abstract class BaseRevenueCatManager : PurchasesDelegate {
    protected val _customerInfo = MutableStateFlow<CustomerInfoWrapper?>(null)
    val customerInfo: StateFlow<CustomerInfoWrapper?> = _customerInfo.asStateFlow()

    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
        _customerInfo.value = RevenueCatCustomerInfoWrapper(customerInfo)
    }

    override fun onPurchasePromoProduct(
        product: StoreProduct,
        startPurchase: (
            onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
            onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit
        ) -> Unit
    ) {
        // Default implementation does nothing
    }

    suspend fun identifyUser(userId: String) {
        try {
            val result = Purchases.sharedInstance.awaitLogIn(userId)
            _customerInfo.value = RevenueCatCustomerInfoWrapper(result.customerInfo)
        } catch (e: Exception) {
        }
    }

    suspend fun logoutUser() {
        try {
            val info = Purchases.sharedInstance.awaitLogOut()
            _customerInfo.value = RevenueCatCustomerInfoWrapper(info)
        } catch (e: Exception) {
        }
    }

    suspend fun fetchCustomerInfo(): CustomerInfoWrapper? {
        return try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            val wrapped = RevenueCatCustomerInfoWrapper(info)
            _customerInfo.value = wrapped
            wrapped
        } catch (e: Exception) {
            null
        }
    }

    suspend fun hasActiveEntitlement(entitlementId: String): Boolean {
        val info = fetchCustomerInfo() ?: return false
        return info.activeEntitlements.contains(entitlementId)
    }

    suspend fun restorePurchases(): CustomerInfoWrapper? {
        return try {
            val info = Purchases.sharedInstance.awaitCustomerInfo()
            val wrapped = RevenueCatCustomerInfoWrapper(info)
            _customerInfo.value = wrapped
            wrapped
        } catch (e: Exception) {
            null
        }
    }
}
