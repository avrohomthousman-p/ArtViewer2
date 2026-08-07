package com.housmantech.artviewer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Stores image/video data supplied by the DeviantArt API
 */
@Serializable
data class DeviantArtMediaResponse(
    @SerialName("results")
    val media: List<DeviantArtMediaItem>
)



@Serializable
data class DeviantArtMediaItem (
    @SerialName("title")
    val title: String,

    @SerialName("tier_access")
    val tierAccess: String? = null,

    @SerialName("videos")
    val videos: List<DeviantArtVideo>? = null,

    @SerialName("content")
    val image: DeviantArtImage? = null
){

    fun getVideoUrl(): String? {
        return this.videos?.firstOrNull()?.url
    }


    fun getImageUrl(): String? {
        return this.image?.url
    }


}



@Serializable
data class DeviantArtVideo(
    @SerialName("src")
    val url: String
)



@Serializable
data class DeviantArtImage(
    @SerialName("src")
    val url: String
)
