package com.housmantech.artviewer.data.util

import com.housmantech.artviewer.data.local.room.Folder




/**
 * Class whose job it is to plan all the queries needed gather the art we want to display
 * from the appropriate folder.
 */
object ArtQueryPlanner {
    const val MAX_ITEMS_SHOWN = 250
    private const val MAX_ITEMS_PER_QUERY = 24




    /**
     * Builds a list of all the queries we need to get the right art.
     */
    fun planQueriesForFolder(folder: Folder): List<ArtQuery> {
        val needAllMedia = folder.totalImages <= MAX_ITEMS_SHOWN

        return if (folder.shouldRandomize && !needAllMedia) {
            planNonConsecutiveQueries(folder)
        } else {
            planConsecutiveQueries(folder)
        }
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
        val remoteIndexesToFetch = chooseItemsToFetch(folder).sorted()
        val queries = mutableListOf<ArtQuery>()

        var i = 0 //Where we are up to in remoteIndexesToFetch

        while (i < remoteIndexesToFetch.size) {
            val offset = remoteIndexesToFetch[i]
            val (lastIndexInQuery, itemsKept) = collectItemsForQuery(remoteIndexesToFetch, i, offset)

            val lastRemoteIndex = remoteIndexesToFetch[lastIndexInQuery]
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
     * @param remoteIndexesToFetch - A list of all the items we want from the API (remote indexes)
     * @param startIndex - The position (within remoteIndexesToFetch) of the first item to be
     *          included in the query.
     *
     * @param offset - The starting index of the first item included in this query on
     *          the API itself (remote index).
     */
    private fun collectItemsForQuery(
        remoteIndexesToFetch: List<Int>,
        startIndex: Int,
        offset: Int
    ): Pair<Int, List<Int>> {
        val itemsKept = mutableListOf<Int>()
        var i = startIndex

        itemsKept.add(0)

        while (i + 1 < remoteIndexesToFetch.size && isWithinQueryWindow(remoteIndexesToFetch[i + 1], offset)) {
            i++
            itemsKept.add(remoteIndexesToFetch[i] - offset)
        }

        return i to itemsKept
    }



    /**
     * Checks if the specified remote index can be included in the query with the given
     * offset. If including it would make the number of items in the query (query limit)
     * greater then [MAX_ITEMS_PER_QUERY], then it cannot be included.
     */
    private fun isWithinQueryWindow(remoteIndex: Int, offset: Int): Boolean {
        return remoteIndex < offset + MAX_ITEMS_PER_QUERY
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
