package com.deviantart.artviewer.data.repository

import android.util.Log
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.remote.DeviantArtMediaItem
import com.deviantart.artviewer.data.remote.MediaApi
import com.deviantart.artviewer.data.util.ArtQuery
import com.deviantart.artviewer.data.util.ArtQueryPlanner
import com.deviantart.artviewer.data.util.AtomicNullableMin
import com.deviantart.artviewer.data.util.MediaAccumulator
import kotlinx.coroutines.Dispatchers
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

        val earliestInvalidIndex = AtomicNullableMin()


        // Run all queries in parallel
        coroutineScope {
            queries.forEach { query ->
                launch(Dispatchers.IO) {
                    try {
                        runQuery(query, folder, accumulator, earliestInvalidIndex)
                    }
                    catch (e: Exception) {
                        Log.e("Query failure", e.message, e)
                    }
                }
            }
        }


        updateImageCountInDB(folder, earliestInvalidIndex.get())

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
     * results in the provided accumulator. If the query ends up out of
     * bounds, this function also updates the tracker for earliestInvalidIndex
     * so the folder can be corrected later.
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
     * @param earliestInvalidIndex A threadsafe variable that is tracking the
     *          earliest index that was out of bounds across all queries.
     */
    private suspend fun runQuery(
        queryData: ArtQuery,
        folder: Folder,
        accumulator: MediaAccumulator,
        earliestInvalidIndex: AtomicNullableMin
    ){
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



        val invalidIndex = gatherQueryResults(
            responseData = responseData,
            queryData = queryData,
            folder = folder,
            accumulator = accumulator
        )


        earliestInvalidIndex.updateMin(invalidIndex)
    }



    /**
     * Extract the items we need from the query response and add them to the accumulator.
     * If some of the indexes we are supposed to save are out of bounds (this happens if
     * the DeviantArt folder is smaller than the totalImages we have saved on the DB) then
     * this function returns the earliest invalid index so it can be used to adjust the
     * folder's total images. If all indexes are in bounds, null is returned.
     *
     *
     * @param responseData - The API response we got for this query.
     * @param queryData - The information about the query and what parts of it we need to keep.
     * @param folder - The folder these results belong to.
     * @param accumulator - An object used to store the results we want to keep.
     * @return the earliest index we needed that was out of bounds, or null if nothing was
     *          out of bounds.
     */
    private fun gatherQueryResults(
        responseData: List<DeviantArtMediaItem>,
        queryData: ArtQuery,
        folder: Folder,
        accumulator: MediaAccumulator
    ): Int? {

        val addItem =
            if (folder.shouldRandomize) {
                { item: DeviantArtMediaItem, _: Int -> accumulator.addItem(item) }
            }
            else {
                { item: DeviantArtMediaItem, index: Int -> accumulator.addItem(item, index) }
            }



        for (index in queryData.itemsToKeep) {
            if (index >= responseData.size) {
                return index + queryData.offset //earliest invalid index
            }

            val item = responseData[index]
            if (!isValidMedia(item)) continue


            addItem(item, index + queryData.offset)
        }

        return null
    }



    private fun isValidMedia(mediaItem: DeviantArtMediaItem): Boolean {
        val isBlocked = mediaItem.tierAccess != null

        val hasVideo = !mediaItem.getVideoUrl().isNullOrEmpty()
        val hasImage = !mediaItem.getImageUrl().isNullOrEmpty()

        val hasExactlyOneMedia = hasVideo xor hasImage

        return !isBlocked && hasExactlyOneMedia
    }



    private suspend fun updateImageCountInDB(folder: Folder, imageCount: Int?){
        if (imageCount != null) {
            coroutineScope {
                db.insertOrReplace(folder.copy(totalImages = imageCount))
            }
        }
    }
}
