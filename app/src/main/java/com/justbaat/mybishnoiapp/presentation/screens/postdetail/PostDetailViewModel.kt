package com.justbaat.mybishnoiapp.presentation.screens.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbaat.mybishnoiapp.data.remote.api.ApiService
import com.justbaat.mybishnoiapp.data.remote.dto.AddCommentRequest
import com.justbaat.mybishnoiapp.data.remote.dto.toDomain
import com.justbaat.mybishnoiapp.domain.model.Comment
import com.justbaat.mybishnoiapp.domain.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun setPost(post: Post) {
        _uiState.update { it.copy(post = post) }
        loadComments(post.id)
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) }

            try {
                val response = apiService.getComments(postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val comments = response.body()!!.comments.map { it.toDomain() }
                    _uiState.update {
                        it.copy(
                            comments = comments,
                            isLoadingComments = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingComments = false,
                            error = "Failed to load comments"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingComments = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun addComment(postId: String, text: String) {
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingComment = true) }

            try {
                val response = apiService.addComment(
                    postId = postId,
                    request = AddCommentRequest(text = text.trim())
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val newComment = response.body()!!.comment.toDomain()

                    _uiState.update {
                        it.copy(
                            comments = listOf(newComment) + it.comments,
                            commentText = "",
                            isAddingComment = false,
                            post = it.post?.copy(
                                commentsCount = it.post.commentsCount + 1
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAddingComment = false,
                            error = "Failed to add comment"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingComment = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun toggleLike() {
        val post = _uiState.value.post ?: return

        viewModelScope.launch {
            try {
                val response = if (post.isLikedByCurrentUser) {
                    apiService.unlikePost(post.id)
                } else {
                    apiService.likePost(post.id)
                }

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            post = post.copy(
                                isLikedByCurrentUser = !post.isLikedByCurrentUser,
                                likesCount = if (post.isLikedByCurrentUser) {
                                    maxOf(post.likesCount - 1, 0)
                                } else {
                                    post.likesCount + 1
                                }
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                println("❌ Like toggle error: ${e.message}")
            }
        }
    }

    fun setCommentText(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val commentText: String = "",
    val isLoadingComments: Boolean = false,
    val isAddingComment: Boolean = false,
    val error: String? = null
)
