package com.deviantart.artviewer.data.repository

import com.deviantart.artviewer.data.local.datastore.AuthenticationDataStore
import com.deviantart.artviewer.data.remote.LoginApi
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Gives access to all authentication related functionality like login
 * and access tokens.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: LoginApi,
    private val dataStore: AuthenticationDataStore
) {

    /**
     * Checks if the user can be automatically logged in using the refresh token,
     * or if they need to manually log in.
     */
    suspend fun shouldRequireLogin() : Boolean {
        //TODO
        return true
    }


    suspend fun performLogin(){
        //TODO
    }


    suspend fun refreshAccessToken() {
        //TODO
    }


    suspend fun logout() {
        //TODO
    }
}