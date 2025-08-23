package com.ist.instocktracker.services.db

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions

object FirestoreProvider {

    // You can get your project ID from the Google Cloud Console
    private const val PROJECT_ID = "instocktracker-464721"


    // Use a lazy delegate to initialize the client only when it's first needed.
    val db: Firestore by lazy {
        val firestoreOptions = FirestoreOptions.newBuilder()
            .setProjectId(PROJECT_ID)
            .setDatabaseId("instocktracker-main")
            .build()

        firestoreOptions.service
    }


    fun checkConnection() {
        return try {
            println("Attempting to connect to Firestore...")
            val collectionIds = db.listCollections().map { it.id }
            println("Successfully connected to Firestore. Found collections: ${collectionIds}.")
        } catch (e: Exception) {
            println("Error connecting to Firestore: ${e.message}")
            e.printStackTrace()
        }
    }

}
