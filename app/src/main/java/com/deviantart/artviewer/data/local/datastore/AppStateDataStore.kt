package com.deviantart.artviewer.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Singleton



private val Context.appStateDataStore by preferencesDataStore(name = "app_state_prefs")


@Singleton
class AppStateDataStore(private val context: Context) {
    private val FIRST_LAUNCH_KEY = booleanPreferencesKey("not_first_launch")


    suspend fun isFirstLaunch(): Boolean {
        val prefs = context.appStateDataStore.data.first()
        return prefs[FIRST_LAUNCH_KEY] ?: true
    }



    suspend fun markFirstLaunchCompleted() {
        context.appStateDataStore.edit { prefs ->
            prefs[FIRST_LAUNCH_KEY] = false
        }
    }
}