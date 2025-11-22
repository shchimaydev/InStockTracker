package com.ist.instocktracker.services

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

object PushProvider {
    val app: FirebaseApp by lazy { FirebaseApp.initializeApp() }
    val messaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance(app) }
}