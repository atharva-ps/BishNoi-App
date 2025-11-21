package com.justbaat.mybishnoiapp.domain.usecase.auth

import com.justbaat.mybishnoiapp.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}
