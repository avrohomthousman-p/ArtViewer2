package com.deviantart.artviewer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



/**
 * Data class for folder data provided by the DeviantArt endpoint [FolderApi.fetchFolders].
 */
@Serializable
data class DeviantArtFolderResponse(
    @SerialName("results")
    val folderList: List<DeviantArtFolder>
)



@Serializable
data class DeviantArtFolder(
    @SerialName("folderid")
    val folderId: String,

    @SerialName("name")
    val folderName: String,

    @SerialName("size")
    val totalImages: Int,

    @SerialName("thumb")
    val thumbnail: ThumbnailContainer
)



@Serializable
data class ThumbnailContainer(
    @SerialName("thumbs")
    val thumbnailList: List<DeviantArtThumbnail>
)



/**
 * Thumbnail information for a DeviantArt folder
 */
@Serializable
data class DeviantArtThumbnail(
    @SerialName("src")
    val url: String
)
