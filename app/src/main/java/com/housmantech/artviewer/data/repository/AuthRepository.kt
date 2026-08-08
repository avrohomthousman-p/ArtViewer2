package com.housmantech.artviewer.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.housmantech.artviewer.data.local.datastore.AuthenticationDataStore
import com.housmantech.artviewer.data.remote.LoginApi
import com.housmantech.artviewer.data.util.PkceUtil
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.housmantech.artviewer.data.remote.GuestLoginRequest
import com.housmantech.artviewer.data.remote.TokenResponse
import com.housmantech.artviewer.data.util.ApiResponse
import com.housmantech.artviewer.data.util.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.time.ZoneOffset
import java.time.ZonedDateTime


/**
 * Gives access to all authentication related functionality like login
 * and access tokens.
 */
@Singleton
class AuthRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val loginApi: LoginApi,
    private val dataStore: AuthenticationDataStore,
    private val tokenManager: TokenManager
) {

    private val CLIENT_ID = "68491"



    /**
     * Checks if the user can be automatically logged in using the refresh token,
     * or if they need to manually log in.
     */
    suspend fun shouldRequireLogin() : Boolean {
        return !isRefreshPossible()
    }



    private suspend fun isRefreshPossible(): Boolean {
        val refreshToken = dataStore.loadRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            return false
        }

        val refreshTokenExpiration = dataStore.loadRefreshTokenExpiration()
            ?.let { Instant.parse(it) }

        val now = Instant.now()

        return refreshTokenExpiration != null && now.isBefore(refreshTokenExpiration)
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
    suspend fun exchangeAuthCodeForAccessToken(authCode: String) : ApiResponse<String> {
        val codeVerifier = dataStore.loadPkceCodeVerifier()
        if (codeVerifier.isNullOrEmpty()){
            return ApiResponse.Error("PKCE code verifier is missing")
        }


        val response = safeApiCall<TokenResponse> {
            loginApi.generateAccessToken(
                grantType = "authorization_code",
                clientId = CLIENT_ID,
                redirectUri = "artviewer://oauth2redirect",
                code = authCode,
                codeVerifier = codeVerifier
            )
        }


        when (response){
            is ApiResponse.Error -> {
                Log.e("Login Failure", response.message)
                return ApiResponse.Error(response.message)
            }
            is ApiResponse.Success -> {
                return saveTokenResponse(response.data)
            }
        }
    }



    /**
     * Gets an access token from a cloudflare worker so the user can access DeviantArt
     * media without needing to log in.
     *
     * Note: this method does not provide a refresh token.
     */
    suspend fun loginAsGuest(): ApiResponse<String> {
        val response = safeApiCall<TokenResponse>{
            val nonce = generateNonce()
            val attestationToken = getAttestationToken(nonce)
            val requestBody = GuestLoginRequest(attestationToken, nonce)
            loginApi.loginAsGuest(requestBody)
        }


        when (response){
            is ApiResponse.Error -> {
                Log.e("Guest Login Failure", response.message)
                return ApiResponse.Error(response.message)
            }
            is ApiResponse.Success -> {
                return saveTokenResponse(response.data)
            }
        }
    }



    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }



    /**
     * Error checking and save data from the response after exchanging the auth token for the
     * accessCode.
     */
    private suspend fun saveTokenResponse(tokenData: TokenResponse) : ApiResponse<String> {
        if (tokenData.status != "success"){
            Log.e("Login Failure", "received status ${tokenData.status}")
            return ApiResponse.Error("Login failure: " + (tokenData.errorDescription ?: ""))
        }
        if (tokenData.accessToken.isNullOrEmpty()) {
            Log.e("Login Failure", "No access token received")
            return ApiResponse.Error("Login failure: did not receive an access token from DeviantArt")
        }


        val newRefreshToken = tokenData.refreshToken
        if (!newRefreshToken.isNullOrEmpty()){
            saveNewRefreshToken(newRefreshToken)
        }


        tokenManager.saveAccessToken(tokenData.accessToken)
        dataStore.clearPkceCodeVerifier()

        return ApiResponse.Success<String>(tokenData.accessToken)
    }



    /**
     * Instead of a full login, use the existing refresh token to load a new access token.
     */
    suspend fun refreshAccessToken(): ApiResponse<String> {
        if (!isRefreshPossible()) {
            Log.e("Login Failure", "Refresh not possible. User must login")
            return ApiResponse.Error("Cannot refresh access token. Please login again.")
        }


        val response = safeApiCall<TokenResponse> {
            val refreshToken = dataStore.loadRefreshToken()!!
            loginApi.refreshAccessToken(
                grantType = "refresh_token",
                clientId = CLIENT_ID,
                refreshToken = refreshToken,
            )
        }



        when (response) {
            is ApiResponse.Error -> {
                onRefreshFailed()
                Log.e("Login Failure", response.message)
                return response
            }
            is ApiResponse.Success<TokenResponse> -> {
                val tokenData = response.data


                if (tokenData.status != "success") {
                    onRefreshFailed()
                    Log.e("Login failure", "received status ${tokenData.status}")
                    return ApiResponse.Error("Login failure: did not receive a success status from DeviantArt")
                }
                if (tokenData.accessToken.isNullOrEmpty()){
                    onRefreshFailed()
                    Log.e("Login failure", "No access token received")
                    return ApiResponse.Error("Login failure: did not receive an access token from DeviantArt")
                }



                tokenManager.saveAccessToken(tokenData.accessToken)
                tokenData.refreshToken?.let { saveNewRefreshToken(it) }
                return ApiResponse.Success(tokenData.accessToken)
            }
        }
    }



    /**
     * Clears the refresh token and its expiration date if we were not able to refresh it.
     */
    private suspend fun onRefreshFailed(){
        dataStore.clearRefreshToken()
        dataStore.clearRefreshTokenExpiration()
    }



    /**
     * Overwrites the existing refresh token with the new one, and updates the expiration date to
     * 3 months from now.
     *
     * You cannot use this function to clear the refresh token by setting it to null. If you want
     * that, you need to call the clearRefreshToken and clearRefreshTokenExpirationDate functions
     * on the data store.
     */
    private suspend fun saveNewRefreshToken(newRefreshToken: String) {
        if (newRefreshToken.isNotBlank()) {
            dataStore.saveRefreshToken(newRefreshToken)

            val expirationDate = ZonedDateTime.now(ZoneOffset.UTC)
                .plusMonths(3)
                .toInstant()
                .toString()

            dataStore.saveRefreshTokenExpiration(expirationDate)
        }
    }



    /**
     * Get a token from the Integrity API that cloudflare will use to ensure it's this
     * app asking for a DeviantArt access token. Only needed for guest login.
     */
    private suspend fun getAttestationToken(nonce: String): String {
        val integrityManager = IntegrityManagerFactory.create(context)

        val request = IntegrityTokenRequest.builder()
            .setNonce(nonce)
            .build()

        val response = integrityManager.requestIntegrityToken(request).await()

        return response.token()
    }



    /**
     * Log out of DeviantArt. This will clear your access token and refresh token
     * so you should navigate to the login screen after calling this.
     */
    suspend fun logout() {
        val refreshToken = dataStore.loadRefreshToken()
        if (!refreshToken.isNullOrEmpty()){
            safeApiCall<Unit> {
                loginApi.logout(inAppOnly = true, token = refreshToken)
            }
        }

        tokenManager.clearAccessToken()
        dataStore.clearRefreshToken()
        dataStore.clearRefreshTokenExpiration()
    }
}