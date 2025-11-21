package com.justbaat.mybishnoiapp.data.repository

import com.justbaat.mybishnoiapp.data.remote.api.ApiService
import com.justbaat.mybishnoiapp.data.remote.dto.*
import com.justbaat.mybishnoiapp.domain.model.*
import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import com.justbaat.mybishnoiapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProfileRepository {

    override suspend fun getProfile(userId: String): Flow<Resource<Profile>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getProfile(userId)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.user.toDomainModel()
                emit(Resource.Success(profile))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch profile"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    override suspend fun updateBasicInfo(
        firstName: String?,
        lastName: String?,
        username: String?,
        mobileNumber: String?,
        gender: String?,
        dob: String?,
        aboutMe: String?
    ): Flow<Resource<Profile>> = flow {
        try {
            emit(Resource.Loading())

            val request = UpdateBasicInfoRequest(
                firstName = firstName,
                lastName = lastName,
                username = username,
                mobileNumber = mobileNumber,
                gender = gender,
                dob = dob,
                aboutMe = aboutMe
            )

            val response = apiService.updateBasicInfo(request)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.user.toDomainModel()
                emit(Resource.Success(profile))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update profile"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    override suspend fun uploadProfilePhoto(imageFile: File): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            // Create request body
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", imageFile.name, requestFile)

            val response = apiService.uploadProfilePhoto(photoPart)

            if (response.isSuccessful && response.body() != null) {
                val photoUrl = response.body()!!.photoUrl
                emit(Resource.Success(photoUrl))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to upload photo"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    override suspend fun uploadCoverPhoto(imageFile: File): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", imageFile.name, requestFile)

            val response = apiService.uploadCoverPhoto(photoPart)

            if (response.isSuccessful && response.body() != null) {
                val photoUrl = response.body()!!.photoUrl
                emit(Resource.Success(photoUrl))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to upload cover photo"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    override suspend fun updateSocialMedia(socialMedia: SocialMedia): Flow<Resource<Profile>> = flow {
        try {
            emit(Resource.Loading())

            val dto = SocialMediaDto(
                instagram = socialMedia.instagram,
                facebook = socialMedia.facebook,
                twitter = socialMedia.twitter,
                linkedin = socialMedia.linkedin,
                youtube = socialMedia.youtube
            )

            val response = apiService.updateSocialMedia(dto)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.user.toDomainModel()
                emit(Resource.Success(profile))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update social media"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    override suspend fun updatePersonalDetails(
        personalDetails: PersonalDetails
    ): Flow<Resource<Profile>> = flow {
        try {
            emit(Resource.Loading())

            val dto = PersonalDetailsDto(
                maritalStatus = personalDetails.maritalStatus,
                relationships = personalDetails.relationships.map {
                    RelationshipDto(
                        type = it.type,
                        name = it.name,
                        userId = it.userId
                    )
                },
                subCaste = personalDetails.subCaste
            )

            val response = apiService.updatePersonalDetails(dto)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.user.toDomainModel()
                emit(Resource.Success(profile))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update personal details"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    override suspend fun updateAddress(address: Address): Flow<Resource<Profile>> = flow {
        try {
            emit(Resource.Loading())

            val dto = AddressDto(
                current = LocationAddressDto(
                    address = address.current.address,
                    city = address.current.city,
                    state = address.current.state,
                    country = address.current.country,
                    pincode = address.current.pincode,
                    coordinates = CoordinatesDto(
                        lat = address.current.coordinates.lat,
                        lng = address.current.coordinates.lng
                    )
                ),
                native = LocationAddressDto(
                    address = address.native.address,
                    city = address.native.city,
                    state = address.native.state,
                    country = address.native.country,
                    pincode = address.native.pincode,
                    coordinates = CoordinatesDto(
                        lat = address.native.coordinates.lat,
                        lng = address.native.coordinates.lng
                    )
                )
            )

            val response = apiService.updateAddress(dto)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.user.toDomainModel()
                emit(Resource.Success(profile))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update address"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    override suspend fun updateProfessionalDetails(
        professionalDetails: ProfessionalDetails
    ): Flow<Resource<Profile>> = flow {
        try {
            emit(Resource.Loading())

            val dto = ProfessionalDetailsDto(
                education = professionalDetails.education.map {
                    EducationDto(
                        degree = it.degree,
                        institution = it.institution,
                        year = it.year,
                        fieldOfStudy = it.fieldOfStudy
                    )
                },
                occupation = professionalDetails.occupation,
                companyName = professionalDetails.companyName,
                designation = professionalDetails.designation,
                industry = professionalDetails.industry
            )

            val response = apiService.updateProfessionalDetails(dto)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!.user.toDomainModel()
                emit(Resource.Success(profile))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update professional details"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }
}
