package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.deviantart.artviewer.ui.screens.DisplayArtScreen
import com.deviantart.artviewer.ui.themes.AppColors


/**
 * Activity where the user is shown the media contents of a specific folder.
 */
class DisplayArtActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = AppColors.StatusBarColor.toArgb(),
                darkScrim = AppColors.StatusBarColor.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = AppColors.NavBarColor.toArgb(),
                darkScrim = AppColors.NavBarColor.toArgb()
            )
        )

        setContent {
            DisplayArtScreen()
        }
    }
}