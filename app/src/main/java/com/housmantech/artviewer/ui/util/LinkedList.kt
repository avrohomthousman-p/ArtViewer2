package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem



/**
 * A linkedList implementation specialized to hold the DeviantArt media that should be lazy loaded
 * as you access items. Invalid items are removed as we load them.
 */
interface LinkedList {

    /**
     * The total number of media items we expect to load. This number will go down as
     * invalid items are found and removed from the list.
     */
    fun size(): Int


    /**
     * Fetch the media we are up to.
     */
    fun getCurrentItem(): LazyMediaItem


    /**
     * Returns the LazyMediaItem located a given number of positions away from the cursor.
     *
     * The distance is measured relative to the current cursor position:
     * - A distance of 0 returns the item at the cursor.
     * - A positive distance walks forward through the list (cursor.next, cursor.next.next, ...).
     * - A negative distance walks backward through the list (cursor.prev, cursor.prev.prev, ...).
     *
     * If the requested position lies outside the bounds of the list, this function returns null.
     */
    fun getDataAt(distanceFromCursor: Int): LazyMediaItem?




    /**
     * Returns a list of remote indices for the next `numberOfItems` nodes that we need to
     * fetch from DevianArt. These items are represented by their remote index.
     *
     * Unless out of bounds, the returned list always has length `numberOfItems`.
     */
    fun getNextPendingItems(numberOfItems: Int): List<Int>


    /**
     * Takes the provided media item and puts it in the next node that is not yet loaded.
     * If the item is null, that node is removed instead.
     *
     * @return true if the item was added and false if the node was deleted
     */
    fun populateNextPendingItem(mediaItem: DeviantArtMediaItem?): Boolean


    /**
     * Takes the provided list of media items and replaces the next pending nodes with
     * that data. For null items, the node is instead deleted.
     *
     * @return the number of nodes deleted as a result of receiving null media items
     */
    fun populateRange(media: List<DeviantArtMediaItem?>): Int
}