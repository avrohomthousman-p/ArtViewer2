package com.housmantech.artviewer.data.remote

import kotlinx.serialization.Serializable


/**
 * Stores the POST body for a guest login request
 */
@Serializable
data class GuestLoginRequest(
    val attestation: String,
    val nonce: String
)
