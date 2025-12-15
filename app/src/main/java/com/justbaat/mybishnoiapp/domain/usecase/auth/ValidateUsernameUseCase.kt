package com.justbaat.mybishnoiapp.domain.usecase.auth

import com.justbaat.mybishnoiapp.utils.isValidUsername
import javax.inject.Inject

class ValidateUsernameUseCase @Inject constructor() {
    operator fun invoke(username: String): ValidationResult {
        when {
            username.isBlank() -> {
                return ValidationResult(false, "Username cannot be empty")
            }
            username.length < 3 -> {
                return ValidationResult(false, "Username must be at least 3 characters")
            }
            username.length > 16 -> {
                return ValidationResult(false, "Username must be at most 16 characters")
            }
            username.first().isDigit() -> {
                return ValidationResult(false, "Username cannot start with a number")
            }
            !username.first().isLetter() || !username.first().isLowerCase() -> {
                return ValidationResult(false, "Username must start with a lowercase letter")
            }
            !username.isValidUsername() -> {
                return ValidationResult(false, "Username can only contain lowercase letters, numbers, and underscores")
            }
        }
        return ValidationResult(true)
    }
}