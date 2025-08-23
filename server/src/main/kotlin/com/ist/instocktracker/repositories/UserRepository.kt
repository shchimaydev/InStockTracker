package com.ist.instocktracker.repositories

import com.google.cloud.firestore.Firestore
import com.ist.instocktracker.data.User
import com.ist.instocktracker.data.mappers.toUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(db: Firestore) {
    val collection = db.collection("users")

    suspend fun get(userId: String?): User? {
        return try {
            userId?.let {
                withContext(Dispatchers.IO) {
                    collection.document(userId).get().get().toUser()
                }
            }
        } catch (e: Exception) {
            println("Error getting user: ${e.message}")
            null
        }
    }

    suspend fun save(user: User): User? {
        return try {
            val docRef = collection.document()
            withContext(Dispatchers.IO) {
                collection.document(user.id).set(user).get()
            }

            val snapshot = withContext(Dispatchers.IO) {
                docRef.get().get()
            }

            snapshot.toUser()
        } catch (e: Exception) {
            println("Error saving user: ${e.message}")
            null
        }
    }

    suspend fun exists(userId: String): Boolean {
        return try {
            val documentReference = collection.document(userId)
            val snapshot = withContext(Dispatchers.IO) {
                documentReference.get().get()
            }

            snapshot.exists()
        } catch (e: Exception) {
            println("Error checking for user existence: ${e.message}")
            false
        }
    }


}