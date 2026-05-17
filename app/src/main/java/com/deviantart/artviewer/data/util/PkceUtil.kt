package com.deviantart.artviewer.data.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Class with PKCE tools for doing an oauth2.1 login.
 */
object PkceUtil {

    fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return base64UrlEncode(bytes)
    }


    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return base64UrlEncode(digest)
    }


    private fun base64UrlEncode(input: ByteArray): String {
        return Base64.encodeToString(input, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
    }
}