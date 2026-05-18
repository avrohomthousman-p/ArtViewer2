package com.deviantart.artviewer.data.util

import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.remote.DeviantArtFolder



/**
 * Convert to a Folder object that is NOT yet saved to the DB.
 */
fun DeviantArtFolder.toFolder(
    ownerUsername: String,
    location: StorageLocation
): Folder {
    return Folder(
        localId = null,
        remoteId = this.folderId,
        ownerUsername = ownerUsername,
        storedIn = location,
        displayName = this.folderName,
        shouldRandomize = true,
        thumbnailUrl = this.getThumbnailUrl(),
        totalImages = this.totalImages
    )
}
