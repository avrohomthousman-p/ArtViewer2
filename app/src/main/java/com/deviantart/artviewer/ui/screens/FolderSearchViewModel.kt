package com.deviantart.artviewer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.R
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.repository.FolderRepository
import com.deviantart.artviewer.data.util.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class FolderSearchViewModel @Inject constructor(
    private val folderRepo: FolderRepository
) : ViewModel() {

    private val _toastMessage = MutableSharedFlow<Int>()
    val toastMessage = _toastMessage


    /**
     * Saves a full collection or gallery to a single local folder
     */
    fun saveFullCollectionAsFolder(
        ownerUsername: String,
        location: StorageLocation,
        shouldRandomize: Boolean
    ){
        viewModelScope.launch(Dispatchers.IO){
            val response = folderRepo.saveFullCollectionAsFolder(
                ownerUsername = ownerUsername,
                location = location,
                shouldRandomize = shouldRandomize
            )


            if(response is ApiResponse.Success){
                _toastMessage.emit(R.string.folder_search_save_success_toast)
            }
            else {
                _toastMessage.emit(R.string.folder_search_save_failed_toast)
            }
        }
    }
}
