package com.justbaat.mybishnoiapp.domain.usecase.auth

import com.justbaat.mybishnoiapp.utils.Constants
import com.justbaat.mybishnoiapp.utils.isValidPassword
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
