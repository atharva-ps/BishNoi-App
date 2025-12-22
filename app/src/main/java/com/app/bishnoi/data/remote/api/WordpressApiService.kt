package com.app.bishnoi.data.remote.api

import com.app.bishnoi.data.remote.dto.NewsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WordpressApiService {

    @GET("wp-json/wp/v2/news")
    suspend fun getNews(
        @Query("_embed") embed: Boolean = true,
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): Response<List<NewsDto>>
}
