package com.ist.instocktracker.services

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.ist.instocktracker.Api
import com.ist.instocktracker.data.TokenDataStore
import com.ist.instocktracker.data.auth.SessionManager
import com.ist.instocktracker.data.interfaces.TokenStore

object ServiceLocator {
    lateinit var tokenStore: TokenStore
    lateinit var api: Api
    lateinit var sessionManager: SessionManager


    fun init(context: Context) {
        val isDev = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        Log.d("ServiceLocator", "Initializing Service Locator with isDev: $isDev")
        tokenStore = TokenDataStore(context.applicationContext)
        api = Api(tokenStore, isDev)
        sessionManager = AndroidSessionManager(context.applicationContext, tokenStore, api)
    }
}