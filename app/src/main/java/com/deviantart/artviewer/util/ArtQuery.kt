package com.deviantart.artviewer.util

import android.util.Log
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.remote.DeviantArtMediaItem
import com.deviantart.artviewer.data.remote.MediaApi



/**
 * Stores information about an API query we will execute.
 *
 * This class is used specifically for the `MediaApi.fetchMedia` endpoint and
 * defines which slice of remote media to fetch, as well as which items from
 * that slice should be retained.
 *
 * @property offset The starting index for the API request.
 * @property limit The maximum number of items to request.
 * @property itemsToKeep The indices (relative to the response) of items that
 *                       should be added to the accumulator.
 */
data class ArtQuery(
    val offset: Int,
    val limit: Int,
    val itemsToKeep: List<Int>
) {


    /**
     * Executes this query against the DeviantArt API and stores the selected
     * results in the provided accumulator.
     *
     * The function:
     *  - Calls `MediaApi.fetchMedia` for the configured offset and limit.
     *  - Performs basic error handling on the response.
     *  - Extracts only the items specified in [itemsToKeep].
     *  - Adds them to [accumulator], optionally preserving global ordering
     *    depending on [Folder.shouldRandomize].
     *
     * @param mediaApi The API instance used to perform the request.
     * @param folder The folder whose media is being fetched.
     * @param accumulator Collects all media items across multiple queries.
     */
    suspend fun runQuery(mediaApi: MediaApi, folder: Folder, accumulator: MediaAccumulator){
        val response = mediaApi.fetchMedia(
            location = folder.storedIn.asUrlPath(),
            remoteId = folder.remoteId ?: "all",
            ownerUsername = folder.ownerUsername,
            offset = this.offset,
            limit = this.limit
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



        //Collect results
        if (folder.shouldRandomize) {
            itemsToKeep.forEach { index ->
                responseData[index]
                    .takeIf { isValidMedia(it) }
                    ?.let { accumulator.addItem(it) }
            }
        } else {
            itemsToKeep.forEach { index ->
                responseData[index]
                    .takeIf { isValidMedia(it) }
                    ?.let { art ->
                        accumulator.addItem(art, index + offset)
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
