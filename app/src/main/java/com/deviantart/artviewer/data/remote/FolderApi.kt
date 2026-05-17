package com.deviantart.artviewer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query



/**
 * Endpoint for getting all folders from a specific user on DeviantArt.
 */
interface FolderApi {

    @GET("api/v1/oauth2/{location}/folders")
    suspend fun fetchFolders(
        @Path("location") location: String,
        @Query("username") ownerUsername: String,
        @Query("calculate_size") calculateSize: Boolean = true,
        @Query("filter_empty_folder") filterEmptyFolders: Boolean = true,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 50,
        @Query("mature_content") allowMatureContent: Boolean = true
    ) : Response<DeviantArtFolderResponse>
}