package com.deviantart.artviewer.data.repository

import android.util.Log
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.remote.DeviantArtMediaItem
import com.deviantart.artviewer.data.remote.MediaApi
import com.deviantart.artviewer.data.util.ArtQuery
import com.deviantart.artviewer.data.util.ArtQueryPlanner
import com.deviantart.artviewer.data.util.MediaAccumulator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton



/**
 * Repository for getting art to display mostly from the MediaApi.fetchMedia endpoint.
 */
@Singleton
class ArtRepository @Inject constructor(
    private val db: FolderDao,
    private val mediaApi: MediaApi
) {


    /**
     * Fetches art from the folder with the specified ID according to these rules:
     *      Maximum images fetched is [ArtQueryPlanner.MAX_ITEMS_SHOWN]
     *      The images will be either randomized or sorted (in the same order as the API stores them)
     *      Sort VS Randomize is determined by the folder.shouldRandomize field
     */
    suspend fun fetchFolderContents(localId: Int): List<DeviantArtMediaItem> {
        val folder = db.getFolder(localId)
        val queries = ArtQueryPlanner.planQueriesForFolder(folder)
        val accumulator = buildAccumulator(folder)

        // Run all queries in parallel
        coroutineScope {
            queries.forEach { query ->
                launch {
                    try {
                        runQuery(query, folder, accumulator)
                    }
                    catch (e: Exception) {
                        Log.e("Query failure", e.message, e)
                    }
                }
            }
        }

        return accumulator.getResults()
    }



    private fun buildAccumulator(folder: Folder): MediaAccumulator {
        return MediaAccumulator(
            mode =
                if (folder.shouldRandomize)
                    MediaAccumulator.Mode.RANDOMIZED
                else
                    MediaAccumulator.Mode.SORTED
        )
    }



    /**
     * Executes a query against the DeviantArt API and stores the selected
     * results in the provided accumulator.
     *
     * The function:
     *  - Calls `MediaApi.fetchMedia` for the configured offset and limit.
     *  - Performs basic error handling on the response.
     *  - Extracts only the items specified in [ArtQuery.itemsToKeep].
     *  - Adds them to [accumulator], optionally preserving global ordering
     *    depending on [Folder.shouldRandomize].
     *
     * @param mediaApi The API instance used to perform the request.
     * @param folder The folder whose media is being fetched.
     * @param accumulator Collects all media items across multiple queries.
     */
    private suspend fun runQuery(queryData: ArtQuery, folder: Folder, accumulator: MediaAccumulator){
        val remoteIdForUrl =
            if (folder.remoteId == Folder.ID_IF_FULL_COLLECTION) "all"
            else folder.remoteId

        val response = mediaApi.fetchMedia(
            location = folder.storedIn.asUrlPath(),
            remoteId = remoteIdForUrl,
            ownerUsername = folder.ownerUsername,
            offset = queryData.offset,
            limit = queryData.limit
        )


        val responseData = response.body()?.media

        //Error checking
        if (!response.isSuccessful){
            Log.e("Art Fetching Failure", response.message())
            return
        }
        else if (responseData.isNullOrEmpty()){
            Log.e("Art Fetching Failure", "Got no data in a query")
            return
        }



        gatherQueryResults(
            responseData = responseData,
            queryData = queryData,
            folder = folder,
            accumulator = accumulator
        )
    }



    /**
     * Extract the items we need from the query response and add them to the accumulator.
     */
    private fun gatherQueryResults(
        responseData: List<DeviantArtMediaItem>,
        queryData: ArtQuery,
        folder: Folder,
        accumulator: MediaAccumulator
    ){
        if (folder.shouldRandomize) {
            queryData.itemsToKeep.forEach { index ->
                responseData[index]
                    .takeIf { isValidMedia(it) }
                    ?.let { accumulator.addItem(it) }
            }
        } else {
            queryData.itemsToKeep.forEach { index ->
                responseData[index]
                    .takeIf { isValidMedia(it) }
                    ?.let { art ->
                        accumulator.addItem(art, index + queryData.offset)
                    }
            }
        }
    }



    private fun isValidMedia(mediaItem: DeviantArtMediaItem): Boolean {
        val isBlocked = mediaItem.tierAccess != null

        val hasVideo = !mediaItem.getVideoUrl().isNullOrEmpty()
        val hasImage = !mediaItem.getImageUrl().isNullOrEmpty()

        val hasExactlyOneMedia = hasVideo xor hasImage

        return !isBlocked && hasExactlyOneMedia
    }
}
