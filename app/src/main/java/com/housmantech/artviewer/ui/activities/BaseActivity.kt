package com.housmantech.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat



/**
 * Base activity for all other activities to extend
 */
open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = false
    }
}