package com.ist.instocktracker.feature.linkitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ist.instocktracker.data.LinkItem
import com.ist.instocktracker.data.Mode
import com.ist.instocktracker.services.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LinkItemDetailsUiState {
    data object Loading : LinkItemDetailsUiState()
    data class Success(val linkItem: LinkItem) : LinkItemDetailsUiState()
    data class Error(val message: String) : LinkItemDetailsUiState()
}

class LinkItemDetailsViewModel(
    private val linkItemId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<LinkItemDetailsUiState>(LinkItemDetailsUiState.Loading)
    val uiState: StateFlow<LinkItemDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = LinkItemDetailsUiState.Loading
        viewModelScope.launch {
            try {
                val item = ServiceLocator.api.getLinkItem(linkItemId)
                _uiState.value = LinkItemDetailsUiState.Success(item)
            } catch (e: Exception) {
                _uiState.value = LinkItemDetailsUiState.Error(e.message ?: "Failed to load link item")
            }
        }
    }

    fun refresh() {
        load()
    }

    fun updateLink(newLink: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is LinkItemDetailsUiState.Success) {
                    val updatedItem = currentState.linkItem.copy(link = newLink)
                    ServiceLocator.api.updateLinkItem(linkItemId, updatedItem)
                    _uiState.value = LinkItemDetailsUiState.Success(updatedItem)
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = LinkItemDetailsUiState.Error(e.message ?: "Failed to update link")
                onError(e.message ?: "Failed to update link")
            }
        }
    }

    fun updateMode(newMode: Mode, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is LinkItemDetailsUiState.Success) {
                    val updatedItem = currentState.linkItem.copy(mode = newMode)
                    ServiceLocator.api.updateLinkItem(linkItemId, updatedItem)
                    _uiState.value = LinkItemDetailsUiState.Success(updatedItem)
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = LinkItemDetailsUiState.Error(e.message ?: "Failed to update mode")
                onError(e.message ?: "Failed to update mode")
            }
        }
    }
}
