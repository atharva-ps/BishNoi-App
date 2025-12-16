package com.app.bishnoi.domain.repository

import com.app.bishnoi.domain.model.*
import com.app.bishnoi.utils.Resource
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ProfileRepository {

    // Get profile
    suspend fun getProfile(userId: String): Flow<Resource<Profile>>

    // Update basic info
    suspend fun updateBasicInfo(
        firstName: String?,
        lastName: String?,
        username: String?,
        mobileNumber: String?,
        gender: String?,
        dob: String?,
        aboutMe: String?
    ): Flow<Resource<Profile>>

    // Upload photos
    suspend fun uploadProfilePhoto(imageFile: File): Flow<Resource<String>>
    suspend fun uploadCoverPhoto(imageFile: File): Flow<Resource<String>>

    // Update sections
    suspend fun updateSocialMedia(socialMedia: SocialMedia): Flow<Resource<Profile>>
    suspend fun updatePersonalDetails(personalDetails: PersonalDetails): Flow<Resource<Profile>>
    suspend fun updateAddress(address: Address): Flow<Resource<Profile>>
    suspend fun updateProfessionalDetails(professionalDetails: ProfessionalDetails): Flow<Resource<Profile>>
}
