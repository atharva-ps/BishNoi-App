package com.app.bishnoi.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.app.bishnoi.domain.model.User

// Request DTOs
data class RegisterRequest(
    val username: String,
    val email: String,
    val id: String
)

data class LoginRequest(
    val identifier: String,
    val id: String
)

// ✅ NEW: Get email request
data class GetEmailRequest(
    val username: String
)

// ✅ NEW: Get email response
data class GetEmailResponse(
    val success: Boolean,
    val email: String? = null,
    val message: String? = null
)

// Response DTOs
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: UserDto
)

data class UserDto(
    @SerializedName("_id")
    val _id: String,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val profilePhoto: String? = null,
    val bio: String? = null,
    val createdAt: String? = null
)

fun UserDto.toDomainModel(): User {
    return User(
        id = _id,
        name = username ?: "${firstName ?: ""} ${lastName ?: ""}".trim(),
        email = email,
        profileImage = profilePhoto,
        bio = bio,
        createdAt = createdAt
    )
}
