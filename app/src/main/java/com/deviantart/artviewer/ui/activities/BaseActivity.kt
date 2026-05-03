package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.deviantart.artviewer.ui.themes.AppColors

/**
 * Base activity for all other activities to extend
 */
open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setEdgeToEdge()
    }


    /**
     * Sets the colors of the system navigation bar and status bar.
     */
    private fun setEdgeToEdge(){
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
    }
}