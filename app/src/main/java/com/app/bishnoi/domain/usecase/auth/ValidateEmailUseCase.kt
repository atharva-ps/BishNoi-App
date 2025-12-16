package com.app.bishnoi.domain.usecase.auth

import com.app.bishnoi.utils.isValidEmail
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor() {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Email cannot be empty"
            )
        }
        if (!email.isValidEmail()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Invalid email format"
            )
        }
        return ValidationResult(successful = true)
    }
}

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)
