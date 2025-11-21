package com.justbaat.mybishnoiapp.data.remote.api

import com.justbaat.mybishnoiapp.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== Authentication ====================
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    // ✅ NEW: Get email by username
    @POST("api/auth/get-email")
    suspend fun getEmailByUsername(@Body request: GetEmailRequest): Response<GetEmailResponse>

    // ==================== Profile ====================

    // Get user profile
    @GET("api/profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileResponse>

    // Update basic info
    @PUT("api/profile/basic-info")
    suspend fun updateBasicInfo(@Body request: UpdateBasicInfoRequest): Response<ProfileResponse>

    // Upload profile photo
    @Multipart
    @POST("api/profile/profile-photo")
    suspend fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part
    ): Response<PhotoUploadResponse>

    // Upload cover photo
    @Multipart
    @POST("api/profile/cover-photo")
    suspend fun uploadCoverPhoto(
        @Part photo: MultipartBody.Part
    ): Response<PhotoUploadResponse>

    // Update social media
    @PUT("api/profile/social-media")
    suspend fun updateSocialMedia(@Body socialMedia: SocialMediaDto): Response<ProfileResponse>

    // Update personal details
    @PUT("api/profile/personal-details")
    suspend fun updatePersonalDetails(@Body personalDetails: PersonalDetailsDto): Response<ProfileResponse>

    // Update address
    @PUT("api/profile/address")
    suspend fun updateAddress(@Body address: AddressDto): Response<ProfileResponse>

    // Update professional details
    @PUT("api/profile/professional-details")
    suspend fun updateProfessionalDetails(@Body professionalDetails: ProfessionalDetailsDto): Response<ProfileResponse>

    // ==================== Search ====================
    @GET("api/search/users")
    suspend fun searchUsers(@Query("query") query: String): Response<SearchUsersResponse>

    @GET("api/search/recent")
    suspend fun getRecentUsers(@Query("limit") limit: Int = 10): Response<SearchUsersResponse>

    // ==================== Settings ====================
    @GET("api/settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PUT("api/settings/privacy")
    suspend fun updatePrivacySettings(@Body request: UpdatePrivacyRequest): Response<SettingsResponse>

    @PUT("api/settings/communication")
    suspend fun updateCommunicationSettings(@Body request: UpdateCommunicationRequest): Response<SettingsResponse>

    @PUT("api/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): Response<SettingsResponse>


}
