package com.housmantech.artviewer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.housmantech.artviewer.R
import com.housmantech.artviewer.common.StorageLocation
import com.housmantech.artviewer.data.repository.FolderRepository
import com.housmantech.artviewer.data.util.ApiResponse
import com.housmantech.artviewer.ui.util.NavDestination
import com.housmantech.artviewer.data.repository.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class FolderSearchViewModel @Inject constructor(
    private val folderRepo: FolderRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _toastMessage = MutableSharedFlow<Int>()
    val toastMessage = _toastMessage


    private val _navigation = Channel<NavDestination>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()



    /**
     * Checks if the access token has expired and if it has, triggers
     * navigation back to the login screen.
     *
     * @return true if the access token has expired and a navigation is being triggered,
     * and false otherwise.
     */
    fun checkIfAccessTokenExpired(): Boolean {
        if(tokenManager.isTokenExpired()){
            viewModelScope.launch(Dispatchers.IO) {
                _navigation.send(NavDestination.ToLoginActivity)
            }
            return true
        }

        return false
    }



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
                _navigation.send(NavDestination.ToMainActivity)
            }
            else {
                _toastMessage.emit(R.string.folder_search_save_failed_toast)
            }
        }
    }
}
