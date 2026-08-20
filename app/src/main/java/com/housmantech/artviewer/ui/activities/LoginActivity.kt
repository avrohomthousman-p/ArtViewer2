package com.housmantech.artviewer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.housmantech.artviewer.ui.screens.LoginScreen
import com.housmantech.artviewer.ui.screens.LoginViewModel
import com.housmantech.artviewer.BuildConfig
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

        Log.e("App Start", "app version: ${BuildConfig.VERSION_NAME}")

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