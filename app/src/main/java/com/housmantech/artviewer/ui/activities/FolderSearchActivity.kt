package com.housmantech.artviewer.ui.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.housmantech.artviewer.ui.screens.FolderSearchScreen
import com.housmantech.artviewer.ui.screens.FolderSearchViewModel
import dagger.hilt.android.AndroidEntryPoint



/**
 * Activity for entering search criteria to find new DeviantArt folders.
 */
@AndroidEntryPoint
class FolderSearchActivity : BaseActivity() {
    private val viewModel: FolderSearchViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            FolderSearchScreen(this.viewModel)
        }
    }
}