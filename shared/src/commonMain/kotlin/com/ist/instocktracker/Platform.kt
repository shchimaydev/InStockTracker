package com.ist.instocktracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

interface CustomerInfoWrapper {
    val activeEntitlements: Set<String>
    val isPremium: Boolean
    val activeSubscriptions: Set<String>
}