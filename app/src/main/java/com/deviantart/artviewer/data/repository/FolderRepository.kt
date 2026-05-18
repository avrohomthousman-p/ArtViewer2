package com.deviantart.artviewer.data.repository

import android.util.Log
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.remote.DeviantArtFolder
import com.deviantart.artviewer.data.remote.FolderApi
import com.deviantart.artviewer.data.util.ApiResponse
import com.deviantart.artviewer.data.util.toFolder
import javax.inject.Inject
import javax.inject.Singleton



/**
 * Repository for fetching folders from DeviantArt.
 */
@Singleton
class FolderRepository @Inject constructor(
    private val db: FolderDao,
    private val folderApi: FolderApi
) {

    suspend fun loadFolders(
        ownerUsername: String,
        location: StorageLocation,
        offset: Int = 0
    ): ApiResponse<List<Folder>> {

        val response = folderApi.fetchFolders(
            location = location.asUrlPath(),
            ownerUsername = ownerUsername,
            calculateSize = true,
            filterEmptyFolders = true,
            offset = offset
        )


        if (!response.isSuccessful || response.body()?.folderList.isNullOrEmpty()){
            val error = response.errorBody()?.string() ?: "Could not fetch folders"
            Log.e("API failure", error)
            return ApiResponse.Error(error)
        }


        val resultAsFolders = response.body()!!
            .folderList.map { deviantArtFolder ->
                deviantArtFolder.toFolder(ownerUsername, location)
            }


        return ApiResponse.Success(resultAsFolders)
    }
}