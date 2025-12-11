package com.justbaat.mybishnoiapp.domain.model

data class Member(
    val id: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val profilePhoto: String?,
    val currentCity: String,
    val currentState: String,
    val createdAt: String?
)
