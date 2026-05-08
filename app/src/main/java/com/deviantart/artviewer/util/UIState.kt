package com.deviantart.artviewer.util


/**
 * Class for tracking the state of the UI as it runs a background process to fetch data
 * to display.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String? = null, val throwable: Throwable? = null) : UiState<Nothing>()
}
