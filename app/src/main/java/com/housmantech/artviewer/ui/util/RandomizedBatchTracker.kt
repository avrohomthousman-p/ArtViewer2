package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem



/**
 * Manages batching DeviantArt media queries for large folders that are randomized
 */
class RandomizedBatchTracker(
    private val folderSize: Int,
    private val mediaList: MutableList<DeviantArtMediaItem>
) : BatchTracker {


    //Tracks the indexes we need to fetch and copy to the mediaList in this order
    private var remoteIndexes = (0 until folderSize).shuffled()


    override fun getBatchSize() = 10


    override fun getEndOfListThreshold() = 20


    override fun planNextBatch(): Batch {
        val endIndex = minOf(this.getBatchSize(), remoteIndexes.size)
        val batch = Batch.NonConsecutive(remoteIndexes.subList(0, endIndex))

        remoteIndexes = remoteIndexes.subList(endIndex, remoteIndexes.size)

        return batch
    }


    override fun saveResults(data: List<DeviantArtMediaItem>) {
        mediaList.addAll(data)
    }


    override fun hasMoreData(): Boolean {
        return remoteIndexes.isNotEmpty()
    }
}