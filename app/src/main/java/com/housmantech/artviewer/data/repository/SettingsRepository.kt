package com.housmantech.artviewer.data.repository

import com.housmantech.artviewer.data.local.datastore.AppStateDataStore
import javax.inject.Inject
import javax.inject.Singleton



/**
 * Gets settings related to content display, like maturity allowed/blocked
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val appState: AppStateDataStore,
    private val authRepo: AuthRepository
) {


    /**
     * Gets the users preferences with regard to allowing mature content.
     * This setting should be ignored when logged in as guest.
     */
    suspend fun userPrefersMatureContent(): Boolean {
        return appState.userPrefersMatureContent()
    }


    /**
     * Sets the users preferences with regard to allowing mature content.
     * This setting will be ignored when logged in as guest.
     */
    suspend fun setUserPrefersMatureContent(allowed: Boolean) {
        appState.setUserPrefersMatureContent(allowed)
    }


    /**
     * A convenient way to find out of the user is logged in as guest without
     * needing to call the auth repository directly.
     *
     * This function gets the data directly from the auth repository so it should
     * always be up to date.
     */
    suspend fun isGuestMode(): Boolean {
        return authRepo.isGuestMode()
    }


    /**
     * Decides if the user should be shown mature content or not.
     */
    suspend fun shouldShowMatureContent(): Boolean {
        if (isGuestMode()) {
            return false
        }

        return userPrefersMatureContent()
    }
}
