package com.housmantech.artviewer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.housmantech.artviewer.data.repository.SettingsDefaults
import com.housmantech.artviewer.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {


    val isGuestMode = MutableStateFlow(true)
    val matureContentAllowed = MutableStateFlow(SettingsDefaults.MATURE_CONTENT_ALLOWED)
    val showArtTitle = MutableStateFlow(SettingsDefaults.SHOW_ART_TITLES)



    init {
        viewModelScope.launch(Dispatchers.IO) {
            isGuestMode.value = settingsRepo.isGuestMode()
            matureContentAllowed.value = settingsRepo.shouldShowMatureContent()
            showArtTitle.value = settingsRepo.shouldShowArtTitle()
        }
    }



    fun setMatureContent(allowed: Boolean) {
        if (isGuestMode.value){
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.setUserPrefersMatureContent(allowed)
            matureContentAllowed.value = allowed
        }
    }



    fun setShowArtTitle(showTitle: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.setShouldShowArtTitle(showTitle)
            showArtTitle.value = showTitle
        }
    }
}