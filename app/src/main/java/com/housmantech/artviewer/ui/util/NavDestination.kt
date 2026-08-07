package com.housmantech.artviewer.ui.util

import android.net.Uri

/**
 * Enum that represents all the possible places that can be navigated to.
 * Used to emit navigation events from within a viewModel.
 */
sealed class NavDestination {
    data object ToLoginActivity : NavDestination()
    data object ToRedirectActivity : NavDestination()
    data object ToMainActivity : NavDestination()
    data class ToWebLogin(val url: Uri) : NavDestination()
    data object ToFolderSearch : NavDestination()
}