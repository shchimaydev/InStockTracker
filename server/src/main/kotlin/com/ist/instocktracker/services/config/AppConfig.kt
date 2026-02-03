package com.ist.instocktracker.services.config

import io.ktor.server.application.*

class AppConfig(application: Application) {
    val projectId: String = application.environment.config.propertyOrNull("storage.projectId")?.getString()
        ?.split("~")?.getOrNull(1) ?: "instocktracker-464721"

    val location: String = application.environment.config.property("storage.location").getString()

    val serverUrl: String = "https://$projectId.ey.r.appspot.com"

    val browserlessToken: String = application.environment.config.property("app.browserlessioToken").getString()
    val genAIToken: String = application.environment.config.property("app.gemini.apiKey").getString()
    val schedulerCallerSa: String = application.environment.config.property("app.schedulerCallerSa").getString()
}
