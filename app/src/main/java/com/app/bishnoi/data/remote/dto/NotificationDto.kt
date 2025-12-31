package com.app.bishnoi.data.remote.dto

import com.app.bishnoi.domain.model.Notification
import com.google.gson.annotations.SerializedName


// ✅ ADD THIS
data class FcmTokenRequest(
    val token: String
)

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<NotificationDto>,
    val hasMore: Boolean,
    val message: String? = null
)

data class NotificationDto(
    @SerializedName("_id")
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val body: String,
    val senderId: String,
    val senderName: String?,
    val senderProfilePhoto: String?,
    val postId: String?,
    val commentId: String?,
    val newsId: Int? = null,        // ✅ ADD
    val newsLink: String? = null,
    val isRead: Boolean,
    val createdAt: String
)

data class UnreadCountResponse(
    val success: Boolean,
    val count: Int
)

fun NotificationDto.toDomain() = Notification(
    id = id,
    userId = userId,
    type = type,
    title = title,
    body = body,
    senderId = senderId,
    senderName = senderName ?: "User",
    senderProfilePhoto = senderProfilePhoto,
    postId = postId,
    commentId = commentId,
    newsId = newsId,        // ✅ ADD
    newsLink = newsLink,
    isRead = isRead,
    createdAt = createdAt
)
