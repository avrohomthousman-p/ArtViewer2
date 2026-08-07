package com.housmantech.artviewer.ui.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * Class to store all miscellaneous utils that are not big enough to warrant
 * their own file.
 */
object MiscUtils {

    /**
     * Runs the given suspend [operation] and guarantees that the total execution time
     * lasts at least [minimumMillis] milliseconds.
     *
     * If the block finishes quickly, this function waits for the remaining time.
     * If the block takes longer than [minimumMillis], no extra delay is added.
     *
     * @param minimumMillis The minimum total duration in milliseconds.
     * @param operation The suspend function to execute.
     * @return The return value of [operation].
     */
    suspend fun <T> runWithMinimumDuration(
        minimumMillis: Long = 4000,
        operation: suspend () -> T
    ): T {

        return coroutineScope {
            val actualJob = async { operation() }
            val minimumTime = async { delay(minimumMillis) }

            awaitAll(actualJob, minimumTime)

            return@coroutineScope actualJob.await()
        }
    }
}