package com.housmantech.artviewer.ui.util

/**
 * Represents the different ways we might need DeviantArt media fetched
 */
sealed class Batch {

    /**
     * Fetch a consecutive block of items.
     * Example: offset=200, size=100 → fetch indices [200..299]
     */
    data class Consecutive(
        val offset: Int,
        val size: Int
    ) : Batch()


    /**
     * Fetch all items in the folder.
     */
    class All : Batch()


    /**
     * Fetch a specific non-consecutive set of indices.
     * Example: [55, 200, 201, 999]
     */
    data class NonConsecutive(
        val indices: List<Int>
    ) : Batch()
}