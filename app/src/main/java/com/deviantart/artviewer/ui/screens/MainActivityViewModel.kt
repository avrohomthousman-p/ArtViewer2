package com.deviantart.artviewer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.data.repository.AuthRepository
import com.deviantart.artviewer.util.NavDestination
import com.deviantart.artviewer.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class Folder {} //TODO: Temporary class until the DB is set up



@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authRepo: AuthRepository
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
            //TODO: need actual implementation

            delay(5000)
            _uiState.value = UiState.Success(emptyList())
        }
    }


    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.logout()
            _navigation.emit(NavDestination.ToLoginActivity)
        }
    }
}
