package com.housmantech.artviewer.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Singleton


private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")


/**
 * Data store that manages the auth related data we need to save in persistent
 * storage, like refresh tokens.
 */
@Singleton
class AuthenticationDataStore(private val context: Context) {
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val REFESH_TOKEN_EXPIRATION_DATE_KEY = stringPreferencesKey("refresh_token_expiration_date")
    public val PKCE_CODE_VERIFIER_KEY = stringPreferencesKey("pkce_code_verifier")



    suspend fun saveRefreshToken(value: String) {
        context.authDataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = value
        }
    }


    suspend fun loadRefreshToken(): String? {
        val prefs = context.authDataStore.data.first()
        return prefs[REFRESH_TOKEN_KEY]
    }


    suspend fun clearRefreshToken() {
        context.authDataStore.edit { prefs ->
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }


    suspend fun saveRefreshTokenExpiration(value: String) {
        context.authDataStore.edit { prefs ->
            prefs[REFESH_TOKEN_EXPIRATION_DATE_KEY] = value
        }
    }


    suspend fun loadRefreshTokenExpiration(): String? {
        val prefs = context.authDataStore.data.first()
        return prefs[REFESH_TOKEN_EXPIRATION_DATE_KEY]
    }


    suspend fun clearRefreshTokenExpiration() {
        context.authDataStore.edit { prefs ->
            prefs.remove(REFESH_TOKEN_EXPIRATION_DATE_KEY)
        }
    }


    suspend fun savePkceCodeVerifier(value: String) {
        context.authDataStore.edit { prefs ->
            prefs[PKCE_CODE_VERIFIER_KEY] = value
        }
    }


    suspend fun loadPkceCodeVerifier(): String? {
        val prefs = context.authDataStore.data.first()
        return prefs[PKCE_CODE_VERIFIER_KEY]
    }


    suspend fun clearPkceCodeVerifier() {
        context.authDataStore.edit { prefs ->
            prefs.remove(PKCE_CODE_VERIFIER_KEY)
        }
    }
}