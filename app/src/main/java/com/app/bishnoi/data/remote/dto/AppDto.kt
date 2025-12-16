package com.app.bishnoi.data.remote.dto

data class VersionCheckResponse(
    val success: Boolean,
    val needsUpdate: Boolean,
    val forceUpdate: Boolean,
    val latestVersion: String,
    val minimumVersion: String,
    val updateMessage: String,
    val playStoreUrl: String
)

data class AppConfigResponse(
    val success: Boolean,
    val version: VersionInfo,
    val serverTime: String
)

data class VersionInfo(
    val latestVersion: String,
    val minimumVersion: String,
    val forceUpdate: Boolean,
    val updateMessage: String,
    val playStoreUrl: String
)
