package com.justbaat.mybishnoiapp.data.remote.api

import com.justbaat.mybishnoiapp.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import okhttp3.RequestBody
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

    // ==================== Follow System ====================
    @POST("api/follow/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): Response<FollowResponse>

    @HTTP(method = "DELETE", path = "api/follow/{userId}/unfollow", hasBody = false)
    suspend fun unfollowUser(@Path("userId") userId: String): Response<FollowResponse>

    @GET("api/follow/{userId}/followers")
    suspend fun getFollowers(@Path("userId") userId: String): Response<FollowersResponse>

    @GET("api/follow/{userId}/following")
    suspend fun getFollowing(@Path("userId") userId: String): Response<FollowingResponse>

    @GET("api/follow/{userId}/status")
    suspend fun getFollowStatus(@Path("userId") userId: String): Response<FollowStatusResponse>

    // ==================== Posts ====================

    // Multipart create post: image + caption + visibility

    @GET("api/posts/presigned-url")
    suspend fun getPresignedUrl(
        @Query("filename") filename: String
    ): Response<PresignedUrlResponse>

    @FormUrlEncoded
    @POST("api/posts/create-with-url")
    suspend fun createPostWithUrl(
        @Field("imageUrl") imageUrl: String,
        @Field("caption") caption: String,
        @Field("visibility") visibility: String
    ): Response<CreatePostResponse>

    @Multipart
    @POST("api/posts")
    suspend fun createPost(
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody,
        @Part("visibility") visibility: RequestBody
    ): retrofit2.Response<PostResponse>

    // Get home feed
    @GET("api/posts/feed")
    suspend fun getFeed(
        @Query("lastPostId") lastPostId: String? = null,
        @Query("limit") limit: Int = 10
    ): retrofit2.Response<FeedResponse>

    // Get posts for profile (will use later)
    @GET("api/posts/user/{userId}")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
        @Query("lastPostId") lastPostId: String? = null,
        @Query("limit") limit: Int = 12
    ): Response<FeedResponse>

    // ==================== Post Interactions ====================

    @POST("api/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "api/posts/{postId}/unlike", hasBody = false)
    suspend fun unlikePost(@Path("postId") postId: String): Response<MessageResponse>

    // ==================== Comments ====================

    @POST("api/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body request: AddCommentRequest
    ): Response<CommentResponse>

    @GET("api/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Query("limit") limit: Int = 20
    ): Response<CommentsListResponse>

    @HTTP(method = "DELETE", path = "api/posts/{postId}/comments/{commentId}", hasBody = false)
    suspend fun deleteComment(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String
    ): Response<MessageResponse>


    // ==================== Delete ====================

    @HTTP(method = "DELETE", path = "api/posts/{postId}", hasBody = false)
    suspend fun deletePost(@Path("postId") postId: String): Response<MessageResponse>

    // ==================== Members ====================

    @GET("api/members")
    suspend fun getAllMembers(
        @Query("search") search: String = "",
        @Query("state") state: String = "",
        @Query("limit") limit: Int = 50,
        @Query("lastUserId") lastUserId: String = ""
    ): Response<MembersResponse>

    @GET("api/members/states")
    suspend fun getAllStates(): Response<StatesResponse>

}
