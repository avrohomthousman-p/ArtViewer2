package com.deviantart.artviewer.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey



/**
 * Represents a collection of DeviantArt media saved for viewing within the app.
 * This typically corresponds to a single folder on DeviantArt, but may also
 * represent an entire gallery or collection belonging to a DeviantArt user.
 */
@Entity(tableName = "folders")
data class Folder (
    @PrimaryKey(autoGenerate = true)
    val localId: Int,

    //DeviantArt folder ID or null if it's the full collection/gallery
    val remoteId: String?,

    val ownerUsername: String,

    val storedIn: StorageLocation,

    val displayName: String,

    val shouldRandomize: Boolean,

    val thumbnailUrl: String?,

    val totalImages: Int
)