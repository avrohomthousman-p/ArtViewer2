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


    /**
     * Gets the string representation of thi enum as it should be displayed
     * on the screen for the user to read.
     */
    fun asUiFriendlyLabel(): String {
        return when(this) {
            COLLECTION -> "Collection"
            GALLERY -> "Gallery"
        }
    }
}