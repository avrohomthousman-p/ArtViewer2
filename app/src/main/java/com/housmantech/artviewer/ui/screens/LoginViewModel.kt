package com.housmantech.artviewer.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.housmantech.artviewer.data.repository.AuthRepository
import com.housmantech.artviewer.data.repository.DbSetupRepository
import com.housmantech.artviewer.data.util.ApiResponse
import com.housmantech.artviewer.ui.util.MiscUtils
import com.housmantech.artviewer.ui.util.NavDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val authRepo: AuthRepository,
    private val dbSetupRepo: DbSetupRepository
) : ViewModel() {


    private lateinit var initializeDB: Job


    private val _navigation = Channel<NavDestination>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()


    var loginState by mutableStateOf(LoginState.LoggedOut)
        private set



    fun initializeDB(){
        this.initializeDB = viewModelScope.launch(Dispatchers.IO) {
            dbSetupRepo.onFirstAppLaunch()
        }
    }


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
            Log.e("LoginError", response.message)
            loginState = LoginState.LoginFailure
            delay(2500)
            loginState = LoginState.LoggedOut
            return
        }
        else {
            loginState = LoginState.LoginSuccess
            MiscUtils.runWithMinimumDuration(2500) {
                this.initializeDB.join()
            }
            _navigation.send(NavDestination.ToMainActivity)
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
                MiscUtils.runWithMinimumDuration(2500) {
                    this.initializeDB.join()
                }
                _navigation.send(NavDestination.ToMainActivity)
            }
        }
    }



    /**
     * Initiates a login by triggering navigation to DeviantArt's oauth2 webpage.
     */
    fun triggerLoginStart() {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = authRepo.buildAuthorizationUrl()
            _navigation.send(NavDestination.ToWebLogin(uri))
        }
    }



    /**
     * Initiate a login as guest
     */
    fun triggerGuestLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            loginState = LoginState.LoginInProgress

            val response = MiscUtils.runWithMinimumDuration(2500){
                authRepo.loginAsGuest()
            }


            when(response){
                is ApiResponse.Error -> {
                    Log.e("Guest Login Failure", "Could not login as guest")
                    loginState = LoginState.LoginFailure
                    delay(2500)
                    loginState = LoginState.LoggedOut
                }
                is ApiResponse.Success<*> -> {
                    loginState = LoginState.LoginSuccess
                    MiscUtils.runWithMinimumDuration(2500) {
                        initializeDB.join()
                    }
                    _navigation.send(NavDestination.ToMainActivity)
                }
            }
        }
    }
}
