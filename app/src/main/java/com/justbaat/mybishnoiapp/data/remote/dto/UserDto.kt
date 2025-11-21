package com.justbaat.mybishnoiapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String,
    val bio: String? = null,
    val profileImage: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)
