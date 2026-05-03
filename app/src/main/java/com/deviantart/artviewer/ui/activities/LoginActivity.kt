package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.deviantart.artviewer.ui.screens.LoginScreen
import com.deviantart.artviewer.ui.screens.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * Launcher activity that asks the user to log into DeviantArt
 * or refreshes their login token instead (if possible).
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreen(viewModel)
        }
    }
}