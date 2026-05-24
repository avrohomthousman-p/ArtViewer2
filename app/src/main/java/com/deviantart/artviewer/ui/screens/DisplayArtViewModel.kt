package com.deviantart.artviewer.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.data.remote.DeviantArtMediaItem
import com.deviantart.artviewer.data.repository.ArtRepository
import com.deviantart.artviewer.data.repository.TokenManager
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
class DisplayArtViewModel @Inject constructor(
    private val artRepo: ArtRepository,
    private val tokenManager: TokenManager
) : ViewModel() {


    private val _uiState = MutableStateFlow<UiState<List<DeviantArtMediaItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DeviantArtMediaItem>>> = _uiState


    private val _navigation = Channel<NavDestination>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()



    fun loadFolderContent(folderId: Int){
        viewModelScope.launch(Dispatchers.IO) {
            if(tokenManager.isTokenExpired()){
                _navigation.send(NavDestination.ToLoginActivity)
                return@launch
            }


            try {
                val artData = artRepo.fetchFolderContents(folderId)

                if (artData.isEmpty()){
                    Log.e("Art Fetching Failure", "Got no data in view model")
                    _uiState.value = UiState.Error("Something went wrong. We could not load your art.")
                    return@launch
                }


                _uiState.value = UiState.Success(artData)
            }
            catch (e: Exception) {
                Log.e("Art Fetching Failure", e.message, e)
                _uiState.value = UiState.Error("Something went wrong. We could not load your art.")
            }
        }
    }
}
