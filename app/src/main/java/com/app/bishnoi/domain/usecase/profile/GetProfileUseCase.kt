package com.app.bishnoi.domain.usecase.profile

import com.app.bishnoi.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String) = profileRepository.getProfile(userId)
}
