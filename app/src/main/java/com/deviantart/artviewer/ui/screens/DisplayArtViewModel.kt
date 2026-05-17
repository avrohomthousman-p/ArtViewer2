package com.deviantart.artviewer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.repository.ArtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DisplayArtViewModel @Inject constructor(
    private val artRepo: ArtRepository,
    private val db: FolderDao
) : ViewModel() {



    fun loadFolderContent(folderId: Int){
        viewModelScope.launch(Dispatchers.IO) {
            artRepo.fetchFolderContents(folderId)//TODO: save this to a state var
        }
    }
}