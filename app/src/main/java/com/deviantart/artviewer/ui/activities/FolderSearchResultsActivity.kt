package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.ui.screens.FolderSearchResultsScreen
import com.deviantart.artviewer.ui.screens.FolderSearchResultsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



/**
 * Activity for viewing DeviantArt folders that the user searched for, and saving the ones you want.
 */
@AndroidEntryPoint
class FolderSearchResultsActivity : BaseActivity() {
    companion object {
        const val USERNAME_KEY = "username"
        const val LOCATION_KEY = "folderName"
    }


    private val viewModel: FolderSearchResultsViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)


        val ownerUsername = intent?.getStringExtra(USERNAME_KEY)
        val location = intent?.getStringExtra(LOCATION_KEY)

        if (ownerUsername.isNullOrEmpty() || location.isNullOrEmpty()){
            if (ownerUsername.isNullOrEmpty()){
                Log.e("Navigation", "No username received by FolderSearchResultsActivity")
            }
            if (location.isNullOrEmpty()){
                Log.e("Navigation", "No location received by FolderSearchResultsActivity")
            }

            handleMissingIntentData()
            return
        }


        lateinit var locationAsEnum: StorageLocation
        try {
            locationAsEnum = StorageLocation.valueOf(location.uppercase())
        }
        catch (e: IllegalArgumentException){
            Log.e("Navigation", e.message, e)
            handleMissingIntentData()
            return
        }



        setContent {
            FolderSearchResultsScreen(
                viewModel = viewModel,
                ownerUsername = ownerUsername,
                location = locationAsEnum
            )
        }
    }



    private fun handleMissingIntentData(){
        Toast.makeText(
            this,
            "Something went wrong. We could not load any folders.",
            Toast.LENGTH_LONG
        ).show()

        lifecycleScope.launch {
            delay(2000)
            finish()
        }
    }
}
