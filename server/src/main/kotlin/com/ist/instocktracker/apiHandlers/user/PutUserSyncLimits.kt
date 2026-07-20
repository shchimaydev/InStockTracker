package com.ist.instocktracker.apiHandlers.user

import com.ist.instocktracker.data.ApiError
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.LinkItemInfo
import com.ist.instocktracker.data.SyncLimitsResult
import com.ist.instocktracker.plugins.getUser
import com.ist.instocktracker.services.ServiceProvider
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Handler for PUT /api/v1/user/sync-limits endpoint
 * Synchronizes the user's item limit with their current subscription tier.
 *
 * If the new limit is lower than the number of currently-unfrozen items, the least-recently-updated
 * excess items are frozen (scheduler paused, kept in place). If the new limit is higher and some items
 * are frozen, the most-recently-updated frozen items are unfrozen first (scheduler resumed), up to the
 * newly available capacity.
 */
fun Route.putUserSyncLimits() {
    put("sync-limits") {
        val userRepository = ServiceProvider.userRepository
        val linkItemRepository = ServiceProvider.linkItemRepository
        val schedulerService = ServiceProvider.schedulerService
        val currentUser = call.getUser()

        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
            ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ApiError(error = "Missing or invalid 'limit' parameter")
            )

        try {
            val items = linkItemRepository.getAll(currentUser.id)
            val unfrozen = items.filter { !it.isFrozen }
            val frozen = items.filter { it.isFrozen }
            val delta = limit - unfrozen.size

            val itemsToFreeze: List<LinkItem>
            val itemsToUnfreeze: List<LinkItem>

            if (delta < 0) {
                itemsToFreeze = unfrozen.sortedBy { it.updatedAt }.take(-delta)
                itemsToUnfreeze = emptyList()
            } else if (delta > 0 && frozen.isNotEmpty()) {
                itemsToFreeze = emptyList()
                itemsToUnfreeze = frozen.sortedByDescending { it.updatedAt }.take(delta)
            } else {
                itemsToFreeze = emptyList()
                itemsToUnfreeze = emptyList()
            }

            itemsToFreeze.forEach { item ->
                linkItemRepository.setFrozen(item.id, true)
                item.scheduleJobId?.let { schedulerService.pauseJob(it) }
            }
            itemsToUnfreeze.forEach { item ->
                linkItemRepository.setFrozen(item.id, false)
                item.scheduleJobId?.let { schedulerService.resumeJob(it) }
            }

            val newUnfrozenCount = unfrozen.size - itemsToFreeze.size + itemsToUnfreeze.size
            val remaining = limit - newUnfrozenCount

            userRepository.setTrackableItemsLeft(currentUser.id, remaining)

            return@put call.respond(
                HttpStatusCode.OK,
                SyncLimitsResult(
                    trackableItemsLeft = remaining,
                    frozenItems = itemsToFreeze.map { LinkItemInfo(it.id, it.label) },
                    unfrozenItems = itemsToUnfreeze.map { LinkItemInfo(it.id, it.label) }
                )
            )
        } catch (e: Exception) {
            return@put call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(error = "Failed to sync limits: ${e.message}")
            )
        }
    }
}
