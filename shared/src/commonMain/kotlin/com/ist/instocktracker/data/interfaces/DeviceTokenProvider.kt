package com.ist.instocktracker.data.interfaces

import com.ist.instocktracker.data.Platform

interface DeviceTokenProvider {
    suspend fun getCurrentToken(): String?
    val platform: Platform
    var deviceToken: String?

}