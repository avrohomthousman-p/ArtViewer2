package com.deviantart.artviewer.data.util

import retrofit2.Response


private const val ERROR_MESSAGE = "No response from DeviantArt"



/**
 * Executes an API call inside a try catch block to ensure there is no crash.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResponse<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                ApiResponse.Error(ERROR_MESSAGE)
            }
            else {
                ApiResponse.Success(body)
            }
        }
        else {
            val error = response.errorBody()?.string() ?: ERROR_MESSAGE
            ApiResponse.Error(error)
        }
    }
    catch (e: Exception) {
        ApiResponse.Error(e.message ?: "Unknown error")
    }
}
