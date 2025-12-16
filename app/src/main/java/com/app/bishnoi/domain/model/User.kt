package com.app.bishnoi.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String? = null,
    val bio: String? = null,
    val createdAt: String? = null
)
