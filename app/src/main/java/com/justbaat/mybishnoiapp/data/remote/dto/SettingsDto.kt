package com.justbaat.mybishnoiapp.data.remote.dto

data class SettingsResponse(
    val success: Boolean,
    val settings: SettingsDto,
    val message: String? = null
)

data class SettingsDto(
    val privacy: PrivacySettingsDto,
    val communication: CommunicationSettingsDto,
    val notifications: NotificationSettingsDto
)

data class PrivacySettingsDto(
    val isProfilePublic: Boolean,
    val showEmail: Boolean,
    val showMobile: Boolean,
    val showAddress: Boolean
)

data class CommunicationSettingsDto(
    val allowMessagesFromAnyone: Boolean,
    val showOnlineStatus: Boolean
)

data class NotificationSettingsDto(
    val enablePushNotifications: Boolean,
    val enableEmailNotifications: Boolean
)

// Request DTOs
data class UpdatePrivacyRequest(
    val isProfilePublic: Boolean,
    val showEmail: Boolean,
    val showMobile: Boolean,
    val showAddress: Boolean
)

data class UpdateCommunicationRequest(
    val allowMessagesFromAnyone: Boolean,
    val showOnlineStatus: Boolean
)

data class UpdateSettingsRequest(
    val privacy: PrivacySettingsDto,
    val communication: CommunicationSettingsDto,
    val notifications: NotificationSettingsDto
)
