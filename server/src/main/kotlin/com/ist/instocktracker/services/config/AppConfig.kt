package com.ist.instocktracker.services.config

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import java.io.File

class AppConfig(application: Application) {
    // Reads secrets from a local `.env` file (gitignored) in development, and
    // falls back to real process environment variables in production — on App
    // Engine these are injected via `env_secrets.yaml` (included from app.yaml).
    //
    // The `.env` lives in the server module dir, but the working directory differs
    // between Gradle (`server/`) and an IDE "run main()" (repo root), so we probe
    // the likely locations. In production no `.env` exists and we fall back to env vars.
    private val config = application.environment.config

    private val env = dotenv {
        directory = listOf(".", "server", "../server")
            .firstOrNull { File(it, ".env").exists() } ?: "."
        ignoreIfMissing = true
    }

    // Resolves a secret from the `.env` file / environment variables, falling back
    // to the Ktor config (used by tests that inject values via MapApplicationConfig).
    private fun secret(envKey: String, configPath: String): String =
        env[envKey]
            ?: config.propertyOrNull(configPath)?.getString()
            ?: error("Missing required secret: $envKey")

    val projectId: String = config.propertyOrNull("storage.projectId")?.getString()
        ?.split("~")?.getOrNull(1) ?: "instocktracker-464721"

    val location: String = config.property("storage.location").getString()

    val serverUrl: String = "https://$projectId.ey.r.appspot.com"

    val browserlessToken: String = secret("BROWSERLESSIO_TOKEN", "app.browserlessioToken")
    val genAIToken: String = secret("GEMINI_API_KEY", "app.gemini.apiKey")
    val schedulerCallerSa: String = secret("SCHEDULER_CALLER_SA", "app.schedulerCallerSa")
}
