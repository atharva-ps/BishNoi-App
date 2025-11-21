package com.justbaat.mybishnoiapp.domain.usecase.profile

import com.justbaat.mybishnoiapp.domain.model.PersonalDetails
import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdatePersonalDetailsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(personalDetails: PersonalDetails) =
        profileRepository.updatePersonalDetails(personalDetails)
}
