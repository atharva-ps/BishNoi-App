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
            _uiState.update { it.copy(currentPage = 1, newsList = emptyList(), hasMorePages = true) }
        }

        // ✅ Don't load if already loading or no more pages
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
                        // ✅ Check if we got fewer items than requested (no more pages)
                        val hasMore = newsList.size >= 20  // 20 is perPage from API call
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                newsList = currentList + newsList,
                                error = null,
                                isRefreshing = false,
                                hasMorePages = hasMore  // ✅ Update pagination state
                            )
                        }
                    }
                    is Resource.Error -> {
                        // ✅ Check if error is "no more pages" - don't show popup
                        val isPageError = result.message?.contains("page_number", ignoreCase = true) == true ||
                                result.message?.contains("page number", ignoreCase = true) == true

                        if (isPageError) {
                            // Just stop pagination, don't show error
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    hasMorePages = false,
                                    isRefreshing = false
                                )
                            }
                        } else {
                            // Show other errors
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message,
                                    isRefreshing = false
                                )
                            }
                        }
                        }
                }
            }
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && _uiState.value.hasMorePages) {  // ✅ Check hasMorePages
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            loadNews()
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadNews(refresh = true)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class NewsUiState(
    val newsList: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true  // ✅ NEW: Track if more pages exist
)
