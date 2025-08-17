package com.ist.instocktracker.services

import android.content.Context
import com.ist.instocktracker.Api
import com.ist.instocktracker.data.TokenDataStore
import com.ist.instocktracker.data.auth.SessionManager
import com.ist.instocktracker.data.interfaces.TokenStore

object ServiceLocator {
    lateinit var tokenStore: TokenStore
    lateinit var api: Api
    lateinit var sessionManager: SessionManager


    fun init(context: Context) {
        tokenStore = TokenDataStore(context.applicationContext)
        api = Api(tokenStore)
        sessionManager = AndroidSessionManager(context.applicationContext, tokenStore, api)
    }
}