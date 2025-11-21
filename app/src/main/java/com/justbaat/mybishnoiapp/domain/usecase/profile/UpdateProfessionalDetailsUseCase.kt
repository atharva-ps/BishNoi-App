package com.justbaat.mybishnoiapp.domain.usecase.profile

import com.justbaat.mybishnoiapp.domain.model.ProfessionalDetails
import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfessionalDetailsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(professionalDetails: ProfessionalDetails) =
        profileRepository.updateProfessionalDetails(professionalDetails)
}
