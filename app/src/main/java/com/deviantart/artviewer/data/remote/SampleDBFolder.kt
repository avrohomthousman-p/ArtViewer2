package com.deviantart.artviewer.data.remote

import kotlinx.serialization.Serializable



/**
 * Represents folder data as provided by the endpoint for starter DB folders.
 *
 * location is taken as a string "GALLERY" or "COLLECTION" and must be converted
 * manually.
 */
@Serializable
data class SampleDBFolder(
    val remoteId: String,
    val ownerUsername: String,
    val storedIn: String,
    val displayName: String,
    val shouldRandomize: Boolean,
    val thumbnailUrl: String?,
    val totalImages: Int
)
