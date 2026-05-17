package com.deviantart.artviewer.util

import com.deviantart.artviewer.data.remote.DeviantArtMediaItem
import java.util.Collections



/**
 * Accumulates media items fetched from DeviantArt across multiple queries.
 *
 * The accumulator operates in one of two modes:
 *
 *  - [Mode.SORTED] — items must be added with an explicit (remote) index, and
 *    the final result list will be sorted by that index.
 *
 *  - [Mode.RANDOMIZED] — items are added without an index, and the final result
 *    list will be returned in randomized order.
 *
 * This class is thread‑safe: internal storage uses synchronized lists to allow
 * concurrent additions from parallel fetch operations.
 *
 * Usage:
 *  - Call [addItem] with an index when in SORTED mode.
 *  - Call [addItem] without an index when in RANDOMIZED mode.
 *  - Calling the wrong overload for the current mode will throw an
 *    [IllegalArgumentException].
 *
 * After all items have been added, call [getResults] to obtain the final list.
 *
 * @property mode Determines whether items are stored with indices or randomized.
 */
class MediaAccumulator(val mode: Mode) {
    enum class Mode { SORTED, RANDOMIZED }


    private val sortedData = Collections.synchronizedList(mutableListOf<Pair<DeviantArtMediaItem, Int>>())
    private val unsortedData = Collections.synchronizedList(mutableListOf<DeviantArtMediaItem>())




    /**
     * Add an item to the accumulator.
     *
     * Use this only when operating in [Mode.RANDOMIZED].
     *
     * @throws IllegalArgumentException if called in SORTED mode.
     */
    fun addItem(item: DeviantArtMediaItem){
        if (this.mode == Mode.SORTED){
            throw IllegalArgumentException("Accumulator set to ${this.mode} cannot accept items with no index")
        }

        unsortedData.add(item)
    }



    /**
     * Add an item to the accumulator.
     *
     * Use this only when operating in [Mode.SORTED].
     *
     * @throws IllegalArgumentException if called in RANDOMIZED mode.
     */
    fun addItem(item: DeviantArtMediaItem, index: Int){
        if (this.mode == Mode.RANDOMIZED){
            throw IllegalArgumentException("Accumulator set to ${this.mode} should not have an index")
        }

        sortedData.add(item to index)
    }



    /**
     * Get all the items added to the accumulator, sorted or randomized, depending on [mode].
     */
    fun getResults(): List<DeviantArtMediaItem> {
        return when (this.mode) {
            Mode.RANDOMIZED -> this.unsortedData.toMutableList().also { it.shuffle() }
            Mode.SORTED -> this.sortedData.sortedBy { it.second }.map { it.first }
        }
    }
}
