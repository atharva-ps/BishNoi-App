package com.app.bishnoi.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NewsDto(
    val id: Int,
    val date: String,
    @SerializedName("date_gmt")
    val dateGmt: String,
    val slug: String,
    val status: String,
    val type: String,
    val link: String,
    val title: TitleDto,
    val content: ContentDto,
    @SerializedName("featured_media")
    val featuredMedia: Int,
    val categories: List<Int>,
    @SerializedName("news-source")
    val newsSource: List<Int>,
    @SerializedName("external_link")
    val externalLink: String?,
    @SerializedName("_embedded")
    val embedded: EmbeddedDto?
)

data class TitleDto(
    val rendered: String
)

data class ContentDto(
    val rendered: String,
    val protected: Boolean
)

data class EmbeddedDto(
    @SerializedName("wp:featuredmedia")
    val featuredMedia: List<FeaturedMediaDto>?,
    @SerializedName("wp:term")
    val terms: List<List<TermDto>>?
)

data class FeaturedMediaDto(
    val id: Int,
    @SerializedName("source_url")
    val sourceUrl: String
)

data class TermDto(
    val id: Int,
    val name: String,
    val slug: String,
    val taxonomy: String
)
