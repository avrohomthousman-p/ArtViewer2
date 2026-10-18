package com.housmantech.artviewer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.housmantech.artviewer.ui.themes.AppColors



/**
 * Wrapper around a screen that ensures edge to edge is handled correctly.
 */
@Composable
fun BaseScreen(
    screenBackground: Color = AppColors.White,
    statusBarColor: Color = AppColors.StatusBarColor,
    navBarColor: Color = AppColors.NavBarColor,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(statusBarColor)
        )



        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(screenBackground)
        ) {
            content()
        }



        Box(
            Modifier
                .fillMaxWidth()
                .height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .background(navBarColor)
        )
    }
}
