package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem
import com.housmantech.artviewer.data.util.ArtQueryPlanner.MAX_ITEMS_SHOWN


data class Node(
    var next: Node? = null,
    var prev: Node? = null,
    var data: LazyMediaItem
) {
    constructor(next: Node? = null, prev: Node? = null, remoteIndex: Int) :
            this(next, prev, LazyMediaItem.Pending(remoteIndex))
}



/**
 * Need a linked list so we can track the media as the user scrolls through it and remove items if
 * DeviantArt gives us a null media item (API call failure, mature content, blocked by paywall)
 */
class LinkedList {
    var size: Int = 0
        private set


    //Dummy node before the list
    var head: Node = Node(data = LazyMediaItem.Pending(-1))
        private set


    var tail: Node = head
        private set


    private var cursor: Node = head



    /**
     * Creates a linked list filled with LazyMediaItems that are not yet loaded
     */
    constructor(totalImages: Int, shouldRandomize: Boolean) {
        if (totalImages <= 0)
            return


        val desiredItems =
            if (shouldRandomize)
                (0 until totalImages).shuffled()
            else
                (0 until totalImages)


        //Convert to nodes
        var current = this.tail
        for(item in desiredItems){
            current.next = Node(prev = current, data = LazyMediaItem.Pending(item))
            current = current.next!!
        }

        this.size = totalImages

        this.tail = current
        this.cursor = this.head.next!!
    }


    fun getCurrentItem(): LazyMediaItem {
        return this.cursor.data
    }



    /**
     * Returns a list of remote indices for the next `numberOfItems` nodes starting from
     * `startPosition`. Loaded items are represented as `null`, and pending items are
     * represented by their remote index.
     *
     * Unless out of bounds, the returned list always has length `numberOfItems`, but only
     * the non-null entries represent items that still need to be fetched.
     *
     * The `startingPosition` is relative to the cursor position in the linked list. A value
     * of 0 indicates the node the cursor currently points to. A positive number indicates a
     * starting point that many nodes ahead, and a negative number indicates that many nodes
     * behind.
     */
    fun getFetchWindow(startingPosition: Int, numberOfItems: Int): List<Int?> {
        val remoteIndexes = mutableListOf<Int?>()

        var current = getNodeAt(startingPosition)

        while(remoteIndexes.size < numberOfItems && current != null){
            when(val lazyMediaItem = current.data) {
                is LazyMediaItem.Loaded -> remoteIndexes.add(null)
                is LazyMediaItem.Pending -> remoteIndexes.add(lazyMediaItem.remoteIndex)
            }

            current = current.next
        }

        return remoteIndexes
    }



    fun populateSingleResult(position: Int, mediaItem: DeviantArtMediaItem?){
        val node = getNodeAt(position)

        if (node == null){
            return
        }
        else if (node.data is LazyMediaItem.Loaded) {
            //If we already have the data do nothing
            //This shouldn't happen because we shouldn't query for it in the first place
        }
        else if (mediaItem == null){
            removeNode(node)
        }
        else {
            node.data = LazyMediaItem.Loaded(mediaItem)
        }
    }



    /**
     * Populates a consecutive range of nodes starting at a position relative to the
     * current cursor. The starting position uses the same cursor‑relative indexing
     * rules as getDataAt() and getNodeAt():
     *
     * - A startPosition of 0 refers to the node at the cursor.
     * - A positive startPosition walks forward (cursor.next, cursor.next.next, ...).
     * - A negative startPosition walks backward (cursor.prev, cursor.prev.prev, ...).
     *
     * Each node in the range initially contains a LazyMediaItem.Pending placeholder.
     * For each item in the provided list:
     *
     * - If the item is non-null, the node's data is replaced with
     *   LazyMediaItem.Loaded(item).
     *
     * - If the item is null, the node is removed entirely from the list unless it
     *   is already loaded from a previous query (rare)
     *
     * The operation stops early if traversal reaches the end of the list.
     * No new nodes are appended; this function only updates or removes existing ones.
     */
    fun populateRange(startPosition: Int, media: List<DeviantArtMediaItem?>){
        var current: Node? = getNodeAt(startPosition) ?: return

        for(incomingData in media){
            if (current == null) break

            if (current.data is LazyMediaItem.Loaded) {
                //If we already have the data skip it
            }
            else if (incomingData == null) {
                removeNode(current)
            }
            else {
                current.data = LazyMediaItem.Loaded(incomingData)
            }


            current = current.next
        }
    }



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
    fun getDataAt(distanceFromCursor: Int): LazyMediaItem? {
        return getNodeAt(distanceFromCursor)?.data
    }



    /**
     * Internal helper for getDataAt(). Performs the same traversal logic but returns
     * the underlying Node instead of its LazyMediaItem.
     *
     * Forward traversal is delegated to advanceToNode(), and backward traversal is
     * delegated to goBackToNode(). See those functions for detailed traversal rules.
     */
    private fun getNodeAt(distanceFromCursor: Int): Node? {
        return (
            if (distanceFromCursor >= 0)
                advanceToNode(distanceFromCursor)
            else
                goBackToNode(distanceFromCursor * -1)
        )
    }



    /**
     * Walks forward from the cursor by the given number of positions and returns the
     * Node at that location. A distance of 0 returns the cursor itself.
     *
     * Forward traversal follows the `next` pointers of the doubly‑linked list:
     * cursor.next, cursor.next.next, and so on.
     *
     * If the traversal attempts to move past the last real node (`tail`), this
     * function returns null. The dummy head sentinel is never returned.
     *
     * This function does not modify the cursor; it only reads the list structure.
     */
    private fun advanceToNode(distanceFromCursor: Int): Node? {
        if (distanceFromCursor == 0) return cursor

        var current = cursor
        var remaining = distanceFromCursor

        while (remaining > 0 && current != tail) {
            current = current.next!!
            remaining--
        }

        return if (remaining == 0) current else null
    }



    /**
     * Walks backward from the cursor by the given number of positions and returns
     * the Node at that location. A distance of 0 returns the cursor itself.
     *
     * Backward traversal follows the `prev` pointers of the doubly‑linked list:
     * cursor.prev, cursor.prev.prev, and so on.
     *
     * The dummy head sentinel marks the beginning of the list. If traversal reaches
     * the head before covering the requested distance, this function returns null.
     * The head sentinel is never returned as a valid node.
     *
     * This function does not modify the cursor; it only reads the list structure.
     */
    private fun goBackToNode(distanceFromCursor: Int): Node? {
        if (distanceFromCursor == 0){
            return cursor
        }

        var current: Node = cursor
        var remainingDistance = distanceFromCursor
        while(remainingDistance > 0 && current != head) {
            current = current.prev!!
            remainingDistance--
        }

        return if (current == head) null else current
    }



    /**
     * Removes a node from the doubly‑linked list. The dummy head sentinel can never
     * be removed. This function updates all surrounding pointers and adjusts the
     * tail if necessary.
     */
    private fun removeNode(node: Node) {
        // The dummy head is not a real node and must never be removed.
        if (node == head) {
            throw IllegalArgumentException("Cannot remove the head sentinel")
        }

        val prev = node.prev!!
        val next = node.next

        // Link previous node to next node
        prev.next = next

        // next is only null if we removed the tail
        if (next != null) {
            next.prev = prev
        }
        else {
            tail = prev
        }

        size--
    }
}