package com.app.bishnoi.data.remote.dto

import com.app.bishnoi.domain.model.News
import java.text.SimpleDateFormat
import java.util.*

fun NewsDto.toDomain(): News {
    // ✅ Extract categories from embedded terms
    val categoryNames = embedded?.terms
        ?.flatten() // Flatten list of lists
        ?.filter { it.taxonomy == "category" } // Only categories (not news-source)
        ?.map { it.name } // Extract names
        ?: emptyList()

    // ✅ Extract news source from embedded terms
    val newsSourceName = embedded?.terms
        ?.flatten()
        ?.firstOrNull { it.taxonomy == "news-source" }
        ?.name
        ?: "Unknown"

    // ✅ Extract image URL from embedded featured media
    val imageUrl = embedded?.featuredMedia?.firstOrNull()?.sourceUrl

    // ✅ Format published time (relative time)
    val publishedTime = formatRelativeTime(date)

    return News(
        id = id,
        title = title.rendered.cleanHtml(),
        description = content.rendered.cleanHtml(),
        imageUrl = imageUrl,
        source = newsSourceName,
        externalLink = externalLink,
        publishedTime = publishedTime,
        categories = categoryNames  // ✅ Now populated correctly
    )
}

// ✅ Helper: Clean HTML tags from text
private fun String.cleanHtml(): String {
    return this
        .replace(Regex("<[^>]*>"), "") // Remove HTML tags
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .trim()
}

// ✅ Helper: Format relative time
private fun formatRelativeTime(dateString: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateString)
        val now = Date()

        if (date != null) {
            val diffInMillis = now.time - date.time
            val diffInMinutes = diffInMillis / (1000 * 60)
            val diffInHours = diffInMinutes / 60
            val diffInDays = diffInHours / 24

            when {
                diffInMinutes < 1 -> "Just now"
                diffInMinutes < 60 -> "$diffInMinutes minute${if (diffInMinutes > 1) "s" else ""} ago"
                diffInHours < 24 -> "$diffInHours hour${if (diffInHours > 1) "s" else ""} ago"
                diffInDays < 7 -> "$diffInDays day${if (diffInDays > 1) "s" else ""} ago"
                else -> {
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    displayFormat.format(date)
                }
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}
