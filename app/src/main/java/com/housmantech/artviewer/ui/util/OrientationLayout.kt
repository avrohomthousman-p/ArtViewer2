package com.housmantech.artviewer.ui.util

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration



/**
 * Composable to display different content depending on the screen orientation.
 */
@Composable
fun OrientationLayout(
    portrait: @Composable () -> Unit,
    landscape: @Composable () -> Unit
) {
    val config = LocalConfiguration.current

    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        landscape()
    }
    else {
        portrait()
    }
}
