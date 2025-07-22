package com.ist.instocktracker.data

data class LinkItemLog(
    val timestamp: String,
    val jobStatus: Boolean,
    /**
     * Boolean result of whether item's status on the page equal to LinkItem.mode
     */
    val result: Boolean
)
