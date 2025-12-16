package com.app.bishnoi.domain.usecase.auth

import com.app.bishnoi.utils.Constants
import com.app.bishnoi.utils.isValidPassword
import javax.inject.Inject

class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password cannot be empty"
            )
        }
        if (!password.isValidPassword()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters"
            )
        }
        return ValidationResult(successful = true)
    }
}
