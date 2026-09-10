package com.housmantech.artviewer.data.repository

import android.util.Log
import com.housmantech.artviewer.data.local.room.Folder
import com.housmantech.artviewer.data.local.room.FolderDao
import com.housmantech.artviewer.data.remote.DeviantArtMediaItem
import com.housmantech.artviewer.data.remote.MediaApi
import com.housmantech.artviewer.data.util.ApiResponse
import com.housmantech.artviewer.data.util.ArtQuery
import com.housmantech.artviewer.data.util.ArtQueryPlanner
import com.housmantech.artviewer.data.util.AtomicNullableMin
import com.housmantech.artviewer.data.util.MediaAccumulator
import com.housmantech.artviewer.data.util.safeApiCall
import com.housmantech.artviewer.ui.util.Batch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject
import javax.inject.Singleton


const val QUERY_PAGE_SIZE = 24
const val MAX_CONCURRENT_QUERIES = 4



/**
 * Repository for getting art to display mostly from the MediaApi.fetchMedia endpoint.
 */
@Singleton
class ArtRepository @Inject constructor(
    private val db: FolderDao,
    private val mediaApi: MediaApi
) {


    private val semaphore = Semaphore(MAX_CONCURRENT_QUERIES)



    /**
     * Fetches DeviantArt media from the API as required by the batch object that is provided
     */
    suspend fun runBatch(folder: Folder, batch: Batch): List<DeviantArtMediaItem> {
        return when (batch) {
            is Batch.All -> fetchFullFolder(folder)
            is Batch.Consecutive -> fetchConsecutiveMedia(folder, batch.offset, batch.size)
            is Batch.NonConsecutive -> fetchNonConsecutiveMedia(folder, batch.indices)
        }
    }



    private suspend fun fetchConsecutiveMedia(folder: Folder, offset: Int, mediaCount: Int): List<DeviantArtMediaItem> {
        val mediaReceived = arrayOfNulls<DeviantArtMediaItem>(mediaCount)

        coroutineScope {
            for (i in 0 until mediaCount step QUERY_PAGE_SIZE){
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        val data = runQuery(folder = folder, offset = offset + i)

                        //Copy the results
                        val numItemsToCopy = minOf(data.size, mediaCount - i)
                        for (j in 0 until numItemsToCopy){
                            mediaReceived[i + j] = data[j]
                        }
                    }
                }
            }
        }


        return mediaReceived
            .filterNotNull()
            .filter(::isValidMedia)
    }



    /**
     * Fetches media items for the given list of remote indices and returns the results
     * in the same order as the input (not sorted by remote index).
     *
     * Each index is fetched individually. If the corresponding media item is invalid
     * (null, mature content, paywalled, or otherwise unusable), it is omitted from the results.
     *
     * @param folder The folder whose media items should be fetched.
     * @param remoteIndexes A list of remote DeviantArt indices to fetch, in the order
     *                      they should appear in the result.
     *
     * @return A list of media items matching the order of `remoteIndexes`, with the invalid
     * items removed. An item is invalid if:
     *             -the fetch failed
     *             -the item was not allowed (mature content, behind paywall, or not a supported media type)
     *             -the item was does not exist (folder was smaller than expected).
     */
    private suspend fun fetchNonConsecutiveMedia(folder: Folder, remoteIndexes: List<Int>): List<DeviantArtMediaItem> {
        val results = arrayOfNulls<DeviantArtMediaItem>(remoteIndexes.size)


        // Pair each remote index with its local position
        val queue = ArrayDeque(remoteIndexes.withIndex().toList())

        coroutineScope {
            repeat(MAX_CONCURRENT_QUERIES) {
                launch(Dispatchers.IO) {
                    while (true) {
                        val next = synchronized(queue) {
                            if (queue.isEmpty()) return@launch
                            queue.removeFirst()
                        }

                        val (localIndex, remoteIndex) = next
                        val item = fetchSingleMediaItem(folder, remoteIndex)
                        results[localIndex] = item
                    }
                }
            }
        }


        return results.filterNotNull()
    }



    /**
     * Fetches one specific media item in a specific place within the folder.
     *
     * @returns the media item at that index or null if there is no such index or
     * an error occurred.
     */
    private suspend fun fetchSingleMediaItem(folder: Folder, remoteIndex: Int): DeviantArtMediaItem? {
        val response = safeApiCall {
            mediaApi.fetchMedia(
                location = folder.storedIn.asUrlPath(),
                remoteId = folder.folderIdForApi(),
                ownerUsername = folder.ownerUsername,
                offset = remoteIndex,
                limit = 1
            )
        }

        when(response) {
            is ApiResponse.Error -> {
                Log.e("Art Fetching Failure", response.message)
                return null
            }
            is ApiResponse.Success -> {
                val responseData = response.data.media

                if (responseData.isEmpty()){
                    Log.e("Art Fetching Failure", "Got no data in a query")
                    return null
                }


                val firstItem = responseData[0]
                return if (isValidMedia(firstItem)) firstItem else null
            }
        }
    }



    /**
     * Gets all the media of an entire folder at once, removing all missing or blocked items.
     */
    private suspend fun fetchFullFolder(folder: Folder): List<DeviantArtMediaItem> {
        val results = fetchConsecutiveMedia(folder, 0, folder.totalImages)

        return (
            if (folder.shouldRandomize) {
                results.shuffled()
            }
            else {
                results
            }
        )
    }


    private suspend fun runQuery(folder: Folder, offset: Int): List<DeviantArtMediaItem> {
        val response = safeApiCall {
            mediaApi.fetchMedia(
                location = folder.storedIn.asUrlPath(),
                remoteId = folder.folderIdForApi(),
                ownerUsername = folder.ownerUsername,
                offset = offset,
                limit = QUERY_PAGE_SIZE
            )
        }


        val results = mutableListOf<DeviantArtMediaItem>()

        when (response) {
            is ApiResponse.Error -> {
                Log.e("Art Fetching Failure", response.message)
                return results   // empty
            }

            is ApiResponse.Success -> {
                val data = response.data.media

                if (data.size < QUERY_PAGE_SIZE){
                    Log.w("Art Fetching Failure", "Fewer results then expected")
                }

                results.addAll(data)
                return results
            }
        }
    }


















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
                    runQuery(query, folder, accumulator, earliestInvalidIndex)
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


        val response = safeApiCall {
            mediaApi.fetchMedia(
                location = folder.storedIn.asUrlPath(),
                remoteId = remoteIdForUrl,
                ownerUsername = folder.ownerUsername,
                offset = queryData.offset,
                limit = queryData.limit
            )
        }


        when (response){
            is ApiResponse.Error -> {
                Log.e("Art Fetching Failure", response.message)
                return
            }
            is ApiResponse.Success -> {
                val responseData = response.data.media

                if (responseData.isEmpty()){
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
        }
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
