package com.deviantart.artviewer.data.repository

import android.net.Uri
import com.deviantart.artviewer.data.local.datastore.AuthenticationDataStore
import com.deviantart.artviewer.data.remote.LoginApi
import com.deviantart.artviewer.util.PkceUtil
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri


/**
 * Gives access to all authentication related functionality like login
 * and access tokens.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val loginApi: LoginApi,
    private val dataStore: AuthenticationDataStore
) {

    private val CLIENT_ID = "48967"


    /**
     * Checks if the user can be automatically logged in using the refresh token,
     * or if they need to manually log in.
     */
    suspend fun shouldRequireLogin() : Boolean {
        val refreshTokenExpiration = dataStore.loadRefreshTokenExpiration()
            ?.let { Instant.parse(it) }

        val now = Instant.now()

        return refreshTokenExpiration == null || now.isAfter(refreshTokenExpiration)
    }


    /**
     * Builds the URL for the DeviantArt OAuth2.1 login page.
     */
    suspend fun buildAuthorizationUrl(): Uri {
        val codeVerifier = PkceUtil.generateCodeVerifier()
        val codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier)

        dataStore.savePkceCodeVerifier(codeVerifier)

        val baseUrl = "https://www.deviantart.com/oauth2/authorize"
        return baseUrl.toUri()
            .buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", "artviewer://oauth2redirect")
            .appendQueryParameter("scope", "browse")
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
    }


    /**
     * Completes the login by exchanging the authentication token provided by DeviantArt
     * for an access token that can be used to fetch DeviantArt media.
     */
    suspend fun exchangeAuthCodeForAccessToken(authCode: String){
        //TODO
    }


    suspend fun refreshAccessToken() {
        //TODO
    }


    suspend fun logout() {
        //TODO
    }
}