package com.app.bishnoi.domain.model

data class Notification(
    val id: String,
    val userId: String,
    val type: String, // "like", "comment", "follow", "mention"
    val title: String,
    val body: String,
    val senderId: String,
    val senderName: String,
    val senderProfilePhoto: String?,
    val postId: String?,
    val commentId: String?,
    val newsId: Int? = null,        // ✅ For news notifications
    val newsLink: String? = null,
    val isRead: Boolean,
    val createdAt: String
)
