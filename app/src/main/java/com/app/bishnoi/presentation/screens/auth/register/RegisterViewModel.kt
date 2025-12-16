package com.app.bishnoi.presentation.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.domain.model.User
import com.app.bishnoi.domain.usecase.auth.RegisterUseCase
import com.app.bishnoi.domain.usecase.auth.ValidateEmailUseCase
import com.app.bishnoi.domain.usecase.auth.ValidatePasswordUseCase
import com.app.bishnoi.domain.usecase.auth.ValidateUsernameUseCase
import com.app.bishnoi.domain.usecase.auth.ValidationResult
import com.app.bishnoi.utils.Resource
import com.app.bishnoi.utils.isValidName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.NameChanged -> {
                _uiState.update { it.copy(name = event.name, nameError = null) }
            }
            is RegisterEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, emailError = null) }
            }
            is RegisterEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password, passwordError = null) }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.update {
                    it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null)
                }
            }
            is RegisterEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            RegisterEvent.Register -> {
                register()
            }
            RegisterEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Name cannot be empty")
        }
        if (!name.isValidName()) {
            return ValidationResult(false, "Name is too short")
        }
        return ValidationResult(true)
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(false, "Please confirm password")
        }
        if (password != confirmPassword) {
            return ValidationResult(false, "Passwords do not match")
        }
        return ValidationResult(true)
    }

    private fun register() {
        val state = _uiState.value

        // Validate all inputs
        val usernameValidation = validateUsernameUseCase(state.name)
//        val nameValidation = validateName(state.name)
        val emailValidation = validateEmailUseCase(state.email)
        val passwordValidation = validatePasswordUseCase(state.password)
        val confirmPasswordValidation = validateConfirmPassword(state.password, state.confirmPassword)

        val hasError = listOf(
//            nameValidation,
            usernameValidation,
            emailValidation,
            passwordValidation,
            confirmPasswordValidation
        ).any { !it.successful }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nameError = usernameValidation.errorMessage,
                    emailError = emailValidation.errorMessage,
                    passwordError = passwordValidation.errorMessage,
                    confirmPasswordError = confirmPasswordValidation.errorMessage
                )
            }
            return
        }

        // Proceed with registration
        viewModelScope.launch {
            registerUseCase(
                name = state.name,
                email = state.email,
                password = state.password
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRegistrationSuccessful = true,
                                user = result.data
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Registration failed"
                            )
                        }
                    }
                }
            }
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val error: String? = null,
    val user: User? = null
)

sealed class RegisterEvent {
    data class NameChanged(val name: String) : RegisterEvent()
    data class EmailChanged(val email: String) : RegisterEvent()
    data class PasswordChanged(val password: String) : RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent()
    object TogglePasswordVisibility : RegisterEvent()
    object Register : RegisterEvent()
    object ClearError : RegisterEvent()
}
