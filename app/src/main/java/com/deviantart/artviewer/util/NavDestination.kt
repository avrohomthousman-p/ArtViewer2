package com.deviantart.artviewer.util

/**
 * Enum that represents all the possible places that can be navigated to.
 * Used to emit navigation events from within a viewModel.
 */
sealed class NavDestination {
    data object ToLoginActivity : NavDestination()
    data object ToRedirectActivity : NavDestination()
    data object ToMainActivity : NavDestination()
    data object ToWebLogin : NavDestination()
    //TODO: the rest of the activities
}