package com.deviantart.artviewer.data.remote

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


/**
 * Class with methods for running API calls related to DeviantArt authentication.
 */
interface LoginApi {


    /**
     * Exchange the authorization code for an access token.
     */
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun generateAccessToken(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String
    ): Response<TokenResponse>


    //TODO: create logout functions
}