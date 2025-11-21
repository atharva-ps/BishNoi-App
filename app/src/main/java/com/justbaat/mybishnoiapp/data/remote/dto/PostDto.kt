package com.justbaat.mybishnoiapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/// Post DTOs
data class Post(
    @SerializedName("_id")
    val id: String,
    val userId: String,
    val userName: String,
    val userImage: String?,
    val content: String,
    val images: List<String>? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: String
)

data class FeedResponse(
    val posts: List<Post>,
    val hasMore: Boolean,
    val nextPage: Int?
)

data class CreatePostRequest(
    val content: String,
    val images: List<String>? = null
)