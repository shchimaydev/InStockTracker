package com.ist.instocktracker.data

import com.ist.instocktracker.DocumentId

data class LinkItemLog(
    @DocumentId
    val id: String = "",
    val timestamp: String,
    val jobStatus: Boolean,
    val mode: Mode,
    /**
     * Boolean result of whether item's status on the page equal to LinkItem.mode
     */
    val result: Boolean
)

