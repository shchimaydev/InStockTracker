package com.ist.instocktracker

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {
    private val _sharedUrl = MutableStateFlow<String?>(null)
    val sharedUrl = _sharedUrl.asStateFlow()

    private val _deepLink = MutableStateFlow<String?>(null)
    val deepLink = _deepLink.asStateFlow()

    fun setDeepLink(link: String?) {
        _deepLink.value = link
    }

    fun consumedDeepLink() {
        _deepLink.value = null
    }


    fun setSharedUrl(url: String?) {
        _sharedUrl.value = url
    }

    fun consumeSharedUrl() {
        _sharedUrl.value = null
    }
}