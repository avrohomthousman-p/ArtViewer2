package com.deviantart.artviewer.data.util

import com.deviantart.artviewer.common.StorageLocation
import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.remote.DeviantArtFolder
import com.deviantart.artviewer.data.remote.SampleDBFolder


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



/**
 * Convert to a Folder object that is NOT yet saved to the DB.
 */
fun SampleDBFolder.toFolder(): Folder {
    return Folder(
        localId = null,
        remoteId = this.remoteId,
        ownerUsername = this.ownerUsername,
        storedIn = StorageLocation.valueOf(this.storedIn),
        displayName = this.displayName,
        shouldRandomize = this.shouldRandomize,
        thumbnailUrl = this.thumbnailUrl,
        totalImages = this.totalImages
    )
}
