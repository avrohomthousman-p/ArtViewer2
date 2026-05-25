package com.deviantart.artviewer.data.repository

import android.app.Application
import com.deviantart.artviewer.R
import com.deviantart.artviewer.data.local.datastore.AppStateDataStore
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.remote.SampleDBFolder
import com.deviantart.artviewer.data.util.toFolder
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton



/**
 * Repository for populating the DB with sample data on first app launch.
 */
@Singleton
class DbSetupRepository @Inject constructor(
    private val app: Application,
    private val dataStore: AppStateDataStore,
    private val db: FolderDao
) {

    suspend fun onFirstAppLaunch(){
        if (!dataStore.isFirstLaunch()){
            return
        }


        val jsonString = app.resources
            .openRawResource(R.raw.sample_db)
            .bufferedReader()
            .use { it.readText() }

        val folders = Json.decodeFromString<List<SampleDBFolder>>(jsonString)
            .map { it.toFolder() }

        db.bulkCreate(folders)

        dataStore.markFirstLaunchCompleted()
    }
}