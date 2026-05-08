package com.deviantart.artviewer.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.data.repository.AuthRepository
import com.deviantart.artviewer.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class Folder {} //TODO: Temporary class until the DB is set up



@HiltViewModel
class MainActivityViewModel @Inject constructor(
    //TODO: inject dependencies here
) : ViewModel() {

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
}
