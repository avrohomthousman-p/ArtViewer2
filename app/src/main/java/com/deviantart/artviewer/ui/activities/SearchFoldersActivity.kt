package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import com.deviantart.artviewer.ui.screens.SearchFoldersScreen
import dagger.hilt.android.AndroidEntryPoint



/**
 * Activity for entering search criteria to find new DeviantArt folders.
 */
@AndroidEntryPoint
class SearchFoldersActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        //TODO: build a viewModel

        setContent {
            SearchFoldersScreen()
        }
    }
}