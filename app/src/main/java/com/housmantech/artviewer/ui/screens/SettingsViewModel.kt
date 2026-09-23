package com.housmantech.artviewer.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val matureContentAllowed = MutableStateFlow(false)



    init {
        viewModelScope.launch(Dispatchers.IO) {
            isGuestMode.value = settingsRepo.isGuestMode()
            matureContentAllowed.value = settingsRepo.shouldShowMatureContent()
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
}