package com.housmantech.artviewer.data.repository

import java.time.Instant
import javax.inject.Singleton


/**
 * Class for managing the storage of the DeviantArt access token.
 */
@Singleton
class TokenManager {
    private var _accessToken: String? = null
    private var _expiresAt: Instant? = null


    /**
     * Gets the access token if the user is logged in, and throws an exception otherwise.
     * @throws IllegalStateException if the access token is null (user not logged in).
     */
    fun getAccessToken(): String {
        return this._accessToken ?: throw IllegalStateException("User not logged in")
    }


    fun isTokenExpired(): Boolean {
        if (this._accessToken == null || this._expiresAt == null){
            return true
        }


        return !(Instant.now().isBefore(this._expiresAt))
    }


    fun saveAccessToken(accessToken: String){
        this._accessToken = accessToken
        this._expiresAt = Instant.now().plusSeconds(3600)
    }


    fun clearAccessToken() {
        this._accessToken = null
        this._expiresAt = null
    }
}