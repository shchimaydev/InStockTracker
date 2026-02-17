package com.ist.instocktracker.billing

import android.app.Application
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

actual object RevenueCatManager : BaseRevenueCatManager() {

    fun initializeWithContext(application: Application, apiKey: String) {
        val configuration = PurchasesConfiguration(apiKey)
        Purchases.configure(configuration)
        initialize(apiKey)
    }

    actual fun initialize(apiKey: String) {
        // Updated CustomerInfo is automatically handled by other methods like identifyUser, fetchCustomerInfo, etc.
        // In this version of KMP SDK, we don't have a direct setOnUpdatedCustomerInfoListener on the KMP Purchases object.
    }
}
