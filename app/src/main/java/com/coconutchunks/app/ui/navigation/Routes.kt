package com.coconutchunks.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val LIBRARY = "library"
    const val REVIEW = "review"
    const val SETTINGS = "settings"

    const val EDIT_ARGUMENT = "chunkId"
    const val EDIT = "edit/{$EDIT_ARGUMENT}"

    fun edit(chunkId: Long): String = "edit/$chunkId"
}
