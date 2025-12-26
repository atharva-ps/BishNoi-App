package com.app.bishnoi.domain.model

data class News(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val source: String,
    val externalLink: String?,
    val publishedTime: String,
    val categories: List<String> = emptyList()
)
