package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem



/**
 * Manages batching DeviantArt media queries for ordered folders that are too large
 * to get everything in advance.
 */
class SequentialBatchTracker(
    private val folderSize: Int,
    private val mediaList: MutableList<DeviantArtMediaItem>
) : BatchTracker {

    private var offset = 0


    override fun getBatchSize() = 48


    override fun getEndOfListThreshold() = 48


    override fun planNextBatch(): Batch {
        val batchSize = minOf(this.getBatchSize(), remainingItemCount())
        val batch = Batch.Consecutive(offset = this.offset, size = batchSize)

        this.offset += batchSize

        return batch
    }


    override fun saveResults(data: List<DeviantArtMediaItem>) {
        mediaList.addAll(data)
    }


    override fun hasMoreData(): Boolean {
        return remainingItemCount() > 0
    }


    private fun remainingItemCount() : Int {
        return this.folderSize - this.offset
    }
}