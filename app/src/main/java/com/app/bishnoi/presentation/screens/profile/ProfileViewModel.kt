package com.app.bishnoi.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.toDomain
import com.app.bishnoi.domain.model.Post
import com.app.bishnoi.domain.model.Profile
import com.app.bishnoi.domain.usecase.profile.GetProfileUseCase
import com.app.bishnoi.domain.usecase.profile.UploadCoverPhotoUseCase
import com.app.bishnoi.domain.usecase.profile.UploadProfilePhotoUseCase
import com.app.bishnoi.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val uploadProfilePhotoUseCase: UploadProfilePhotoUseCase,
    private val uploadCoverPhotoUseCase: UploadCoverPhotoUseCase,
    private val apiService: ApiService // ✅ Add for follow operations
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            getProfileUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                profile = result.data,
                                error = null
                            )
                        }
                        // Load follow status if not own profile
                        result.data?.let { profile ->
                            if (profile.isOwnProfile == false) {
                                checkFollowStatus(userId)
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load profile"
                            )
                        }
                    }
                }
            }
        }
    }

    // ✅ Check follow status
    private fun checkFollowStatus(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getFollowStatus(userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update {
                        it.copy(isFollowing = response.body()!!.isFollowing)
                    }
                }
            } catch (e: Exception) {
                // Silent fail - not critical
            }
        }
    }

    // ✅ Follow user
    fun followUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFollowLoading = true) }

            try {
                val response = apiService.followUser(userId)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isFollowing = true,
                            isFollowLoading = false,
                            // Update follower count
                            profile = it.profile?.copy(
                                followersCount = (it.profile.followersCount) + 1
                            )
                        )
                    }
                    // ✅ Reload profile to get updated privacy permissions
                    loadProfile(userId)
                } else {
                    _uiState.update {
                        it.copy(
                            isFollowLoading = false,
                            error = "Failed to follow user"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFollowLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    // ✅ Unfollow user
    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFollowLoading = true) }

            try {
                val response = apiService.unfollowUser(userId)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isFollowing = false,
                            isFollowLoading = false,
                            // Update follower count
                            profile = it.profile?.copy(
                                followersCount = maxOf((it.profile.followersCount) - 1, 0)
                            )
                        )
                    }
                    // ✅ Reload profile to get updated privacy permissions
                    loadProfile(userId)
                } else {
                    _uiState.update {
                        it.copy(
                            isFollowLoading = false,
                            error = "Failed to unfollow user"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFollowLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun uploadProfilePhoto(file: File) {
        viewModelScope.launch {
            uploadProfilePhotoUseCase(file).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isUploadingProfilePhoto = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploadingProfilePhoto = false,
                                profile = it.profile?.copy(profilePhoto = result.data),
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploadingProfilePhoto = false,
                                error = result.message ?: "Upload failed"
                            )
                        }
                    }
                }
            }
        }
    }

    fun uploadCoverPhoto(file: File) {
        viewModelScope.launch {
            uploadCoverPhotoUseCase(file).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isUploadingCoverPhoto = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploadingCoverPhoto = false,
                                profile = it.profile?.copy(coverPhoto = result.data),
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploadingCoverPhoto = false,
                                error = result.message ?: "Upload failed"
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
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val isUploadingProfilePhoto: Boolean = false,
    val isUploadingCoverPhoto: Boolean = false,
    val isFollowing: Boolean = false, // ✅ Add follow state
    val isFollowLoading: Boolean = false, // ✅ Add follow loading
    val error: String? = null,
    val userPosts: List<Post> = emptyList(),        // ✅ Add this
    val isLoadingPosts: Boolean = false,
)
