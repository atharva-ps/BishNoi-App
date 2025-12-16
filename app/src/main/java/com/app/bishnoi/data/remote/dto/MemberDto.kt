package com.app.bishnoi.data.remote.dto

import com.app.bishnoi.domain.model.Member

data class MembersResponse(
    val success: Boolean,
    val members: List<MemberDto>,
    val totalCount: Int,
    val hasMore: Boolean,
    val message: String? = null
)

data class StatesResponse(
    val success: Boolean,
    val states: List<String>,
    val message: String? = null
)

data class MemberDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val profilePhoto: String?,
    val currentCity: String,
    val currentState: String,
    val createdAt: String?
)

fun MemberDto.toDomain(): Member = Member(
    id = id,
    firstName = firstName,
    lastName = lastName,
    username = username,
    profilePhoto = profilePhoto,
    currentCity = currentCity,
    currentState = currentState,
    createdAt = createdAt
)
