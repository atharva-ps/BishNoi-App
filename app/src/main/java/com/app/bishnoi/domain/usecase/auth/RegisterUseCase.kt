package com.app.bishnoi.domain.usecase.auth

import com.app.bishnoi.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String) =
        authRepository.register(name, email, password)
}
