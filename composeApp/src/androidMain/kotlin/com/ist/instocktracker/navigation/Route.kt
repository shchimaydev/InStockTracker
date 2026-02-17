package com.ist.instocktracker.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Auth : Route

    @Serializable
    data class AddFromShare(val url: String) : Route

    @Serializable
    data object Main : Route

    @Serializable
    data object MainList : Route


    @Serializable
    data class LinkItemDetails(val linkItemId: String) : Route

    @Serializable
    data class EditLabel(val linkItemId: String) : Route

    @Serializable
    data class EditLink(val linkItemId: String) : Route

    @Serializable
    data class EditMode(val linkItemId: String) : Route

    @Serializable
    data class EditStartAt(val linkItemId: String) : Route

    @Serializable
    data class EditInterval(val linkItemId: String) : Route

    @Serializable
    data class EditStatus(val linkItemId: String) : Route

    @Serializable
    data class EditImage(val linkItemId: String) : Route

    @Serializable
    data class EditInstructions(val linkItemId: String) : Route

    @Serializable
    data object Paywall : Route

}



