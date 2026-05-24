package com.deviantart.artviewer.data.util



/**
 * Stores information about an API query and executes that query.
 *
 * This class is used specifically for the `MediaApi.fetchMedia` endpoint and
 * defines which slice of remote media to fetch, as well as which items from
 * that slice should be retained.
 *
 * @property offset The starting index for the API request.
 * @property limit The maximum number of items to request.
 * @property itemsToKeep The indices (relative to the response) of items that
 *                       should be included in the results.
 */
class ArtQuery(
    val offset: Int,
    val limit: Int,
    val itemsToKeep: List<Int>
)
