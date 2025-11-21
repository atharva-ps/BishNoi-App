package com.justbaat.mybishnoiapp.domain.usecase.auth

import com.justbaat.mybishnoiapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String) =
        authRepository.register(name, email, password)
}
