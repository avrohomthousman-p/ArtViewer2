package com.deviantart.artviewer.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviantart.artviewer.data.repository.AuthRepository
import com.deviantart.artviewer.data.util.ApiResponse
import com.deviantart.artviewer.ui.util.MiscUtils
import com.deviantart.artviewer.ui.util.NavDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _navigation = MutableSharedFlow<NavDestination>()
    val navigation = _navigation.asSharedFlow()


    var loginState by mutableStateOf(LoginState.LoggedOut)
        private set


    fun setInitialState(authCode: String? = null){
        viewModelScope.launch(Dispatchers.IO){
            if(!authCode.isNullOrEmpty()){
                completeLogin(authCode)
            }
            else if (authRepo.shouldRequireLogin()) {
                loginState = LoginState.LoggedOut //Shows login button
            }
            else {
                performRefresh()
            }
        }
    }


    /**
     * Completes the login by having the auth repository exchange the authCode
     * for an access token.
     */
    private suspend fun completeLogin(authCode: String){
        loginState = LoginState.LoginInProgress

        val response = MiscUtils.runWithMinimumDuration(2500) {
            authRepo.exchangeAuthCodeForAccessToken(authCode)
        }

        if (response is ApiResponse.Error) {
            loginState = LoginState.LoginFailure
            Log.e("LoginError", response.message)
            return
        }
        else {
            loginState = LoginState.LoginSuccess
            delay(2500)
            _navigation.emit(NavDestination.ToMainActivity)
        }
    }



    /**
     * Refreshes the access token and navigates to the main activity.
     */
    private suspend fun performRefresh(){
        loginState = LoginState.LoginInProgress


        val response =
            MiscUtils.runWithMinimumDuration {
                authRepo.refreshAccessToken()
            }


        when(response) {
            is ApiResponse.Error -> {
                Log.e("LoginError", response.message)
                loginState = LoginState.LoginFailure
                delay(2500)
                loginState = LoginState.LoggedOut
                return
            }
            is ApiResponse.Success<*> -> {
                loginState = LoginState.LoginSuccess
                delay(2500)
                _navigation.emit(NavDestination.ToMainActivity)
            }
        }
    }



    /**
     * Initiates a login by triggering navigation to DeviantArt's oauth2 webpage.
     */
    fun triggerLoginStart() {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = authRepo.buildAuthorizationUrl()
            _navigation.emit(NavDestination.ToWebLogin(uri))
        }
    }
}
