package com.housmantech.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.housmantech.artviewer.ui.screens.SettingsScreen
import com.housmantech.artviewer.ui.screens.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class SettingsActivity : BaseActivity() {
    private val viewModel: SettingsViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingsScreen(viewModel)
        }
    }
}