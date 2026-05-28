package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.deviantart.artviewer.ui.screens.DisplayArtScreen
import com.deviantart.artviewer.ui.screens.DisplayArtViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Activity where the user is shown the media contents of a specific folder.
 */
@AndroidEntryPoint
class DisplayArtActivity : BaseActivity() {
    companion object {
        const val FOLDER_ID_KEY = "folderID"
        const val FOLDER_NAME_KEY = "folderName"
    }


    private val viewModel: DisplayArtViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val folderId = intent?.getIntExtra(FOLDER_ID_KEY, -1)
        if (folderId == null || folderId == -1){
            this.handleFolderNotFound()
            return
        }
        val folderName = intent?.getStringExtra(FOLDER_NAME_KEY) ?: "Art Display"



        //TODO: this should not run on a rotation
        viewModel.loadFolderContent(folderId)


        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            DisplayArtScreen(viewModel, folderName)
        }
    }



    /**
     * Shows an error toast and goes back to the previous activity.
     */
    private fun handleFolderNotFound(){
        Toast.makeText(
            this,
            "Something went wrong. We could not load your folder.",
            Toast.LENGTH_LONG
        ).show()

        lifecycleScope.launch {
            delay(2000)
            finish()
        }
    }
}
