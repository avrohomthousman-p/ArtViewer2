package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem



object BatchConfig {
    const val FETCH_FULL_FOLDER_THRESHOLD = 264
}




/**
 * Object whose job it is to decide what media to query next and save it to the results list
 */
interface BatchTracker {


    /**
     * Gets the number of media items we fetch for a single batch
     */
    fun getBatchSize(): Int


    /**
     * When the pager is this many items away from the end of the list of media,
     * it's time to fetch more media.
     */
    fun getEndOfListThreshold(): Int


    /**
     * Create a batch request for the data that should be fetched next
     */
    fun planNextBatch(): Batch


    /**
     * Copies the fetched media into the results list so it can be accessed in the front end
     */
    fun saveResults(data: List<DeviantArtMediaItem>)


    /**
     * Returns true if there is more data in the folder to fetch.
     *
     * This is decided by looking at the number of items we fetched so far and the total items
     * in the folder. It is possible to get a false positive if we have an incorrect folder
     * size or many invalid results are filtered out.
     */
    fun hasMoreData(): Boolean
}