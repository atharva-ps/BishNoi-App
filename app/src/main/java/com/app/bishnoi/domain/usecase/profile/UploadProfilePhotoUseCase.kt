package com.app.bishnoi.domain.usecase.profile

import com.app.bishnoi.domain.repository.ProfileRepository
import java.io.File
import javax.inject.Inject

class UploadProfilePhotoUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(imageFile: File) =
        profileRepository.uploadProfilePhoto(imageFile)
}
