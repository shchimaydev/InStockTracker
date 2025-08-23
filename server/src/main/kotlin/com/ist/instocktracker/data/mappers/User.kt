package com.ist.instocktracker.data.mappers

import com.google.cloud.firestore.DocumentSnapshot
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
        val googleIdToken = data["googleIdToken"] as? String

        // You must have an ID and a token to create a valid user.
        if (googleIdToken == null) {
            println("Error: Document ${this.id} is missing 'googleIdToken'. Cannot map to User.")
            return null
        }

        User(
            id = id,
            name = name,
            email = email,
            googleIdToken = googleIdToken
        )
    } catch (e: Exception) {
        println("Error mapping snapshot to User: ${e.message}")
        null
    }
}