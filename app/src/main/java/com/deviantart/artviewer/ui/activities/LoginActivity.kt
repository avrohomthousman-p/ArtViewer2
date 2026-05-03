package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import com.deviantart.artviewer.ui.screens.LoginScreen
import com.deviantart.artviewer.ui.screens.LoginViewModel


/**
 * Launcher activity that asks the user to log into DeviantArt
 * or refreshes their login token instead (if possible).
 */
class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreen(LoginViewModel())
        }
    }
}