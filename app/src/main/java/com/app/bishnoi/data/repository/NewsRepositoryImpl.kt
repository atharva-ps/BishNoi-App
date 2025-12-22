package com.app.bishnoi.data.repository

import android.text.Html
import android.util.Log
import com.app.bishnoi.data.remote.api.WordpressApiService
import com.app.bishnoi.domain.model.News
import com.app.bishnoi.domain.repository.NewsRepository
import com.app.bishnoi.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val wordpressApi: WordpressApiService
) : NewsRepository {

    override suspend fun getNews(page: Int): Flow<Resource<List<News>>> = flow {
        try {
            emit(Resource.Loading())

            val response = wordpressApi.getNews(embed = true, perPage = 20, page = page)

            if (response.isSuccessful) {
                val newsItems = response.body()?.map { dto ->
                    News(
                        id = dto.id,
                        title = Html.fromHtml(dto.title.rendered, Html.FROM_HTML_MODE_LEGACY).toString().trim(),
                        description = Html.fromHtml(dto.content.rendered, Html.FROM_HTML_MODE_LEGACY).toString().trim(),
                        imageUrl = dto.embedded?.featuredMedia?.firstOrNull()?.sourceUrl,
                        source = extractSourceName(dto.externalLink),
                        externalLink = dto.externalLink,
                        publishedTime = formatTime(dto.date),
                        categories = dto.embedded?.terms?.flatten()
                            ?.filter { it.taxonomy == "category" }
                            ?.map { it.name } ?: emptyList()
                    )
                } ?: emptyList()

                emit(Resource.Success(newsItems))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch news"
                Log.e("NewsRepository", "Error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("NewsRepository", "Exception: ${e.message}", e)
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override suspend fun refreshNews(): Flow<Resource<List<News>>> = getNews(page = 1)

    private fun extractSourceName(url: String?): String {
        if (url.isNullOrEmpty()) return "Unknown Source"

        return try {
            val cleanUrl = url.removePrefix("http://").removePrefix("https://")
            val domain = cleanUrl.split("/").firstOrNull() ?: return "Unknown Source"
            val parts = domain.split(".")

            // Get main domain (before last dot)
            val mainDomain = if (parts.size >= 2) {
                parts[parts.size - 2]
            } else {
                parts.firstOrNull() ?: "Unknown"
            }

            mainDomain.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "News Source"
        }
    }

    private fun formatTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)

            val now = Date()
            val diff = now.time - (date?.time ?: 0)

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                minutes < 1 -> "just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hours ago"
                days < 7 -> "$days days ago"
                else -> {
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    outputFormat.format(date!!)
                }
            }
        } catch (e: Exception) {
            "Recently"
        }
    }
}
