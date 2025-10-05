package com.ist.instocktracker.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ist.instocktracker.Api
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.services.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow

class MainVIewModel(val api: Api) : ViewModel() {

    val linkItems = MutableStateFlow(emptyList<LinkItem>())

    suspend fun getLinkItems() {
        try {
            val list = ServiceLocator.sessionManager.runWithAuth { api.getLinkItemsForUser() }
            linkItems.value = list


        } catch (e: Exception) {
            Log.e("MainViewModel", "Error getting link items: ${e.message}")
        }
    }
}