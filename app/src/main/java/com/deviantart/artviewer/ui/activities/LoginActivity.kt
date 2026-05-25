package com.deviantart.artviewer.ui.activities

import android.content.Intent
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

        val intent = intent
        val authCode =
            if (intent?.action == Intent.ACTION_VIEW) {
                // If we came here from the DeviantArt OAuth 2.1 login page
                intent.data?.getQueryParameter("code")
            } else {
                // Normal app launch
                null
            }

        viewModel.initializeDB()
        viewModel.setInitialState(authCode)

        setContent {
            LoginScreen(viewModel)
        }
    }
}