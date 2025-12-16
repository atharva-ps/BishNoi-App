package com.app.bishnoi.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val response = apiService.getSettings()
                if (response.isSuccessful && response.body() != null) {
                    val settings = response.body()!!.settings
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            settings = settings,
                            // Update individual settings
                            isProfilePublic = settings.privacy.isProfilePublic,
                            showEmail = settings.privacy.showEmail,
                            showMobile = settings.privacy.showMobile,
                            showAddress = settings.privacy.showAddress,
                            allowMessagesFromAnyone = settings.communication.allowMessagesFromAnyone,
                            showOnlineStatus = settings.communication.showOnlineStatus
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load settings"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun updatePrivacySetting(
        isProfilePublic: Boolean? = null,
        showEmail: Boolean? = null,
        showMobile: Boolean? = null,
        showAddress: Boolean? = null
    ) {
        isProfilePublic?.let { _uiState.update { state -> state.copy(isProfilePublic = it) } }
        showEmail?.let { _uiState.update { state -> state.copy(showEmail = it) } }
        showMobile?.let { _uiState.update { state -> state.copy(showMobile = it) } }
        showAddress?.let { _uiState.update { state -> state.copy(showAddress = it) } }
    }

    fun updateCommunicationSetting(
        allowMessagesFromAnyone: Boolean? = null,
        showOnlineStatus: Boolean? = null
    ) {
        allowMessagesFromAnyone?.let { _uiState.update { state -> state.copy(allowMessagesFromAnyone = it) } }
        showOnlineStatus?.let { _uiState.update { state -> state.copy(showOnlineStatus = it) } }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val state = _uiState.value
                val request = UpdateSettingsRequest(
                    privacy = PrivacySettingsDto(
                        isProfilePublic = state.isProfilePublic,
                        showEmail = state.showEmail,
                        showMobile = state.showMobile,
                        showAddress = state.showAddress
                    ),
                    communication = CommunicationSettingsDto(
                        allowMessagesFromAnyone = state.allowMessagesFromAnyone,
                        showOnlineStatus = state.showOnlineStatus
                    ),
                    notifications = state.settings?.notifications ?: NotificationSettingsDto(
                        enablePushNotifications = true,
                        enableEmailNotifications = false
                    )
                )

                val response = apiService.updateSettings(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            successMessage = "Settings saved successfully"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = response.message() ?: "Failed to save settings"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(saveSuccess = false, successMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val settings: SettingsDto? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val successMessage: String? = null,

    // Privacy settings
    val isProfilePublic: Boolean = true,
    val showEmail: Boolean = true,
    val showMobile: Boolean = false,
    val showAddress: Boolean = false,

    // Communication settings
    val allowMessagesFromAnyone: Boolean = true,
    val showOnlineStatus: Boolean = true
)
