package com.ist.instocktracker.services

import com.google.genai.Client
import io.ktor.server.application.*

class GenAI() {
    private lateinit var geminiApiKey: String

    constructor(application: Application) : this() {
        geminiApiKey = application.environment.config.property("app.gemini.apiKey").getString()
    }

    val geminiClient: Client by lazy { Client.builder().apiKey(geminiApiKey).build() }


}