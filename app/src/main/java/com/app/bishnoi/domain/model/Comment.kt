package com.app.bishnoi.domain.model

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val userProfilePhoto: String?,
    val text: String,
    val createdAt: String?
)
