package com.ist.instocktracker.data

import kotlinx.serialization.Serializable

@Serializable
data class LinkItemInfo(val id: String, val label: String? = null)

@Serializable
data class SyncLimitsResult(
    val trackableItemsLeft: Int,
    val frozenItems: List<LinkItemInfo>,
    val unfrozenItems: List<LinkItemInfo>
)
