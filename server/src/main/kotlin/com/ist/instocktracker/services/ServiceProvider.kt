package com.ist.instocktracker.services

import com.google.cloud.firestore.Firestore
import com.ist.instocktracker.repositories.LinkItemRepository
import com.ist.instocktracker.repositories.UserRepository
import com.ist.instocktracker.services.config.AppConfig
import com.ist.instocktracker.services.config.JwtConfig
import com.ist.instocktracker.services.db.FirestoreProvider
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json

object ServiceProvider {
    val idTokenVerifierService: IdTokenVerifierService by lazy { IdTokenVerifierService() }
    lateinit var config: AppConfig

    lateinit var jwtConfig: JwtConfig
    lateinit var db: Firestore
    lateinit var genAi: GenAI
    lateinit var userRepository: UserRepository
    lateinit var linkItemRepository: LinkItemRepository
    lateinit var pushProvider: PushProvider
    lateinit var notificationService: NotificationService
    lateinit var schedulerService: SchedulerService
    lateinit var httpClient: HttpClient

    fun init(application: Application) {
        config = AppConfig(application)
        jwtConfig = JwtConfig.fromEnvironment(application.environment)
        db = FirestoreProvider.db
        genAi = GenAI(config.genAIToken)
        userRepository = UserRepository(db)
        linkItemRepository = LinkItemRepository(db)
        pushProvider = PushProvider
        notificationService = NotificationService(userRepository)

        // Init scheduler service
        schedulerService = SchedulerService(
            appConfig = config
        )
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json { ignoreUnknownKeys = true; isLenient = true }
                )
            }
        }
    }

    fun stop() {
        schedulerService.close()
        httpClient.close()
        db.close()
    }
}