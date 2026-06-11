package com.ist.instocktracker.feature.linkitem

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ist.instocktracker.services.ServiceLocator
import com.ist.instocktracker.utils.LinkItemFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddFromSharedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    fun createLinkItemFromShape(url: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val user = ServiceLocator.api.getCurrentUser()
                if (user.trackableItemsLeft <= 0) {
                    throw IllegalStateException("You have reached your limit of trackable items. Please upgrade your subscription.")
                }

                val newLinkItem = LinkItemFactory.createLinkItemFromSharedUrl(url, user.id)
                val createdLinkItem = ServiceLocator.api.createLinkItem(newLinkItem)

                Log.d("AddFromShareViewModel", "New LinkItem: $newLinkItem")
                delay(5000)

                onSuccess(createdLinkItem.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class UIState(
    val isLoading: Boolean = false,
    val error: String? = null
)