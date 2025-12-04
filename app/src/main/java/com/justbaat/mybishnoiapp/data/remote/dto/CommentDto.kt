package com.justbaat.mybishnoiapp.data.remote.dto

import com.justbaat.mybishnoiapp.domain.model.Comment

data class CommentResponse(
    val success: Boolean,
    val comment: CommentDto,
    val message: String? = null
)

data class CommentsListResponse(
    val success: Boolean,
    val comments: List<CommentDto>,
    val message: String? = null
)

data class CommentDto(
    val id: String,
    val postId: String,
    val userId: String,
    val username: String?,
    val userProfilePhoto: String?,
    val text: String,
    val createdAt: String?,
    val updatedAt: String?
)

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    postId = postId,
    userId = userId,
    username = username ?: "User",
    userProfilePhoto = userProfilePhoto,
    text = text,
    createdAt = createdAt
)

data class AddCommentRequest(
    val text: String
)

