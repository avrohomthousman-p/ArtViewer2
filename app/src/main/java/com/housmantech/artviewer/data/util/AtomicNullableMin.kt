package com.housmantech.artviewer.data.util

import java.util.concurrent.atomic.AtomicReference



/**
 * Class that tracks the minimum value of something, and uses null for when no value has
 * been set.
 */
@Suppress("ATOMIC_REF_WITHOUT_CONSISTENT_IDENTITY")
class AtomicNullableMin {
    private val min = AtomicReference<Int?>(null)


    /**
     * Get the current minimum.
     */
    fun get(): Int? = min.get()


    /**
     * Sets the minimum value to the new minimum only if it is less then the existing min.
     * This is done in a threadsafe manner.
     */
    fun updateMin(newValue: Int?) {
        if (newValue == null) return

        while (true) {
            val current = min.get()

            // If no value yet, try to set it
            if (current == null) {
                if (min.compareAndSet(null, newValue)) return
            }
            // If newValue is smaller, try to update
            else if (newValue < current) {
                if (min.compareAndSet(current, newValue)) return
            }
            // If newValue is larger, ignore it
            else {
                return
            }
        }
    }
}
