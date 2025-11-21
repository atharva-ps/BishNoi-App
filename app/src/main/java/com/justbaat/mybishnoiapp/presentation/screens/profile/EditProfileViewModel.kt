package com.justbaat.mybishnoiapp.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbaat.mybishnoiapp.domain.model.*
import com.justbaat.mybishnoiapp.domain.usecase.profile.*
import com.justbaat.mybishnoiapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateBasicInfoUseCase: UpdateBasicInfoUseCase,
    private val updateSocialMediaUseCase: UpdateSocialMediaUseCase,
    private val updatePersonalDetailsUseCase: UpdatePersonalDetailsUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val updateProfessionalDetailsUseCase: UpdateProfessionalDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun loadProfile(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            getProfileUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        result.data?.let { profile ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    profile = profile,
                                    // Initialize form fields
                                    firstName = profile.firstName,
                                    lastName = profile.lastName,
                                    username = profile.username,
                                    mobileNumber = profile.mobileNumber,
                                    gender = profile.gender,
                                    dob = profile.dob,
                                    aboutMe = profile.aboutMe,
                                    socialMedia = profile.socialMedia,
                                    personalDetails = profile.personalDetails,
                                    currentAddress = profile.address.current,
                                    nativeAddress = profile.address.native,
                                    professionalDetails = profile.professionalDetails,
                                    error = null
                                )
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

    // Basic Info Updates
    fun updateFirstName(value: String) {
        _uiState.update { it.copy(firstName = value) }
    }

    fun updateLastName(value: String) {
        _uiState.update { it.copy(lastName = value) }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun updateMobileNumber(value: String) {
        _uiState.update { it.copy(mobileNumber = value) }
    }

    fun updateGender(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun updateDob(value: String) {
        _uiState.update { it.copy(dob = value) }
    }

    fun updateAboutMe(value: String) {
        _uiState.update { it.copy(aboutMe = value) }
    }

    // Save Basic Info
    fun saveBasicInfo() {
        viewModelScope.launch {
            val state = _uiState.value

            updateBasicInfoUseCase(
                firstName = state.firstName,
                lastName = state.lastName,
                username = state.username,
                mobileNumber = state.mobileNumber,
                gender = state.gender,
                dob = state.dob,
                aboutMe = state.aboutMe
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                profile = result.data,
                                saveSuccess = true,
                                successMessage = "Basic info updated successfully"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message ?: "Failed to update basic info"
                            )
                        }
                    }
                }
            }
        }
    }

    // Social Media Updates
    fun updateSocialMedia(socialMedia: SocialMedia) {
        _uiState.update { it.copy(socialMedia = socialMedia) }
    }

    fun saveSocialMedia() {
        viewModelScope.launch {
            updateSocialMediaUseCase(_uiState.value.socialMedia).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                profile = result.data,
                                saveSuccess = true,
                                successMessage = "Social media links updated successfully"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message ?: "Failed to update social media"
                            )
                        }
                    }
                }
            }
        }
    }

    // Personal Details Updates
    fun updatePersonalDetails(personalDetails: PersonalDetails) {
        _uiState.update { it.copy(personalDetails = personalDetails) }
    }

    fun savePersonalDetails() {
        viewModelScope.launch {
            updatePersonalDetailsUseCase(_uiState.value.personalDetails).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                profile = result.data,
                                saveSuccess = true,
                                successMessage = "Personal details updated successfully"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message ?: "Failed to update personal details"
                            )
                        }
                    }
                }
            }
        }
    }

    // Address Updates
    fun updateCurrentAddress(address: LocationAddress) {
        _uiState.update { it.copy(currentAddress = address) }
    }

    fun updateNativeAddress(address: LocationAddress) {
        _uiState.update { it.copy(nativeAddress = address) }
    }

    fun saveAddress() {
        viewModelScope.launch {
            val address = Address(
                current = _uiState.value.currentAddress,
                native = _uiState.value.nativeAddress
            )

            updateAddressUseCase(address).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                profile = result.data,
                                saveSuccess = true,
                                successMessage = "Address updated successfully"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message ?: "Failed to update address"
                            )
                        }
                    }
                }
            }
        }
    }

    // Professional Details Updates
    fun updateProfessionalDetails(professionalDetails: ProfessionalDetails) {
        _uiState.update { it.copy(professionalDetails = professionalDetails) }
    }

    fun saveProfessionalDetails() {
        viewModelScope.launch {
            updateProfessionalDetailsUseCase(_uiState.value.professionalDetails).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                profile = result.data,
                                saveSuccess = true,
                                successMessage = "Professional details updated successfully"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message ?: "Failed to update professional details"
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

    fun clearSuccess() {
        _uiState.update { it.copy(saveSuccess = false, successMessage = null) }
    }
}

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val profile: Profile? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val successMessage: String? = null,

    // Basic Info Fields
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val mobileNumber: String = "",
    val gender: String = "",
    val dob: String = "",
    val aboutMe: String = "",

    // Social Media
    val socialMedia: SocialMedia = SocialMedia(),

    // Personal Details
    val personalDetails: PersonalDetails = PersonalDetails(),

    // Address
    val currentAddress: LocationAddress = LocationAddress(),
    val nativeAddress: LocationAddress = LocationAddress(),

    // Professional Details
    val professionalDetails: ProfessionalDetails = ProfessionalDetails()
)
