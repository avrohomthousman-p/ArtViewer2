package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem


/**
 * Manages batching DeviantArt media queries for small folders where we can just fetch
 * everything upfront.
 */
class FullBatchTracker(
    private val mediaList: MutableList<DeviantArtMediaItem>
) : BatchTracker {

    private var ranBatch = false


    override fun getBatchSize() = BatchConfig.FETCH_FULL_FOLDER_THRESHOLD


    override fun getEndOfListThreshold() = 0


    override fun planNextBatch(): Batch {
        ranBatch = true
        return Batch.All()
    }


    override fun saveResults(data: List<DeviantArtMediaItem>) {
        mediaList.addAll(data)
    }


    override fun hasMoreData(): Boolean {
        return !ranBatch
    }
}
