package com.justbaat.mybishnoiapp.presentation.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbaat.mybishnoiapp.domain.model.Profile
import com.justbaat.mybishnoiapp.domain.usecase.profile.GetProfileUseCase
import com.justbaat.mybishnoiapp.domain.usecase.profile.UpdateBasicInfoUseCase
import com.justbaat.mybishnoiapp.domain.usecase.profile.UploadCoverPhotoUseCase
import com.justbaat.mybishnoiapp.domain.usecase.profile.UploadProfilePhotoUseCase
import com.justbaat.mybishnoiapp.utils.Resource
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
    private val updateBasicInfoUseCase: UpdateBasicInfoUseCase,
    private val uploadProfilePhotoUseCase: UploadProfilePhotoUseCase,
    private val uploadCoverPhotoUseCase: UploadCoverPhotoUseCase
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

    fun uploadProfilePhoto(imageFile: File) {
        viewModelScope.launch {
            uploadProfilePhotoUseCase(imageFile).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isUploadingProfilePhoto = true) }
                    }
                    is Resource.Success -> {
                        // ✅ Update profile photo URL immediately
                        _uiState.update {
                            it.copy(
                                isUploadingProfilePhoto = false,
                                profile = it.profile?.copy(profilePhoto = result.data),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploadingProfilePhoto = false,
                                error = result.message ?: "Failed to upload photo"
                            )
                        }
                    }
                }
            }
        }
    }

    fun uploadCoverPhoto(imageFile: File) {
        viewModelScope.launch {
            uploadCoverPhotoUseCase(imageFile).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isUploadingCoverPhoto = true) }
                    }
                    is Resource.Success -> {
                        // ✅ Update cover photo URL immediately
                        _uiState.update {
                            it.copy(
                                isUploadingCoverPhoto = false,
                                profile = it.profile?.copy(coverPhoto = result.data),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploadingCoverPhoto = false,
                                error = result.message ?: "Failed to upload cover photo"
                            )
                        }
                    }
                }
            }
        }
    }


    fun updateBasicInfo(
        firstName: String,
        lastName: String,
        username: String,
        mobileNumber: String,
        gender: String,
        dob: String,
        aboutMe: String
    ) {
        viewModelScope.launch {
            updateBasicInfoUseCase(
                firstName = firstName,
                lastName = lastName,
                username = username,
                mobileNumber = mobileNumber,
                gender = gender,
                dob = dob,
                aboutMe = aboutMe
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isUpdating = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                profile = result.data,
                                updateSuccess = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                error = result.message ?: "Failed to update profile"
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

    fun clearUpdateSuccess() {
        _uiState.update { it.copy(updateSuccess = false) }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isUploadingProfilePhoto: Boolean = false,
    val isUploadingCoverPhoto: Boolean = false,
    val profile: Profile? = null,
    val error: String? = null,
    val updateSuccess: Boolean = false
)
