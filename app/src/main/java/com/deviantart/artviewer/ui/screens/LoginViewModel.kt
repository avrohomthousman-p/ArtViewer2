package com.deviantart.artviewer.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
class LoginViewModel : ViewModel() {
    var loginState by mutableStateOf(LoginState.LoggedOut)
        private set


    fun performLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            loginState = LoginState.LoginInProgress

            try {
                delay(5000) // TODO: need real implementation

                loginState = LoginState.LoginSuccess
            }
            catch (e: Exception){
                Log.e("Login", e.message ?: "login failure")
                loginState = LoginState.LoginFailure
            }
        }
    }
}
