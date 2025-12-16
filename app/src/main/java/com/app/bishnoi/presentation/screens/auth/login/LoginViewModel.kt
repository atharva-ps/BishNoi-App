package com.app.bishnoi.presentation.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.domain.usecase.auth.LoginUseCase
import com.app.bishnoi.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun login() {
        // ✅ Updated validation to accept username or email
        if (!validateInput()) return

        viewModelScope.launch {
            loginUseCase(_uiState.value.email, _uiState.value.password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loginSuccess = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Login failed"
                            )
                        }
                    }
                }
            }
        }
    }

    // ✅ Updated validation logic
    private fun validateInput(): Boolean {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        var isValid = true

        // Check if email/username is empty
        if (email.isEmpty()) {
            _uiState.update { it.copy(emailError = "Email or username is required") }
            isValid = false
        }
        // ✅ REMOVED email format validation - accept both username and email
        // No need to validate format since we accept usernames too

        // Validate password
        if (password.isEmpty()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)
