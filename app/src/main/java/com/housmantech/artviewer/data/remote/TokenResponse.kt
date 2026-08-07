package com.housmantech.artviewer.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


/**
 * Data class for fetching an access token or refreshing an access token
 * from the DeviantArt API.
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String? = null,

    @SerialName("refresh_token")
    val refreshToken: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("error_description")
    val errorDescription: String? = null
)
