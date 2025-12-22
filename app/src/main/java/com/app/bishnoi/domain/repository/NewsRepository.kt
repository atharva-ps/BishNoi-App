package com.app.bishnoi.domain.repository

import com.app.bishnoi.domain.model.News
import com.app.bishnoi.utils.Resource
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getNews(page: Int = 1): Flow<Resource<List<News>>>
    suspend fun refreshNews(): Flow<Resource<List<News>>>
}
