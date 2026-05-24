package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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

        viewModel.loadFolders()

        setContent {
            MainActivityScreen(viewModel)
        }
    }



    var firstResume = true

    /**
     * <!-- @inheritDoc -->
     * The first time you resume this activity, the folders are already loaded by the
     * onCreate function. On subsequent visits we need to refresh the folders in case
     * you added any new ones in a different activity.
     */
    override fun onResume() {
        super.onResume()

        if (!firstResume){
            viewModel.loadFolders()
        }

        firstResume = false
    }
}