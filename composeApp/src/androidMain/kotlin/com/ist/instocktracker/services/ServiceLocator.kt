package com.ist.instocktracker.services

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.ist.instocktracker.Api
import com.ist.instocktracker.data.TokenDataStore
import com.ist.instocktracker.data.auth.SessionManager
import com.ist.instocktracker.data.interfaces.DeviceTokenProvider
import com.ist.instocktracker.data.interfaces.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow

object ServiceLocator {
    lateinit var tokenStore: TokenStore
    lateinit var api: Api
    lateinit var sessionManager: SessionManager
    lateinit var deviceTokenManager: DeviceTokenProvider

    /** Bumped whenever a LinkItem may have changed outside the main list's own screen (e.g. a subscription-driven freeze/unfreeze), so the main list knows to refetch. */
    val linkItemsChanged = MutableStateFlow(0)

    fun init(context: Context) {
        val isDev = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        Log.d("ServiceLocator", "Initializing Service Locator with isDev: $isDev")


        tokenStore = TokenDataStore(context.applicationContext)
        deviceTokenManager = AndroidDeviceTokenManager(tokenStore)
        api = Api(tokenStore, isDev)
        sessionManager = AndroidSessionManager(context.applicationContext, tokenStore, api)
    }
}