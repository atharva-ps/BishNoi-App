package com.justbaat.mybishnoiapp.utils

import com.google.gson.Gson
import com.justbaat.mybishnoiapp.data.remote.dto.ErrorResponse
import retrofit2.HttpException

/**
 * Extension function to parse error response from Retrofit HttpException
 */
fun HttpException.parseErrorMessage(): String {
    return try {
        val errorBody = this.response()?.errorBody()?.string()
        if (errorBody.isNullOrBlank()) {
            return getDefaultErrorMessage(this.code())
        }

        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
        errorResponse.message.ifBlank {
            getDefaultErrorMessage(this.code())
        }
    } catch (e: Exception) {
        getDefaultErrorMessage(this.code())
    }
}

/**
 * Get default error message based on HTTP status code
 */
private fun getDefaultErrorMessage(code: Int): String {
    return when (code) {
        400 -> "Invalid request. Please check your input."
        401 -> "Unauthorized. Please login again."
        403 -> "Access forbidden."
        404 -> "Resource not found."
        409 -> "Conflict. Resource already exists."
        422 -> "Validation error. Please check your input."
        500 -> "Server error. Please try again later."
        502 -> "Bad gateway. Please try again later."
        503 -> "Service unavailable. Please try again later."
        else -> "An error occurred. Please try again."
    }
}