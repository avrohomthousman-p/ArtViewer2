package com.deviantart.artviewer.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.repository.AuthRepository
import com.deviantart.artviewer.util.NavDestination
import com.deviantart.artviewer.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val db: FolderDao
) : ViewModel() {

    private val _navigation = MutableSharedFlow<NavDestination>()
    val navigation = _navigation.asSharedFlow()

    private val _uiState = MutableStateFlow<UiState<List<Folder>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Folder>>> = _uiState


    /**
     * Loads all saved folders from the DB so they can be displayed.
     */
    fun loadFolders(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val folders = db.getAllFolders()
                _uiState.value = UiState.Success(folders)
            } catch (e: Exception) {
                Log.e("db", e.message ?: "Room DB failure")
                _uiState.value = UiState.Error(e.message, e)
            }
        }
    }



    fun updateFolder(folder: Folder, index: Int){
        val currentState = _uiState.value
        if (currentState !is UiState.Success){
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.updateOrCreateFolder(folder)

                val folderList = currentState.data.toMutableList()
                folderList[index] = folder

                withContext(Dispatchers.Main){
                    _uiState.value = UiState.Success(folderList)
                }
            }
            catch (e: Exception){
                Log.e("db", e.message ?: "Room DB failure")
                //TODO: make toast
            }
        }
    }



    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.logout()
            _navigation.emit(NavDestination.ToLoginActivity)
        }
    }
}
