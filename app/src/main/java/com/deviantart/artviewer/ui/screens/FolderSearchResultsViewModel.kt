package com.deviantart.artviewer.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.repository.FolderRepository
import com.deviantart.artviewer.data.repository.TokenManager
import com.deviantart.artviewer.data.util.ApiResponse
import com.deviantart.artviewer.ui.util.NavDestination
import com.deviantart.artviewer.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class FolderSearchResultsViewModel @Inject constructor(
    private val folderRepo: FolderRepository,
    private val db: FolderDao,
    private val tokenManager: TokenManager
) : ViewModel() {


    private val _uiState = MutableStateFlow<UiState<List<Folder>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Folder>>> = _uiState


    private val _navigation = Channel<NavDestination>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()



    /**
     * Load DeviantArt folders from the specified user so they can be displayed on
     * the screen for the user to save.
     */
    fun loadDeviantArtFolders(ownerUsername: String, location: StorageLocation){
        viewModelScope.launch(Dispatchers.IO) {
            if(tokenManager.isTokenExpired()){
                _navigation.send(NavDestination.ToLoginActivity)
                return@launch
            }


            val response = folderRepo.loadFolders(ownerUsername, location)

            if (response is ApiResponse.Error){
                Log.e("Folder search", response.message)
                _uiState.value = UiState.Error(response.message)
            }
            else if(response is ApiResponse.Success) {
                _uiState.value = UiState.Success(response.data)
            }
        }
    }



    fun removeFolderFromDisplayList(index: Int){
        val state = _uiState.value
        require(state is UiState.Success<List<Folder>>) {
            "Cannot remove folder from results if there are no results"
        }


        val currentList = state.data.toMutableList()
        currentList.removeAt(index)

        _uiState.value = UiState.Success(
            data = currentList.toList()
        )
    }



    fun saveFolderToDB(folder: Folder, index: Int){
        val state = _uiState.value
        require(state is UiState.Success<List<Folder>>) {
            "Cannot remove folder from results if there are no results"
        }


        viewModelScope.launch(Dispatchers.IO) {
            db.updateOrCreateFolder(folder)

            removeFolderFromDisplayList(index)
        }
    }
}
