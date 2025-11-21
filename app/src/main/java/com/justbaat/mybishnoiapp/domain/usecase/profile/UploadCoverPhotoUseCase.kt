package com.justbaat.mybishnoiapp.domain.usecase.profile

import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import java.io.File
import javax.inject.Inject

class UploadCoverPhotoUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(imageFile: File) =
        profileRepository.uploadCoverPhoto(imageFile)
}
