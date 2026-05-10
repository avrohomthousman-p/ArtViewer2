package com.deviantart.artviewer.data.local.room

import androidx.room.TypeConverter



/**
 * Ways DeviantArt might store a folder.
 */
enum class StorageLocation {
    COLLECTION, GALLERY
}



/**
 * Tools for converting StorageLocation instances to and from String so
 * it can be stored in the DB.
 */
class Converters {
    @TypeConverter
    fun fromFolderType(value: StorageLocation): String = value.name

    @TypeConverter
    fun toFolderType(value: String): StorageLocation = StorageLocation.valueOf(value)
}
