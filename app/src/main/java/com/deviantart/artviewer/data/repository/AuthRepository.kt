package com.deviantart.artviewer.data.repository

import android.net.Uri
import android.util.Log
import com.deviantart.artviewer.data.local.datastore.AuthenticationDataStore
import com.deviantart.artviewer.data.remote.LoginApi
import com.deviantart.artviewer.util.PkceUtil
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import com.deviantart.artviewer.data.remote.TokenResponse
import com.deviantart.artviewer.util.ApiResponse
import java.time.ZoneOffset
import java.time.ZonedDateTime


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


    var accessToken: String? = null
        private set


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
    suspend fun exchangeAuthCodeForAccessToken(authCode: String) : ApiResponse<String> { //FIXME: Do I want to use a Respone object
        val codeVerifier = dataStore.loadPkceCodeVerifier()
        if (codeVerifier.isNullOrEmpty()){
            return ApiResponse.Error("PKCE code verifier is missing")
        }


        val response = loginApi.generateAccessToken(
            grantType = "authorization_code",
            clientId = CLIENT_ID,
            redirectUri = "artviewer://oauth2redirect",
            code = authCode,
            codeVerifier = codeVerifier
        )


        if (!response.isSuccessful){
            val error = response.errorBody()?.string() ?: "Could not authenticate"
            val raw = response.errorBody()?.string()
            Log.e("TOKEN_ERROR", raw ?: "no error body")
            Log.e("API failure", error)
            return ApiResponse.Error(error)
        }


        val tokenData = response.body() ?: return ApiResponse.Error("No response from DeviantArt")
        return saveTokenResponse(tokenData)
    }


    /**
     * Error checking and save data from the response after exchanging the auth token for the
     * accessCode.
     */
    private suspend fun saveTokenResponse(tokenData: TokenResponse) : ApiResponse<String> {
        if (tokenData.status != "success"){
            return ApiResponse.Error("Login failure: " + (tokenData.errorDescription ?: ""))
        }
        if (tokenData.accessToken.isNullOrEmpty()) {
            return ApiResponse.Error("Login failure: did not receive an access token from DeviantArt")
        }


        if (!tokenData.refreshToken.isNullOrEmpty()){
            dataStore.saveRefreshToken(tokenData.refreshToken)

            val expirationDate = ZonedDateTime.now(ZoneOffset.UTC)
                .plusMonths(3)
                .toInstant()
                .toString()

            dataStore.saveRefreshTokenExpiration(expirationDate)
        }


        this.accessToken = tokenData.accessToken
        dataStore.clearPkceCodeVerifier()

        return ApiResponse.Success<String>(tokenData.accessToken)
    }


    suspend fun refreshAccessToken() {
        //TODO
    }


    suspend fun logout() {
        //TODO
    }
}