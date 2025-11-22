package com.ist.instocktracker.services

import com.google.cloud.firestore.Firestore
import com.ist.instocktracker.repositories.LinkItemRepository
import com.ist.instocktracker.repositories.UserRepository
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.server.application.*

object ServiceProvider {
    val idTokenVerifierService: IdTokenVerifierService by lazy { IdTokenVerifierService() }
    lateinit var db: Firestore;
    lateinit var genAi: GenAI;
    lateinit var userRepository: UserRepository;
    lateinit var linkItemRepository: LinkItemRepository;
    lateinit var pushProvider: PushProvider
    lateinit var notificationService: NotificationService

    fun init(application: Application) {
        db = FirestoreProvider.db
        genAi = GenAI(application)
        userRepository = UserRepository(db)
        linkItemRepository = LinkItemRepository(db)
        pushProvider = PushProvider
        notificationService = NotificationService(userRepository)
    }
}