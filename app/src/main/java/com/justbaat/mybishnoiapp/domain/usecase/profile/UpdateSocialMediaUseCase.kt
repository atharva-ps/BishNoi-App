package com.justbaat.mybishnoiapp.domain.usecase.profile

import com.justbaat.mybishnoiapp.domain.model.SocialMedia
import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateSocialMediaUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(socialMedia: SocialMedia) =
        profileRepository.updateSocialMedia(socialMedia)
}
