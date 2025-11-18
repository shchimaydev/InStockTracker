package com.ist.instocktracker.services

import com.google.firebase.messaging.FirebaseMessaging
import com.ist.instocktracker.data.Platform
import com.ist.instocktracker.data.interfaces.DeviceTokenProvider
import com.ist.instocktracker.data.interfaces.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidDeviceTokenManager(private val tokenStore: TokenStore) : DeviceTokenProvider {
    override val platform = Platform.ANDROID
    override var deviceToken: String? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun getCurrentToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener {
                scope.launch { tokenStore.saveDeviceToken(it) }
                cont.resume(it, null)
            }
            .addOnFailureListener { cont.resume(null, null) }

    }
}