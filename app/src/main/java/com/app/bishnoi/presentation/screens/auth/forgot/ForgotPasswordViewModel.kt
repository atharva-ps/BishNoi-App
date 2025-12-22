package com.app.bishnoi.presentation.screens.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.domain.repository.AuthRepository
import com.app.bishnoi.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, identifierError = null) }
    }

    fun sendResetLink() {
        val id = _uiState.value.identifier.trim()
        if (id.isEmpty()) {
            _uiState.update { it.copy(identifierError = "Email or username is required") }
            return
        }

        viewModelScope.launch {
            authRepository.sendPasswordReset(id).collect { result ->
                when (result) {
                    is Resource.Loading ->
                        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
                    is Resource.Success ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Password reset email sent. Check your inbox.",
                                error = null
                            )
                        }
                    is Resource.Error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to send reset link"
                            )
                        }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

data class ForgotPasswordUiState(
    val identifier: String = "",
    val identifierError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
