package com.app.bishnoi.presentation.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.domain.model.News
import com.app.bishnoi.domain.repository.NewsRepository
import com.app.bishnoi.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews(refresh: Boolean = false) {
        if (refresh) {
            _uiState.update {
                it.copy(
                    currentPage = 1,
                    newsList = emptyList(),
                    filteredNewsList = emptyList(),
                    hasMorePages = true
                )
            }
        }

        if (_uiState.value.isLoading || !_uiState.value.hasMorePages) {
            return
        }

        viewModelScope.launch {
            newsRepository.getNews(_uiState.value.currentPage).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val newsList = result.data ?: emptyList()
                        val currentList = if (refresh) emptyList() else _uiState.value.newsList
                        val updatedList = currentList + newsList

                        val allCategories = extractCategories(updatedList)
                        val hasMore = newsList.size >= 20

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                newsList = updatedList,
                                filteredNewsList = filterByCategory(updatedList, it.selectedCategory),
                                allCategories = allCategories,
                                error = null,
                                isRefreshing = false,
                                hasMorePages = hasMore
                            )
                        }
                    }
                    is Resource.Error -> {
                        val isPageError = result.message?.contains("page", ignoreCase = true) == true

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                hasMorePages = !isPageError,
                                error = if (isPageError) null else result.message,
                                isRefreshing = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && _uiState.value.hasMorePages) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            loadNews()
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadNews(refresh = true)
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                filteredNewsList = filterByCategory(it.newsList, category)
            )
        }
    }

    private fun extractCategories(newsList: List<News>): List<String> {
        val categoriesSet = mutableSetOf<String>()
        newsList.forEach { news ->
            categoriesSet.addAll(news.categories)
        }
        return listOf("My Feed") + categoriesSet.sorted()
    }

    private fun filterByCategory(newsList: List<News>, category: String): List<News> {
        return if (category == "My Feed") {
            newsList
        } else {
            newsList.filter { news ->
                news.categories.any { it.equals(category, ignoreCase = true) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class NewsUiState(
    val newsList: List<News> = emptyList(),
    val filteredNewsList: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val selectedCategory: String = "My Feed",
    val allCategories: List<String> = listOf("My Feed")
)
