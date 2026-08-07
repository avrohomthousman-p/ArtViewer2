package com.housmantech.artviewer.data.repository

import android.util.Log
import com.housmantech.artviewer.common.StorageLocation
import com.housmantech.artviewer.data.local.room.Folder
import com.housmantech.artviewer.data.local.room.FolderDao
import com.housmantech.artviewer.data.remote.DeviantArtFolderResponse
import com.housmantech.artviewer.data.remote.FolderApi
import com.housmantech.artviewer.data.util.ApiResponse
import com.housmantech.artviewer.data.util.safeApiCall
import com.housmantech.artviewer.data.util.toFolder
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

        val response = safeApiCall<DeviantArtFolderResponse> {
            folderApi.fetchFolders(
                location = location.asUrlPath(),
                ownerUsername = ownerUsername,
                calculateSize = true,
                filterEmptyFolders = true,
                offset = offset
            )
        }


        when (response){
            is ApiResponse.Error -> {
                Log.e("API failure", response.message)
                return response
            }
            is ApiResponse.Success -> {
                if (response.data.folderList.isEmpty()) {
                    Log.e("API failure", "No folders found")
                    return ApiResponse.Error("No folders found")
                }


                val resultAsFolders = response.data
                    .folderList.map { deviantArtFolder ->
                        deviantArtFolder.toFolder(ownerUsername, location)
                    }


                return ApiResponse.Success(resultAsFolders)
            }
        }
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

        val response = safeApiCall<DeviantArtFolderResponse> {
            folderApi.fetchFolders(
                location = location.asUrlPath(),
                ownerUsername = ownerUsername,
                calculateSize = true,
                filterEmptyFolders = true,
                offset = 0
            )
        }



        when (response) {
            is ApiResponse.Error -> {
                Log.e("API failure", response.message)
                return response
            }
            is ApiResponse.Success -> {
                if (response.data.folderList.isEmpty()){
                    Log.e("API failure", "No art found")
                    return ApiResponse.Error("No art found")
                }



                val responseData = response.data.folderList
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
    }
}