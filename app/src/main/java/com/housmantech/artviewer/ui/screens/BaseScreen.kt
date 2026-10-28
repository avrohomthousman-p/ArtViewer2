package com.housmantech.artviewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.housmantech.artviewer.ui.themes.AppColors


/**
 * Wrapper around a screen that ensures edge to edge is handled correctly.
 */
@Composable
fun BaseScreen(
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = { StatusBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // EdgeToEdge padding
                .background(AppColors.White)
        ) {
            content()
        }
    }
}



/**
 * Empty box that fills the status bar with the appropriate color.
 */
@Composable
private fun StatusBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()

            // Sets the height of the box to be the same height as the status bar
            .windowInsetsTopHeight(WindowInsets.statusBars)

            // Ensures that the box doesn't extend too far in landscape orientation
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal
                )
            )

            .background(AppColors.StatusBarColor)
    ) {
        //Empty
    }
}
