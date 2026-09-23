package com.housmantech.artviewer.data.repository

import android.util.Log
import com.housmantech.artviewer.data.local.room.Folder
import com.housmantech.artviewer.data.local.room.FolderDao
import com.housmantech.artviewer.data.remote.DeviantArtMediaItem
import com.housmantech.artviewer.data.remote.MediaApi
import com.housmantech.artviewer.data.util.ApiResponse
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
    private val settingsRepo: SettingsRepository,
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
        val allowMatureContent = settingsRepo.shouldShowMatureContent()
        val mediaReceived = arrayOfNulls<DeviantArtMediaItem>(mediaCount)

        coroutineScope {
            for (i in 0 until mediaCount step QUERY_PAGE_SIZE){
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        val data = runQuery(folder = folder, offset = offset + i, allowMatureContent = allowMatureContent)

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
        val allowMatureContent = settingsRepo.shouldShowMatureContent()
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
                        val item = fetchSingleMediaItem(folder, remoteIndex, allowMatureContent)
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
    private suspend fun fetchSingleMediaItem(
        folder: Folder,
        remoteIndex: Int,
        allowMatureContent: Boolean
    ): DeviantArtMediaItem? {

        val response = safeApiCall {
            mediaApi.fetchMedia(
                location = folder.storedIn.asUrlPath(),
                remoteId = folder.folderIdForApi(),
                ownerUsername = folder.ownerUsername,
                offset = remoteIndex,
                limit = 1,
                matureContent = allowMatureContent
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


    private suspend fun runQuery(folder: Folder, offset: Int, allowMatureContent: Boolean): List<DeviantArtMediaItem> {
        val response = safeApiCall {
            mediaApi.fetchMedia(
                location = folder.storedIn.asUrlPath(),
                remoteId = folder.folderIdForApi(),
                ownerUsername = folder.ownerUsername,
                offset = offset,
                limit = QUERY_PAGE_SIZE,
                matureContent = allowMatureContent
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
