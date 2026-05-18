package com.deviantart.artviewer.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.remote.DeviantArtFolder
import com.deviantart.artviewer.data.repository.FolderRepository
import com.deviantart.artviewer.data.util.ApiResponse
import com.deviantart.artviewer.ui.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class FolderSearchResultsViewModel @Inject constructor(
    private val folderRepo: FolderRepository,
    private val db: FolderDao
) : ViewModel() {


    private val _uiState = MutableStateFlow<UiState<List<DeviantArtFolder>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DeviantArtFolder>>> = _uiState



    /**
     * Load DeviantArt folders from the specified user so they can be displayed on
     * the screen for the user to save.
     */
    fun loadFolders(ownerUsername: String, location: StorageLocation){
        viewModelScope.launch(Dispatchers.IO) {
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



    fun removeFolderFromList(index: Int){
        if (_uiState.value !is UiState.Success<List<DeviantArtFolder>>){
            throw IllegalStateException("Cannot remove folder from results if there are no results")
        }


        val currentList = (_uiState.value as UiState.Success<List<DeviantArtFolder>>).data.toMutableList()
        currentList.removeAt(index)

        _uiState.value = UiState.Success(
            data = currentList.toList()
        )
    }
}
