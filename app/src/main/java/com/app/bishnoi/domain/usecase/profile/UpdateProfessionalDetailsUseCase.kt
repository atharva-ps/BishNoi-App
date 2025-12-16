package com.app.bishnoi.domain.usecase.profile

import com.app.bishnoi.domain.model.ProfessionalDetails
import com.app.bishnoi.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfessionalDetailsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(professionalDetails: ProfessionalDetails) =
        profileRepository.updateProfessionalDetails(professionalDetails)
}
