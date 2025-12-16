package com.app.bishnoi.domain.model

data class Profile(
    val id: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
    val username: String,
    val mobileNumber: String,
    val gender: String,
    val dob: String,
    val profilePhoto: String?,
    val aboutMe: String,
    val coverPhoto: String?,
    val socialMedia: SocialMedia,
    val personalDetails: PersonalDetails,
    val address: Address,
    val professionalDetails: ProfessionalDetails,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val isActive: Boolean, // ✅ Added
    val createdAt: String?,

    // ✅ Privacy fields
    val isPrivate: Boolean? = null,
    val isFollowing: Boolean? = null,
    val canViewPosts: Boolean? = null,
    val isOwnProfile: Boolean? = null
)

data class SocialMedia(
    val instagram: String = "",
    val facebook: String = "",
    val twitter: String = "",
    val linkedin: String = "",
    val youtube: String = ""
)

data class PersonalDetails(
    val maritalStatus: String = "",
    val relationships: List<Relationship> = emptyList(),
    val subCaste: String = ""
)

data class Relationship(
    val type: String,
    val name: String,
    val userId: String? = null
)

data class Address(
    val current: LocationAddress = LocationAddress(),
    val native: LocationAddress = LocationAddress()
)

data class LocationAddress(
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val pincode: String = "",
    val coordinates: Coordinates = Coordinates()
)

data class Coordinates(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class ProfessionalDetails(
    val education: List<Education> = emptyList(),
    val occupation: String = "",
    val companyName: String = "",
    val designation: String = "",
    val industry: String = ""
)

data class Education(
    val degree: String,
    val institution: String,
    val year: String,
    val fieldOfStudy: String
)
