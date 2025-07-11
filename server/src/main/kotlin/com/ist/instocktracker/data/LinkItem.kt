package com.ist.instocktracker.data

import com.google.cloud.firestore.DocumentSnapshot

fun DocumentSnapshot.toLinkItem(): LinkItem? {
    if (!exists()) {
        return null
    }
    // Manually map the fields, including the ID from the snapshot
    return LinkItem(
        id = this.id,
        link = this.getString("link") ?: "",
        mode = this.getString("mode")?.let { Mode.valueOf(it) } ?: Mode.OUT_OF_STOCK,
        additionalInstructions = this.getString("additionalInstructions"),
        isActive = this.getBoolean("isActive") ?: false
    )
}
