package com.ist.instocktracker.repositories

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.mappers.toLinkItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LinkItemRepository(db: Firestore) {
    val collection = db.collection("links")

    suspend fun exists(id: String): Pair<Boolean, DocumentReference?> {
        return try {
            withContext(Dispatchers.IO) {
                val reference = collection.document(id)
                val exists = reference.get().get().exists()
                exists to reference
            }
        } catch (e: Exception) {
            println("Error checking for link item existence: ${e.message}")
            false to null
        }
    }

    suspend fun save(linkItem: LinkItem): LinkItem? {
        return try {
            withContext(Dispatchers.IO) {
                val docRef = if (linkItem.id.isBlank()) {
                    // Auto-generate ID if not provided
                    collection.document()
                } else {
                    // Use provided ID
                    collection.document(linkItem.id)
                }
                docRef.set(linkItem.toMap()).get()
                docRef.get().get().toLinkItem()
            }
        } catch (e: Exception) {
            println("Error saving link item: ${e.message}")
            null
        }
    }

    fun getReference(id: String): DocumentReference {
        return collection.document(id)
    }

    suspend fun getAll(userSub: String): List<LinkItem> {
        return try {
            withContext(Dispatchers.IO) {
                collection.whereEqualTo("userId", userSub).get().get().documents.mapNotNull { it.toLinkItem() }
            }
        } catch (e: Exception) {
            println("Error getting all link items: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAll(): List<LinkItem> {
        return try {
            withContext(Dispatchers.IO) {
                collection.get().get().documents.mapNotNull { it.toLinkItem() }
            }
        } catch (e: Exception) {
            println("Error getting all link items: ${e.message}")
            emptyList()
        }
    }


    suspend fun get(id: String): LinkItem? {
        val docRef = collection.document(id) // Get the DocumentReference
        return getLinkItemFromSnapshot(docRef) // Call the helper function
    }

    // Overloaded function that takes a DocumentReference directly
    suspend fun get(docRef: DocumentReference): LinkItem? {
        return getLinkItemFromSnapshot(docRef) // Call the helper function
    }

    suspend fun getLinkItemFromSnapshot(docRef: DocumentReference): LinkItem? {
        return try {
            withContext(Dispatchers.IO) {
                docRef.get().get().toLinkItem()
            }
        } catch (e: Exception) {
            println("Error getting link item: ${e.message}") // Centralized error logging
            null // Return null on error
        }
    }

    suspend fun count(userId: String): Int {
        return try {
            withContext(Dispatchers.IO) {
                collection.whereEqualTo("userId", userId).get().get().size()
            }
        } catch (e: Exception) {
            println("Error counting link items: ${e.message}")
            0
        }
    }
}