package com.app.bishnoi.presentation.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.ReportRequest
import com.app.bishnoi.data.remote.dto.toDomain
import com.app.bishnoi.data.remote.dto.toDomainModel
import com.app.bishnoi.domain.model.Post
import com.app.bishnoi.domain.model.Profile
import com.app.bishnoi.domain.model.User
import com.app.bishnoi.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadCurrentUserProfile()
        loadSocialFeed()
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

    fun loadSocialFeed(lastPostId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getSocialFeed(lastPostId = lastPostId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val dtoPosts = response.body()!!.posts
                    val domainPosts = dtoPosts.map { it.toDomain() }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            socialPosts = if (lastPostId == null) {
                                domainPosts
                            } else {
                                it.socialPosts + domainPosts
                            }
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.body()?.message ?: "Failed to load social feed"
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
                    _uiState.update { state ->
                        state.copy(
                            socialPosts = state.socialPosts.map { p ->
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

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deletePost(postId)

                if (response.isSuccessful) {
                    _uiState.update { state ->
                        state.copy(
                            socialPosts = state.socialPosts.filter { it.id != postId }
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SocialUiState(
    val currentUser: User? = null,
    val currentUserProfile: Profile? = null,
    val socialPosts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
