package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint



/**
 * Activity for viewing DeviantArt folders that the user searched for, and saving the ones you want.
 */
@AndroidEntryPoint
class FolderSearchResultsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)


        setContent {
            //TODO
        }
    }
}