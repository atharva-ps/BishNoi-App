package com.justbaat.mybishnoiapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Standard error response from backend API
 * Example: { "success": false, "message": "User already exists" }
 */
data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("error")
    val error: String? = null
)