package com.justbaat.mybishnoiapp.domain.usecase.profile

import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String) = profileRepository.getProfile(userId)
}
