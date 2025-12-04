package com.justbaat.mybishnoiapp.data.remote.dto

import com.justbaat.mybishnoiapp.domain.model.Post
import com.justbaat.mybishnoiapp.domain.model.ProfilePost


data class PostResponse(
    val success: Boolean,
    val post: PostDto,
    val message: String? = null
)

data class FeedResponse(
    val success: Boolean,
    val posts: List<PostDto>,
    val hasMore: Boolean,
    val message: String? = null
)

data class PostDto(
    val id: String,
    val userId: String,
    val caption: String?,
    val username: String?,
    val userProfilePhoto: String?,
    val imageUrl: String?,
    val visibility: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val isLikedByCurrentUser: Boolean = false
)

fun PostDto.toDomain(): Post = Post(
    id = id,
    userId = userId,
    username = username,          // or a placeholder until backend sends it
    userProfilePhoto = userProfilePhoto,
    caption = caption ?: "",
    imageUrl = imageUrl,
    likesCount = likesCount,
    commentsCount = commentsCount,
    createdAt = createdAt?.substring(0, 10) ?: "",  // e.g. "2025-12-02",
    isLikedByCurrentUser = isLikedByCurrentUser
)

fun PostDto.toProfilePost() = ProfilePost(
    id = id,
    imageUrl = imageUrl
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)
