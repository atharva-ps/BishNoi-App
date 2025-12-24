package com.app.bishnoi.data.remote.dto

import com.app.bishnoi.domain.model.Post
import com.app.bishnoi.domain.model.ProfilePost


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

data class PresignedUrlResponse(
    val success: Boolean,
    val uploadUrl: String,
    val fileUrl: String,
    val key: String
)

data class CreatePostResponse(
    val success: Boolean,
    val message: String,
    val post: Post?
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
    val isLikedByCurrentUser: Boolean = false,
    val isAdmin: Boolean = false,
    val postedBy: String? = null,
    val format: String?,
    val isSocial: Boolean = false
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
    isLikedByCurrentUser = isLikedByCurrentUser,
    isAdmin = isAdmin,
    postedBy = postedBy ?: "User",
    format = format ?: "VERTICAL",
    isSocial = isSocial
)

fun PostDto.toProfilePost() = ProfilePost(
    id = id,
    imageUrl = imageUrl
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

data class ReportRequest(
    val reportType: String,
    val message: String,
    val reportedPostId: String,
    val reportedUserId: String
)

data class ReportResponse(
    val success: Boolean,
    val message: String
)
