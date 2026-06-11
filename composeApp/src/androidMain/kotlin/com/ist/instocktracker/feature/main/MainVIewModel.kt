package com.ist.instocktracker.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ist.instocktracker.Api
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.services.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Filter applied to the main list, by [LinkItem.isActive]. */
enum class LinkFilter(val label: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ALL("All")
}

class MainVIewModel(val api: Api) : ViewModel() {

    val linkItems = MutableStateFlow(emptyList<LinkItem>())

    /** Selected filter — defaults to showing only active items. */
    val filter = MutableStateFlow(LinkFilter.ACTIVE)

    /** [linkItems] narrowed by the current [filter]. */
    val visibleItems = combine(linkItems, filter) { items, f ->
        when (f) {
            LinkFilter.ACTIVE -> items.filter { it.isActive }
            LinkFilter.INACTIVE -> items.filter { !it.isActive }
            LinkFilter.ALL -> items
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(f: LinkFilter) {
        filter.value = f
    }

    suspend fun getLinkItems() {
        try {
            val list = ServiceLocator.sessionManager.runWithAuth { api.getLinkItemsForUser() }
            linkItems.value = list


        } catch (e: Exception) {
            Log.e("MainViewModel", "Error getting link items: ${e.message}")
        }
    }
}