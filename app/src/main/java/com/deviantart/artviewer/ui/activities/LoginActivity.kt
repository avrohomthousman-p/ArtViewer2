package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.deviantart.artviewer.ui.screens.LoginScreen
import com.deviantart.artviewer.ui.themes.AppColors


/**
 * Launcher activity that asks the user to log into DeviantArt
 * or refreshes their login token instead (if possible).
 */
class LoginActivity : ComponentActivity() {
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
            LoginScreen()
        }
    }
}