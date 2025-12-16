package com.app.bishnoi.data.repository

import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.*
import com.app.bishnoi.domain.model.*
import com.app.bishnoi.domain.repository.ProfileRepository
import com.app.bishnoi.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProfileRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProfileRepository {

    private val httpClient = OkHttpClient()

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

    // ✅ UPDATED: Upload profile photo with presigned URL
    override suspend fun uploadProfilePhoto(imageFile: File): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            // Step 1: Get presigned URL from backend
            val presignedResponse = apiService.getPresignedUrlProfile(imageFile.name)

            if (!presignedResponse.isSuccessful || presignedResponse.body() == null) {
                emit(Resource.Error("Failed to get upload URL"))
                return@flow
            }

            val presignedData = presignedResponse.body()!!
            val uploadUrl = presignedData.uploadUrl
            val photoUrl = presignedData.fileUrl

            // Step 2: Upload image directly to S3
            val contentType = when (imageFile.extension.lowercase()) {
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "heic", "heif" -> "image/heic"
                else -> "image/jpeg"
            }

            val imageRequestBody = imageFile.asRequestBody(contentType.toMediaTypeOrNull())
            val s3Request = Request.Builder()
                .url(uploadUrl)
                .put(imageRequestBody)
                .build()

            val s3Response = withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    httpClient.newCall(s3Request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                            continuation.resumeWithException(e)
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            continuation.resume(response)
                        }
                    })
                }
            }

            if (!s3Response.isSuccessful) {
                emit(Resource.Error("Failed to upload photo to S3: ${s3Response.code}"))
                return@flow
            }

            // Step 3: Update backend with photo URL
            val response = apiService.uploadProfilePhotoWithUrl(photoUrl)

            if (response.isSuccessful && response.body() != null) {
                val finalPhotoUrl = response.body()!!.photoUrl
                emit(Resource.Success(finalPhotoUrl))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to update profile photo"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred"))
        }
    }

    // ✅ UPDATED: Upload cover photo with presigned URL
    override suspend fun uploadCoverPhoto(imageFile: File): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            // Step 1: Get presigned URL from backend
            val presignedResponse = apiService.getPresignedUrlCover(imageFile.name)

            if (!presignedResponse.isSuccessful || presignedResponse.body() == null) {
                emit(Resource.Error("Failed to get upload URL"))
                return@flow
            }

            val presignedData = presignedResponse.body()!!
            val uploadUrl = presignedData.uploadUrl
            val photoUrl = presignedData.fileUrl

            // Step 2: Upload image directly to S3
            val contentType = when (imageFile.extension.lowercase()) {
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "heic", "heif" -> "image/heic"
                else -> "image/jpeg"
            }

            val imageRequestBody = imageFile.asRequestBody(contentType.toMediaTypeOrNull())
            val s3Request = Request.Builder()
                .url(uploadUrl)
                .put(imageRequestBody)
                .build()

            val s3Response = withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    httpClient.newCall(s3Request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                            continuation.resumeWithException(e)
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            continuation.resume(response)
                        }
                    })
                }
            }

            if (!s3Response.isSuccessful) {
                emit(Resource.Error("Failed to upload photo to S3: ${s3Response.code}"))
                return@flow
            }

            // Step 3: Update backend with photo URL
            val response = apiService.uploadCoverPhotoWithUrl(photoUrl)

            if (response.isSuccessful && response.body() != null) {
                val finalPhotoUrl = response.body()!!.photoUrl
                emit(Resource.Success(finalPhotoUrl))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to update cover photo"))
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
