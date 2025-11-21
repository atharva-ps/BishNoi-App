package com.justbaat.mybishnoiapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchUsersResponse(
    val success: Boolean,
    val users: List<UserSearchDto>,
    val count: Int
)

data class UserSearchDto(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val profilePhoto: String?,
    val aboutMe: String?,
    val followersCount: Int,
    val followingCount: Int,
    val createdAt: String?
)

fun UserSearchDto.toDisplayName(): String {
    val fullName = "${firstName ?: ""} ${lastName ?: ""}".trim()
    return fullName.ifEmpty { username }
}
