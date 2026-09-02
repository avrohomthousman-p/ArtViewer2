package com.housmantech.artviewer.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.housmantech.artviewer.data.local.room.Folder
import com.housmantech.artviewer.data.local.room.FolderDao
import com.housmantech.artviewer.data.repository.ArtRepository
import com.housmantech.artviewer.data.repository.TokenManager
import com.housmantech.artviewer.data.util.ArtQueryPlanner
import com.housmantech.artviewer.ui.util.LazyMediaItem
import com.housmantech.artviewer.ui.util.LinkedList
import com.housmantech.artviewer.ui.util.LinkedListGeneric
import com.housmantech.artviewer.ui.util.LinkedListV2
import com.housmantech.artviewer.ui.util.NavDestination
import com.housmantech.artviewer.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Strategy for fetching media from a folder based on its size and ordering.
 *
 * FULL_FOLDER_UPFRONT:
 *   - Used for small folders.
 *   - Fetch everything immediately because the number of required queries is low.
 *   - Ideal when upfront cost is small and random access is needed later (e.g., shuffling).
 *
 * BATCHES:
 *   - Used for large, ordered folders.
 *   - Fetch media in fixed-size chunks (e.g., 24 at a time) as the user scrolls.
 *   - Only practical when the folder is ordered, because batch queries rely on
 *     items being near each other in DeviantArt’s native ordering.
 *
 * ONE_AT_A_TIME:
 *   - Used for large, randomized folders.
 *   - Fetch individual items on demand because randomization destroys contiguity,
 *     making batch queries impossible.
 *
 * The goal is to minimize network calls while keeping scrolling responsive.
 */

enum class FetchStrategy {
    FULL_FOLDER_UPFRONT,
    BATCHES,
    ONE_AT_A_TIME
}

const val WINDOW_SIZE = 15


@HiltViewModel
class DisplayArtViewModel @Inject constructor(
    private val db: FolderDao,
    private val artRepo: ArtRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private lateinit var folder: Folder
    private lateinit var fetchStrategy: FetchStrategy


    private val _uiState = MutableStateFlow<UiState<LinkedList>>(UiState.Loading)
    val uiState: StateFlow<UiState<LinkedList>> = _uiState


    private val _currentMediaItem = MutableStateFlow<LazyMediaItem?>(null)
    val currentMediaItem = _currentMediaItem.asStateFlow()


    private val _navigation = Channel<NavDestination>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()



    /**
     * Loads initial data
     */
    fun loadFolderContent(folderId: Int){
        //Make sure we don't reload on rotations.
        val state = _uiState.value
        if (state is UiState.Success) {
            return
        }


        viewModelScope.launch(Dispatchers.IO) {
            if(tokenManager.isTokenExpired()){
                _navigation.send(NavDestination.ToLoginActivity)
                return@launch
            }


            try {
                folder = db.getFolder(folderId)

                if (folder.totalImages == 0) {
                    Log.e("Art Fetching Failure", "Folder is empty")
                    _uiState.value = UiState.Error("This folder is empty.")
                    return@launch
                }

                chooseFetchStrategy()

                val mediaList = LinkedListV2(folder.totalImages, folder.shouldRandomize)

                when(fetchStrategy){
                    FetchStrategy.FULL_FOLDER_UPFRONT -> {
                        val responseData = artRepo.fetchFullFolder(folder)
                        mediaList.populateRange(responseData)
                    }
                    FetchStrategy.BATCHES -> {
                        fetchStarterDataByBatch(mediaList)
                    }
                    FetchStrategy.ONE_AT_A_TIME -> {
                        fetchStarterDataOneAtATime(mediaList)
                    }
                }

                _uiState.value = UiState.Success(mediaList)
            }
            catch (e: Exception) {
                Log.e("Art Fetching Failure", e.message, e)
                _uiState.value = UiState.Error("Something went wrong. We could not load your art.")
            }
        }
    }


    /**
     * Sets the local fetchStrategy variable to the appropriate strategy depending on the folder being
     * displayed. This function should not be called before the local folder variable is set.
     */
    private fun chooseFetchStrategy(){
        fetchStrategy =
            if (folder.totalImages <= ArtQueryPlanner.MAX_ITEMS_SHOWN)
                FetchStrategy.FULL_FOLDER_UPFRONT
            else if (folder.shouldRandomize)
                FetchStrategy.ONE_AT_A_TIME
            else
                FetchStrategy.BATCHES
    }



    /**
     * Fetch media for populating the LinkedList on first load. Use this only for
     * FetchStrategy.BATCHES situations.
     */
    private suspend fun fetchStarterDataByBatch(mediaList: LinkedList) {
        var itemsNeeded = WINDOW_SIZE + 1

        while (itemsNeeded > 0){
            val nextIndexNeeded =
                mediaList.getNextPendingItems(1).firstOrNull() ?: return

            val responseData = artRepo.fetchNext24Items(folder, offset = nextIndexNeeded)
            val missingItems = mediaList.populateRange(responseData)
            itemsNeeded -= missingItems
        }
    }



    /**
     * Fetch media for populating the LinkedList on first load. Use this only for
     * FetchStrategy.ONE_AT_A_TIME situations.
     */
    private suspend fun fetchStarterDataOneAtATime(mediaList: LinkedList) {
        var itemsNeeded = WINDOW_SIZE + 1
        while (itemsNeeded > 0) {
            val indexesToLoad = mediaList.getNextPendingItems(itemsNeeded)
            val loadedMedia = artRepo.fetchStarterNonContiguousMedia(folder, indexesToLoad)
            val itemsMissing = mediaList.populateRange(loadedMedia)
            itemsNeeded -= itemsMissing
        }
    }



    fun onScroll(forward: Boolean) {
        //TODO:
    }
}
