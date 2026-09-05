package com.housmantech.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import com.housmantech.artviewer.ui.screens.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class SettingsActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingsScreen()
        }
    }
}