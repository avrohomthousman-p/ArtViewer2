package com.deviantart.artviewer.data.repository

import android.net.Uri
import com.deviantart.artviewer.data.local.datastore.AuthenticationDataStore
import com.deviantart.artviewer.data.remote.LoginApi
import com.deviantart.artviewer.util.PkceUtil
import java.time.Instant
import java.time.LocalDate
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

        val authUrl = "https://www.deviantart.com/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=" + CLIENT_ID +
                "&redirect_uri=artviewer://oauth2redirect" +
                "&scope=browse" +
                "&code_challenge=${codeChallenge}" +
                "&code_challenge_method=S256";


        return authUrl.toUri()
    }


    suspend fun refreshAccessToken() {
        //TODO
    }


    suspend fun logout() {
        //TODO
    }
}