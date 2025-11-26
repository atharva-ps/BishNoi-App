package com.justbaat.mybishnoiapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.justbaat.mybishnoiapp.domain.model.*

// Response DTO
data class ProfileResponse(
    val success: Boolean,
    val user: ProfileDto,
    val message: String? = null
)

data class ProfileDto(
    @SerializedName("_id")
    val id: String? = null,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val mobileNumber: String?,
    val gender: String?,
    val dob: String?,
    val aboutMe: String?,
    val profilePhoto: String?,
    val coverPhoto: String?,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?,

    // Privacy fields
    val isPrivate: Boolean? = null,
    val isFollowing: Boolean? = null,
    val canViewPosts: Boolean? = null,
    val isOwnProfile: Boolean? = null,

    // Nested objects
    val socialMedia: SocialMediaDto? = null,
    val personalDetails: PersonalDetailsDto? = null,
    val address: AddressDto? = null,
    val professionalDetails: ProfessionalDetailsDto? = null
)

data class SocialMediaDto(
    val instagram: String? = null,
    val facebook: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val youtube: String? = null
)

data class PersonalDetailsDto(
    val maritalStatus: String? = null,
    val relationships: List<RelationshipDto>? = null,
    val subCaste: String? = null
)

data class RelationshipDto(
    val type: String? = null,
    val name: String? = null,
    val userId: String? = null
)

data class AddressDto(
    val current: LocationAddressDto? = null,
    val native: LocationAddressDto? = null
)

data class LocationAddressDto(
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val pincode: String? = null,
    val coordinates: CoordinatesDto? = null
)

data class CoordinatesDto(
    val lat: Double? = null,
    val lng: Double? = null
)

data class ProfessionalDetailsDto(
    val education: List<EducationDto>? = null,
    val occupation: String? = null,
    val companyName: String? = null,
    val designation: String? = null,
    val industry: String? = null
)

data class EducationDto(
    val degree: String? = null,
    val institution: String? = null,
    val year: String? = null,
    val fieldOfStudy: String? = null
)

// Request DTOs
data class UpdateBasicInfoRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val mobileNumber: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val aboutMe: String? = null
)

// Photo upload response
data class PhotoUploadResponse(
    val success: Boolean,
    val message: String,
    val photoUrl: String
)

// ✅ Extension function to convert DTO to Domain model (FIXED)
fun ProfileDto.toDomainModel(): Profile {
    return Profile(
        id = id ?: "",
        email = email,
        firstName = firstName ?: "",
        lastName = lastName ?: "",
        username = username,
        mobileNumber = mobileNumber ?: "",
        gender = gender ?: "",
        dob = dob ?: "",
        profilePhoto = profilePhoto,
        aboutMe = aboutMe ?: "",
        coverPhoto = coverPhoto,
        socialMedia = socialMedia?.toDomainModel() ?: SocialMedia(),
        personalDetails = personalDetails?.toDomainModel() ?: PersonalDetails(),
        address = address?.toDomainModel() ?: Address(),
        professionalDetails = professionalDetails?.toDomainModel() ?: ProfessionalDetails(),
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount,
        isActive = isActive, // ✅ Added this
        createdAt = createdAt,
        // ✅ Privacy fields
        isPrivate = isPrivate,
        isFollowing = isFollowing,
        canViewPosts = canViewPosts,
        isOwnProfile = isOwnProfile
    )
}

fun SocialMediaDto.toDomainModel() = SocialMedia(
    instagram = instagram ?: "",
    facebook = facebook ?: "",
    twitter = twitter ?: "",
    linkedin = linkedin ?: "",
    youtube = youtube ?: ""
)

fun PersonalDetailsDto.toDomainModel() = PersonalDetails(
    maritalStatus = maritalStatus ?: "",
    relationships = relationships?.map { it.toDomainModel() } ?: emptyList(),
    subCaste = subCaste ?: ""
)

fun RelationshipDto.toDomainModel() = Relationship(
    type = type ?: "",
    name = name ?: "",
    userId = userId
)

fun AddressDto.toDomainModel() = Address(
    current = current?.toDomainModel() ?: LocationAddress(),
    native = native?.toDomainModel() ?: LocationAddress()
)

fun LocationAddressDto.toDomainModel() = LocationAddress(
    address = address ?: "",
    city = city ?: "",
    state = state ?: "",
    country = country ?: "",
    pincode = pincode ?: "",
    coordinates = coordinates?.toDomainModel() ?: Coordinates()
)

fun CoordinatesDto.toDomainModel() = Coordinates(
    lat = lat ?: 0.0,
    lng = lng ?: 0.0
)

fun ProfessionalDetailsDto.toDomainModel() = ProfessionalDetails(
    education = education?.map { it.toDomainModel() } ?: emptyList(),
    occupation = occupation ?: "",
    companyName = companyName ?: "",
    designation = designation ?: "",
    industry = industry ?: ""
)

fun EducationDto.toDomainModel() = Education(
    degree = degree ?: "",
    institution = institution ?: "",
    year = year ?: "",
    fieldOfStudy = fieldOfStudy ?: ""
)
