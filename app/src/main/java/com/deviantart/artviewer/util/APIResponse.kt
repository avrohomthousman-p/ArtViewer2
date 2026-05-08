package com.deviantart.artviewer.util


/**
 * Class for storing the result of an API request, which can be a success or a failure.
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
}
