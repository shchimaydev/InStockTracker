package com.ist.instocktracker.services

import com.google.cloud.firestore.Firestore
import com.ist.instocktracker.repositories.LinkItemRepository
import com.ist.instocktracker.repositories.UserRepository
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.server.application.*

object ServiceProvider {
    val idTokenVerifierService: IdTokenVerifierService by lazy { IdTokenVerifierService() }
    lateinit var db: Firestore
    lateinit var genAi: GenAI
    lateinit var userRepository: UserRepository
    lateinit var linkItemRepository: LinkItemRepository
    lateinit var pushProvider: PushProvider
    lateinit var notificationService: NotificationService
    lateinit var schedulerService: SchedulerService

    fun init(application: Application) {
        db = FirestoreProvider.db
        genAi = GenAI(application)
        userRepository = UserRepository(db)
        linkItemRepository = LinkItemRepository(db)
        pushProvider = PushProvider
        notificationService = NotificationService(userRepository)

        // Init scheduler service
        println("Project ID from environment: ${System.getenv("GAE_APPLICATION")}")
        val projectId = System.getenv("GAE_APPLICATION")?.split("~")?.getOrNull(1) ?: "instocktracker-464721"
        val location = "europe-west3"
        val serverUrl = "https://$projectId.ey.r.appspot.com"
        val schedulerService = SchedulerService(
            location = location,
            serverBaseUrl = serverUrl
        )
        this.schedulerService = schedulerService
    }
}