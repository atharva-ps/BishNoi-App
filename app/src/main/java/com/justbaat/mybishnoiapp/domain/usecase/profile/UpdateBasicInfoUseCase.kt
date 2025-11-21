package com.justbaat.mybishnoiapp.domain.usecase.profile

import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateBasicInfoUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        firstName: String?,
        lastName: String?,
        username: String?,
        mobileNumber: String?,
        gender: String?,
        dob: String?,
        aboutMe: String?
    ) = profileRepository.updateBasicInfo(
        firstName, lastName, username, mobileNumber, gender, dob, aboutMe
    )
}
