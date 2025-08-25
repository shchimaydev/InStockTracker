package com.ist.instocktracker.feature.main

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ist.instocktracker.Api
import com.ist.instocktracker.data.LinkItem
import kotlinx.coroutines.flow.MutableStateFlow

class MainVIewModel(val api: Api) : ViewModel() {

    val linkItems = MutableStateFlow(emptyList<LinkItem>())

    suspend fun getLinkItems() {
        try {
            val res = api.getLinkItemsForUser()
            linkItems.value = res


        } catch (e: Exception) {
            Log.e("MainViewModel", "Error getting link items: ${e.message}")
        }
    }
}