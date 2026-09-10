package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem



object BatchTrackerFactory {

    fun create(
        folderSize: Int,
        shouldRandomize: Boolean,
        mediaList: MutableList<DeviantArtMediaItem>
    ): BatchTracker {

        // Case 1: Small folder → load everything at once
        if (folderSize <= BatchConfig.FETCH_FULL_FOLDER_THRESHOLD) {
            return FullBatchTracker(
                mediaList = mediaList
            )
        }

        // Case 2: Large folder, randomized → use planned indices
        if (shouldRandomize) {
            return RandomizedBatchTracker(
                folderSize = folderSize,
                mediaList = mediaList
            )
        }

        // Case 3: Large folder, sequential → consecutive chunks
        return SequentialBatchTracker(
            folderSize = folderSize,
            mediaList = mediaList
        )
    }
}
