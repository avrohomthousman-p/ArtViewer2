package com.deviantart.artviewer.data.repository

import com.deviantart.artviewer.data.local.room.Folder
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.remote.DeviantArtMediaItem
import com.deviantart.artviewer.data.remote.MediaApi
import com.deviantart.artviewer.util.ArtQuery
import com.deviantart.artviewer.util.MediaAccumulator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


private const val MAX_ITEMS_SHOWN = 250
private const val MAX_ITEMS_PER_QUERY = 24



/**
 * Repository for getting art to display mostly from the MediaApi.fetchMedia endpoint.
 */
@Singleton
class ArtRepository @Inject constructor(
    private val db: FolderDao,
    private val mediaApi: MediaApi
) {


    /**
     * Fetches art from the folder with the specified ID according to these rules:
     *      Maximum images is [MAX_ITEMS_SHOWN]
     *      The images will be either randomized or sorted (in the same order as the API stores them)
     *      Sort VS Randomize is determined by the folder.shouldRandomize field
     */
    suspend fun fetchFolderContents(localId: Int): List<DeviantArtMediaItem> {
        val folder = db.getFolder(localId)


        // Plan the queries
        val needAllMedia = folder.totalImages <= MAX_ITEMS_SHOWN
        val queries =
            if (folder.shouldRandomize && !needAllMedia)
                planNonConsecutiveQueries(folder)
            else
                planConsecutiveQueries(folder)



        val accumulator = MediaAccumulator(
            mode =
                if (folder.shouldRandomize)
                    MediaAccumulator.Mode.RANDOMIZED
                else
                    MediaAccumulator.Mode.SORTED
        )


        // Run all queries in parallel
        coroutineScope {
            queries.forEach { query ->
                launch { query.runQuery(mediaApi, folder, accumulator) }
            }
        }


        return accumulator.getResults()
    }



    /**
     * Generates a list of queries that will need to be done to get a series of media
     * from DeviantArt IN ORDER.
     */
    private fun planConsecutiveQueries(folder: Folder): List<ArtQuery> {
        val queries = mutableListOf<ArtQuery>()

        val maxIndexAllowed = folder.totalImages.coerceAtMost(MAX_ITEMS_SHOWN)
        var offset = 0
        while(offset < maxIndexAllowed){

            val limit = MAX_ITEMS_PER_QUERY.coerceAtMost(maxIndexAllowed - offset)
            queries.add(
                ArtQuery(
                    offset = offset,
                    limit = limit,
                    itemsToKeep = (0 until limit).toList()
                )
            )
            offset += MAX_ITEMS_PER_QUERY
        }

        return queries
    }



    /**
     * Generates a list of queries that will need to be done to get a random set of media
     * items from the DeviantArt folder. Each query will take as many required items as
     * it can fit without going over the [MAX_ITEMS_PER_QUERY]
     */
    private fun planNonConsecutiveQueries(folder: Folder): List<ArtQuery> {
        val itemsNeeded = chooseItemsToFetch(folder).sorted()
        val queries = mutableListOf<ArtQuery>()

        var i = 0 //Where we are up to in itemsNeeded

        while (i < itemsNeeded.size) {
            val offset = itemsNeeded[i]
            val (lastIndexInQuery, itemsKept) = collectItemsForQuery(itemsNeeded, i, offset)

            val lastRemoteIndex = itemsNeeded[lastIndexInQuery]
            val limit = lastRemoteIndex - offset + 1


            queries.add(
                ArtQuery(
                    offset = offset,
                    limit = limit,
                    itemsToKeep = itemsKept
                )
            )

            // Move i to the first index after this query's range
            i = lastIndexInQuery + 1
        }

        return queries
    }



    /**
     * Given the first item in a query, gathers all subsequent items that could also be
     * captured in the same query (because they are not too far away and they items we
     * need).
     *
     * @param itemsNeeded - A list of all the items we want from the API (remote indexes)
     * @param startIndex - The position (within itemsNeeded) of the first item to be
     *          included in the query.
     *
     * @param offset - The starting index of the first item included in this query on
     *          the API itself (remote index).
     */
    private fun collectItemsForQuery(
        itemsNeeded: List<Int>,
        startIndex: Int,
        offset: Int
    ): Pair<Int, List<Int>> {
        val itemsKept = mutableListOf<Int>()
        var i = startIndex

        itemsKept.add(0)

        while (i + 1 < itemsNeeded.size && itemsNeeded[i + 1] < offset + MAX_ITEMS_PER_QUERY) {
            i++
            itemsKept.add(itemsNeeded[i] - offset)
        }

        return i to itemsKept
    }




    /**
     * Decides which images within a folder will be fetched from DeviantArt and displayed.
     */
    private fun chooseItemsToFetch(folder: Folder): List<Int> {
        return (0 until folder.totalImages)
            .shuffled()
            .take(MAX_ITEMS_SHOWN)
    }
}
