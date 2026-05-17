package com.deviantart.artviewer.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * Contains API queries to get media from DeviantArt.
 */
interface MediaApi {


    /**
     * API Query for getting pictures/videos from a specific DeviantArt folder.
     */
    @GET("api/v1/oauth2/{location}/{deviantArtId}")
    suspend fun fetchMedia(
        @Path("location") location: String,
        @Path("deviantArtId") remoteId: String,
        @Query("username") ownerUsername: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 24,
        @Query("mature_content") matureContent: Boolean = true
    ) : Response<DeviantArtMediaResponse>
}