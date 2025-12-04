package com.justbaat.mybishnoiapp.domain.model

data class Post(
    val id: String,
    val userId: String,
    val username: String?,          // optional, we’ll fill later when backend adds it
    val userProfilePhoto: String?,  // optional
    val caption: String,
    val imageUrl: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val createdAt: String?,         // formatted time string later
    val isLikedByCurrentUser: Boolean = false
)
