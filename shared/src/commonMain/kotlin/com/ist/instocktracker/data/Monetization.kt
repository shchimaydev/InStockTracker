package com.ist.instocktracker.data

enum class SubscriptionTier(val entitlementId: String?, val maxItems: Int) {
    FREE(null, 3),
    BASE("sub_5_items", 5),
    STANDARD("sub_10_items", 10),
    ULTRA("sub_20_items", 20);

    companion object {
        fun fromEntitlementId(entitlementId: String?): SubscriptionTier {
            return entries.find { it.entitlementId == entitlementId } ?: FREE
        }
    }
}
