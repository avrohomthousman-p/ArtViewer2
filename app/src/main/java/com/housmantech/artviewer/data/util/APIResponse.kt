package com.housmantech.artviewer.data.util


/**
 * Class for storing the result of an API request, so you can easily return a success or a failure,
 * without try/catch logic.
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
}
