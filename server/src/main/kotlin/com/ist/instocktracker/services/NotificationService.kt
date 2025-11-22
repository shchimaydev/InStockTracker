package com.ist.instocktracker.services

import com.google.firebase.ErrorCode
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.ist.instocktracker.data.PushPayload
import com.ist.instocktracker.data.User
import com.ist.instocktracker.data.removeTokens
import com.ist.instocktracker.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationService(val userRepository: UserRepository = ServiceProvider.userRepository) {
    suspend fun sendToUser(user: User, payload: PushPayload) {
        val tokens = user.deviceTokens.map { it.token }

        if (tokens.isEmpty()) return

        // Send data-only payloads so Android client service handles notification and deep link
        val messages = tokens.map { token ->
            Message.builder()
                .setToken(token)
                .putData("title", payload.title)
                .putData("body", payload.body)
                .putData("linkItemId", payload.linkItemId)
                .build()
        }

        val response = withContext(Dispatchers.IO) {
            PushProvider.messaging.sendEach(messages)
        }


        // Optionally prune invalid tokens
        val invalidTokens = response.responses.mapIndexedNotNull { idx, r ->
            if (!r.isSuccessful && r.exception is FirebaseMessagingException) {
                val ex = r.exception as FirebaseMessagingException
                if (ex.errorCode in listOf(MessagingErrorCode.UNREGISTERED, ErrorCode.INVALID_ARGUMENT)) idx else null
            } else null
        }.map { tokens[it] }
        if (invalidTokens.isNotEmpty()) {
            userRepository.save(user.removeTokens(invalidTokens))
        }

    }
}