package com.deviantart.artviewer.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Enum for tracking the state of the login screen
 */
enum class LoginState {
    LoggedOut,
    LoginInProgress,
    LoginSuccess,
    LoginFailure
}


/**
 * View model for the login screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    var loginState by mutableStateOf(LoginState.LoggedOut)
        private set


    fun performLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            loginState = LoginState.LoginInProgress

            try {
                delay(3000) // TODO: need real implementation

                loginState = LoginState.LoginSuccess
            }
            catch (e: Exception){
                Log.e("Login", e.message ?: "login failure")
                loginState = LoginState.LoginFailure
            }
        }
    }
}
