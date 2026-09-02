package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem

class LinkedListV2 : LinkedList {
    private var size: Int = 0


    //Dummy node before the list
    private var head: Node = Node(data = LazyMediaItem.Pending(-1))


    private var tail: Node = head


    private var cursor: Node = head

    private var lastLoadedItem: Node = head



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



    override fun size(): Int {
        return this.size
    }


    override fun getCurrentItem(): LazyMediaItem {
        return cursor.data
    }


    override fun getDataAt(distanceFromCursor: Int): LazyMediaItem? {
        TODO("Not yet implemented")
    }


    override fun getNextPendingItems(numberOfItems: Int): List<Int> {
        TODO("Not yet implemented")
    }


    override fun populateNextPendingItem(mediaItem: DeviantArtMediaItem?): Boolean {
        TODO("Not yet implemented")
    }


    override fun populateRange(media: List<DeviantArtMediaItem?>): Int {
        TODO("Not yet implemented")
    }
}