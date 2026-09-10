package com.housmantech.artviewer.ui.screens

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.housmantech.artviewer.data.local.room.Folder
import com.housmantech.artviewer.data.local.room.FolderDao
import com.housmantech.artviewer.data.remote.DeviantArtMediaItem
import com.housmantech.artviewer.data.repository.ArtRepository
import com.housmantech.artviewer.data.repository.TokenManager
import com.housmantech.artviewer.ui.util.BatchConfig
import com.housmantech.artviewer.ui.util.BatchTracker
import com.housmantech.artviewer.ui.util.BatchTrackerFactory
import com.housmantech.artviewer.ui.util.NavDestination
import com.housmantech.artviewer.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



@HiltViewModel
class DisplayArtViewModel @Inject constructor(
    private val db: FolderDao,
    private val artRepo: ArtRepository,
    private val tokenManager: TokenManager
) : ViewModel() {


    private val _uiState = MutableStateFlow<UiState<List<DeviantArtMediaItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DeviantArtMediaItem>>> = _uiState


    private val _navigation = Channel<NavDestination>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()


    private lateinit var folder: Folder
    private lateinit var batchTracker: BatchTracker
    private var isRunningBatch = false



    fun loadFolderContent(folderId: Int){
        //Make sure we don't reload on rotations.
        val state = _uiState.value
        if (state is UiState.Success && state.data.isNotEmpty()) {
            return
        }


        viewModelScope.launch(Dispatchers.IO) {
            if(tokenManager.isTokenExpired()){
                _navigation.send(NavDestination.ToLoginActivity)
                return@launch
            }


            try {
                val artData = mutableStateListOf<DeviantArtMediaItem>()

                folder = db.getFolder(folderId)
                batchTracker = BatchTrackerFactory.create(
                    folderSize = folder.totalImages,
                    shouldRandomize = folder.shouldRandomize,
                    mediaList = artData
                )


                isRunningBatch = true
                runBatchWithRetries()
                isRunningBatch = false


                if (artData.isEmpty()){
                    Log.e("Art Fetching Failure", "Got no data in view model")
                    _uiState.value = UiState.Error("Something went wrong. We could not load your art.")
                    return@launch
                }


                _uiState.value = UiState.Success(artData)
            }
            catch (e: Exception) {
                isRunningBatch = false
                Log.e("Art Fetching Failure", e.message, e)
                _uiState.value = UiState.Error("Something went wrong. We could not load your art.")
            }
        }
    }



    fun onScroll(page: Int, isForward: Boolean) {
        if (!isForward) return

        val mediaList = _uiState.value as UiState.Success<List<DeviantArtMediaItem>>

        val distanceFromEndOfList = mediaList.data.size - page
        val shouldRunBatch = distanceFromEndOfList <= batchTracker.getEndOfListThreshold()
        if (shouldRunBatch) {
            viewModelScope.launch(Dispatchers.IO) {
                if (isRunningBatch) return@launch
                isRunningBatch = true

                try {
                    runBatchWithRetries()
                } finally {
                    isRunningBatch = false
                }
            }
        }
    }




    /**
     * Runs a new batch to get more DeviantArt media if there is more to get.
     *
     * If the response is empty (because all the incoming data was invalid) it
     * runs another batch, and will keep running batches until we run out of
     * items in the folder or get at least one item.
     */
    private suspend fun runBatchWithRetries() {
        var lastBatchWasEmpty = true
        while(lastBatchWasEmpty && this.batchTracker.hasMoreData()) {
            val batch = this.batchTracker.planNextBatch()
            val response = artRepo.runBatch(this.folder, batch)
            lastBatchWasEmpty = response.isEmpty() //TODO: can later upgrade this to check if we got at least X items

            withContext(Dispatchers.Main) {
                batchTracker.saveResults(response)
            }
        }
    }
}
