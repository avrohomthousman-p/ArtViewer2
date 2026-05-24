package com.deviantart.artviewer.data.repository

import android.util.Log
import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.local.room.FolderDao
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



    /**
     * Creates a folder in the DB that represents the full collection/gallery
     * of the specified user. This is similar to the collection/all or
     * gallery/all endpoint of DeviantArt.
     *
     * Please note that due to technical limitations of the DeviantArt API,
     * the total number of images might not be completely accurate.
     */
    suspend fun saveFullCollectionAsFolder(
        ownerUsername: String,
        location: StorageLocation,
        shouldRandomize: Boolean
    ): ApiResponse<Unit> {
        val response = folderApi.fetchFolders(
            location = location.asUrlPath(),
            ownerUsername = ownerUsername,
            calculateSize = true,
            filterEmptyFolders = true,
            offset = 0
        )


        if (!response.isSuccessful || response.body()?.folderList.isNullOrEmpty()){
            val error = response.errorBody()?.string() ?: "Could not fetch folders"
            Log.e("API failure", error)
            return ApiResponse.Error(error)
        }


        val responseData = response.body()?.folderList ?: return ApiResponse.Error("Could not fetch folders")

        val totalImages = responseData.sumOf { deviantArtFolder -> deviantArtFolder.totalImages }
        val thumbnail = responseData.firstOrNull()?.getThumbnailUrl()
        val displayName = "${ownerUsername}\'s ${location.asUiFriendlyLabel()}"


        val folder = Folder(
            localId = null,
            remoteId = Folder.ID_IF_FULL_COLLECTION,
            ownerUsername = ownerUsername,
            storedIn = location,
            displayName = displayName,
            shouldRandomize = shouldRandomize,
            thumbnailUrl = thumbnail,
            totalImages = totalImages
        )


        db.insertOrReplace(folder)

        return ApiResponse.Success(Unit)
    }
}