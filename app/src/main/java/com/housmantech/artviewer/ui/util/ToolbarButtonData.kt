package com.housmantech.artviewer.ui.util


/**
 * Class that stores all the data needed to create a single clickable button
 * in the app toolbar.
 */
data class ToolbarButtonData(
    val icon: Int,
    val contentDescription: String,
    val onClick: () -> Unit
)