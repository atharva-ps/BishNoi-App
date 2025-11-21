package com.justbaat.mybishnoiapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// Comment DTOs
data class Comment(
    @SerializedName("_id")
    val id: String,
    val userId: String,
    val userName: String,
    val userImage: String?,
    val content: String,
    val createdAt: String
)

data class CommentRequest(
    val content: String
)

data class CommentsResponse(
    val comments: List<Comment>
)