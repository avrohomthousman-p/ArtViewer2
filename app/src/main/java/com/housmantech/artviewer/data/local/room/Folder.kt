package com.housmantech.artviewer.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.housmantech.artviewer.common.StorageLocation



/**
 * Represents a collection of DeviantArt media saved for viewing within the app.
 * This typically corresponds to a single folder on DeviantArt, but may also
 * represent an entire gallery or collection belonging to a DeviantArt user.
 */
@Entity(
    tableName = "folders",
    indices = [
        Index(
            value = ["remoteId", "ownerUsername", "storedIn"],
            unique = true
        )
    ]
)
data class Folder constructor (
    @PrimaryKey(autoGenerate = true)
    val localId: Int? = null,

    //DeviantArt folder ID or [ID_IF_FULL_COLLECTION] if it's the full collection/gallery
    val remoteId: String,

    val ownerUsername: String,

    val storedIn: StorageLocation,

    val displayName: String,

    val shouldRandomize: Boolean,

    val thumbnailUrl: String?,

    val totalImages: Int
){
    companion object {
        const val ID_IF_FULL_COLLECTION = "__ALL__"
    }
}
