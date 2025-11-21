package com.justbaat.mybishnoiapp.data.remote.dto

data class FollowResponse(
    val success: Boolean,
    val message: String
)

data class FollowStatusResponse(
    val success: Boolean,
    val isFollowing: Boolean,
    val isOwnProfile: Boolean
)

data class FollowersResponse(
    val success: Boolean,
    val followers: List<FollowerDto>,
    val count: Int,
    val message: String? = null
)

data class FollowingResponse(
    val success: Boolean,
    val following: List<FollowerDto>,
    val count: Int,
    val message: String? = null
)

data class FollowerDto(
    val id: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val profilePhoto: String?,
    val followersCount: Int
)

fun FollowerDto.toDisplayName(): String {
    val fullName = "${firstName ?: ""} ${lastName ?: ""}".trim()
    return fullName.ifEmpty { username }
}
