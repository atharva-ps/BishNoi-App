package com.app.bishnoi.domain.usecase.profile

import com.app.bishnoi.domain.model.PersonalDetails
import com.app.bishnoi.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdatePersonalDetailsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(personalDetails: PersonalDetails) =
        profileRepository.updatePersonalDetails(personalDetails)
}
