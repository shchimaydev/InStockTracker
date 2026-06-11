package com.ist.instocktracker.data.mappers

import com.google.cloud.firestore.DocumentSnapshot
import com.ist.instocktracker.data.DeviceToken
import com.ist.instocktracker.data.Platform
import com.ist.instocktracker.data.User

fun DocumentSnapshot.toUser(): User? {
    return try {
        // The `data` property gives you a Map<String, Any> of the document's fields.
        val data = this.data ?: return null

        // Safely extract each field with type casting and provide defaults.
        // The document ID is on the snapshot itself, not in the data map.
        val id = this.id
        val name = data["name"] as? String // Safe cast returns null if "name" isn't a String or is missing
        val email = data["email"] as? String
        val googleIdToken = data["googleIdToken"] as? String ?: ""
        val trackableItemsLeft = (data["trackableItemsLeft"] as? Number)?.toInt() ?: 3

        val deviceTokens = (data["deviceTokens"] as? List<Map<String, Any>>)?.mapNotNull { tokenData ->
            val token = tokenData["token"] as? String
            val platformStr = tokenData["platform"] as? String
            val createdAt = tokenData["createdAt"] as? Long

            if (token != null && platformStr != null && createdAt != null) {
                try {
                    DeviceToken(token, Platform.valueOf(platformStr), createdAt)
                } catch (e: Exception) {
                    null
                }
            } else null
        } ?: emptyList()

        User(
            id = id,
            name = name,
            email = email,
            googleIdToken = googleIdToken,
            deviceTokens = deviceTokens,
            trackableItemsLeft = trackableItemsLeft
        )
    } catch (e: Exception) {
        println("Error mapping snapshot to User: ${e.message}")
        null
    }
}