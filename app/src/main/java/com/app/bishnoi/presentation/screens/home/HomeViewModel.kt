package com.app.bishnoi.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.ReportRequest
import com.app.bishnoi.data.remote.dto.UserSearchDto
import com.app.bishnoi.data.remote.dto.toDomain
import com.app.bishnoi.domain.model.Post
import com.app.bishnoi.domain.model.User
import com.app.bishnoi.domain.repository.AuthRepository
import com.app.bishnoi.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.app.bishnoi.domain.model.Profile
import com.app.bishnoi.data.remote.dto.toDomainModel

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val apiService: ApiService // ✅ Add this
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadCurrentUserProfile()
        loadFeed()
    }

    private fun loadCurrentUser() {
        val currentUser = authRepository.getCurrentUser()
        _uiState.update { it.copy(currentUser = currentUser) }
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val response = apiService.getProfile(currentUser.id)
                    if (response.isSuccessful && response.body()?.success == true) {
                        _uiState.update {
                            it.copy(currentUserProfile = response.body()!!.user.toDomainModel())
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent fail - not critical for feed
                println("❌ Load profile error: ${e.message}")
            }
        }
    }

    // ✅ Search users
    fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }

            try {
                val response = apiService.searchUsers(query)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchResults = response.body()!!.users
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            error = "Search failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoggingOut = true) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                isLoggedOut = true
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                error = result.message ?: "Logout failed"
                            )
                        }
                    }
                }
            }
        }
    }


    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSearchResults() {
        _uiState.update { it.copy(searchResults = emptyList()) }
    }

    fun loadFeed(lastPostId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getFeed(lastPostId = lastPostId)
                if (response.isSuccessful && response.body()?.success == true) {

                    // ✅ map DTO -> domain here
                    val dtoPosts = response.body()!!.posts
                    val domainPosts = dtoPosts.map { it.toDomain() }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            feedPosts = if (lastPostId == null) {
                                domainPosts
                            } else {
                                it.feedPosts + domainPosts
                            }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.body()?.message ?: "Failed to load feed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unexpected error"
                    )
                }
            }
        }
    }
    fun toggleLike(post: Post) {
        viewModelScope.launch {
            try {
                val response = if (post.isLikedByCurrentUser) {
                    apiService.unlikePost(post.id)
                } else {
                    apiService.likePost(post.id)
                }

                if (response.isSuccessful) {
                    // Optimistically update UI
                    _uiState.update { state ->
                        state.copy(
                            feedPosts = state.feedPosts.map { p ->
                                if (p.id == post.id) {
                                    p.copy(
                                        isLikedByCurrentUser = !post.isLikedByCurrentUser,
                                        likesCount = if (post.isLikedByCurrentUser) {
                                            maxOf(p.likesCount - 1, 0)
                                        } else {
                                            p.likesCount + 1
                                        }
                                    )
                                } else p
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ Like toggle error: ${e.message}")
            }
        }
    }
    // In HomeViewModel.kt

    fun updatePost(updatedPost: Post) {
        _uiState.update { state ->
            state.copy(
                feedPosts = state.feedPosts.map { post ->
                    if (post.id == updatedPost.id) updatedPost else post
                }
            )
        }
    }
    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deletePost(postId)

                if (response.isSuccessful) {
                    // Remove post from feed
                    _uiState.update { state ->
                        state.copy(
                            feedPosts = state.feedPosts.filter { it.id != postId }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(error = "Failed to delete post")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Error deleting post")
                }
            }
        }
    }
    fun reportPost(
        post: Post,
        reportType: String,
        message: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = ReportRequest(
                    reportType = reportType,
                    message = message,
                    reportedPostId = post.id,
                    reportedUserId = post.userId
                )

                val response = apiService.submitReport(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Failed to submit report")
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }
}

data class HomeUiState(
    val currentUser: User? = null,
    val currentUserProfile: Profile? = null,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isSearching: Boolean = false, // ✅ Add this
    val searchResults: List<UserSearchDto> = emptyList(), // ✅ Add this
    val error: String? = null,
    val feedPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
)
