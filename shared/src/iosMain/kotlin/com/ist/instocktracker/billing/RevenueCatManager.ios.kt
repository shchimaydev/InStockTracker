package com.ist.instocktracker.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

actual object RevenueCatManager : BaseRevenueCatManager() {

    actual fun initialize(apiKey: String) {
        val configuration = PurchasesConfiguration(apiKey)
        Purchases.configure(configuration)
        Purchases.sharedInstance.delegate = this
    }
}
