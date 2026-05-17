package com.deviantart.artviewer.common

/**
 * Ways DeviantArt might store a folder.
 */
enum class StorageLocation {
    COLLECTION, GALLERY;


    /**
     * Gets the string representation of this enum that DeviantArt expects
     * in its URL's.
     */
    fun asUrlPath(): String {
        return when(this) {
            COLLECTION -> "collections"
            GALLERY -> "gallery"
        }
    }
}