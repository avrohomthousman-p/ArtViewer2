package com.deviantart.artviewer.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.R
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.repository.FolderRepository
import com.deviantart.artviewer.data.util.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
                _toastMessage.emit(R.string.saved_full_collection_toast_message)
            }
            else {
                _toastMessage.emit(R.string.saved_full_collection_failed_toast_message)
            }
        }
    }
}
