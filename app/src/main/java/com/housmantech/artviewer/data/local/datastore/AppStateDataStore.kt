package com.housmantech.artviewer.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.housmantech.artviewer.data.repository.SettingsDefaults
import kotlinx.coroutines.flow.first
import javax.inject.Singleton



private val Context.appStateDataStore by preferencesDataStore(name = "app_state_prefs")


@Singleton
class AppStateDataStore(private val context: Context) {
    private val FIRST_LAUNCH_KEY = booleanPreferencesKey("not_first_launch")
    private val MATURE_CONTENT_PERFERRED_KEY = booleanPreferencesKey("mature_content_allowed")
    private val SHOW_ART_TITLE_KEY = booleanPreferencesKey("show_art_title")



    suspend fun isFirstLaunch(): Boolean {
        val prefs = context.appStateDataStore.data.first()
        return prefs[FIRST_LAUNCH_KEY] ?: true
    }



    suspend fun markFirstLaunchCompleted() {
        context.appStateDataStore.edit { prefs ->
            prefs[FIRST_LAUNCH_KEY] = false
        }
    }



    suspend fun setUserPrefersMatureContent(value: Boolean) {
        context.appStateDataStore.edit { prefs ->
            prefs[MATURE_CONTENT_PERFERRED_KEY] = value
        }
    }



    suspend fun userPrefersMatureContent(): Boolean {
        val prefs = context.appStateDataStore.data.first()
        return prefs[MATURE_CONTENT_PERFERRED_KEY] ?: SettingsDefaults.MATURE_CONTENT_ALLOWED
    }



    suspend fun setUserPrefsShowArtTitle(value: Boolean) {
        context.appStateDataStore.edit { prefs ->
            prefs[SHOW_ART_TITLE_KEY] = value
        }
    }



    suspend fun userPrefersShowingArtTitle(): Boolean {
        val prefs = context.appStateDataStore.data.first()
        return prefs[SHOW_ART_TITLE_KEY] ?: SettingsDefaults.SHOW_ART_TITLES
    }
}