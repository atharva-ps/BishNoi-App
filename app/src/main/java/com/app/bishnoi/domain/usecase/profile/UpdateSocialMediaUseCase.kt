package com.app.bishnoi.domain.usecase.profile

import com.app.bishnoi.domain.model.SocialMedia
import com.app.bishnoi.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateSocialMediaUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(socialMedia: SocialMedia) =
        profileRepository.updateSocialMedia(socialMedia)
}
