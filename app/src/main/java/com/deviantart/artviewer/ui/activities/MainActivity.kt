package com.deviantart.artviewer.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.deviantart.artviewer.ui.screens.LoginScreen
import com.deviantart.artviewer.ui.screens.LoginViewModel
import com.deviantart.artviewer.ui.screens.MainActivityScreen
import com.deviantart.artviewer.ui.screens.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


/**
 * Main activity - where the saved folders are shown.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainActivityScreen(viewModel)
        }
    }
}