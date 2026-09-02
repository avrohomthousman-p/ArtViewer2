package com.housmantech.artviewer.ui.util

import com.housmantech.artviewer.data.remote.DeviantArtMediaItem


/**
 * Stores a media item that is lazy loaded. If it is not yet loaded, it stores the (remote) index
 * of the item it should load. If it is loaded it stores the actual item.
 */
sealed class LazyMediaItem {
    data class Loaded(val media: DeviantArtMediaItem) : LazyMediaItem()
    data class Pending(val remoteIndex: Int) : LazyMediaItem()
}
