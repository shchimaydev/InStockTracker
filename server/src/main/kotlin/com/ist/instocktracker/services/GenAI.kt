package com.ist.instocktracker.services

import com.google.genai.Client

class GenAI() {
    private lateinit var geminiApiKey: String

    constructor(apiKey: String) : this() {
        geminiApiKey = apiKey
    }

    val geminiClient: Client by lazy { Client.builder().apiKey(geminiApiKey).build() }


}