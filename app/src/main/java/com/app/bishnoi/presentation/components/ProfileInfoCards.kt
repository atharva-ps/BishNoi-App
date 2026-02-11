package com.app.bishnoi.presentation.components

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.app.bishnoi.domain.model.Profile
import com.justbaat.ads.sdk.AdSdkManager

@Composable
fun ProfileInfoSection(profile: Profile) {
    val context = LocalContext.current
    val activity = context as? Activity
    val cardBlocks = mutableListOf<@Composable () -> Unit>()

    // Basic Info Card
    if (profile.gender.isNotEmpty() ||
        profile.personalDetails.maritalStatus.isNotEmpty() ||
        profile.dob.isNotEmpty()
    ) {
        cardBlocks.add {
            InfoCard(
                title = "BASIC INFO",
                icon = Icons.Default.Person,
                backgroundColor = Color(0xFFB8E5F0),
                iconColor = Color(0xFF4A90A4)
            ) {
                if (profile.gender.isNotEmpty()) {
                    InfoRow("Gender:", profile.gender)
                }
                if (profile.personalDetails.maritalStatus.isNotEmpty()) {
                    InfoRow("Marital Status:", profile.personalDetails.maritalStatus)
                }
                if (profile.dob.isNotEmpty()) {
                    InfoRow("Date of Birth:", profile.dob)
                }
            }
        }
    }

    // Profession Card
    if (profile.professionalDetails.occupation.isNotEmpty() ||
        profile.professionalDetails.companyName.isNotEmpty()
    ) {
        cardBlocks.add {
            InfoCard(
                title = "PROFESSION",
                icon = Icons.Default.Work,
                backgroundColor = Color(0xFFD4ECD4),
                iconColor = Color(0xFF5A8A5A)
            ) {
                if (profile.professionalDetails.occupation.isNotEmpty()) {
                    Text(
                        text = profile.professionalDetails.occupation,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (profile.professionalDetails.designation.isNotEmpty()) {
                    Text(
                        text = profile.professionalDetails.designation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                if (profile.professionalDetails.companyName.isNotEmpty()) {
                    Text(
                        text = profile.professionalDetails.companyName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Education Card
    if (profile.professionalDetails.education.isNotEmpty()) {
        cardBlocks.add {
            InfoCard(
                title = "EDUCATION",
                icon = Icons.Default.School,
                backgroundColor = Color(0xFFC8E6E6),
                iconColor = Color(0xFF4A8A8A)
            ) {
                profile.professionalDetails.education.forEach { education ->
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = education.degree,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (education.fieldOfStudy.isNotEmpty()) {
                            Text(
                                text = education.fieldOfStudy,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        if (education.institution.isNotEmpty()) {
                            Text(
                                text = education.institution,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        if (education.year.isNotEmpty()) {
                            Text(
                                text = education.year,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    if (education != profile.professionalDetails.education.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Family Details Card
    if (profile.personalDetails.relationships.isNotEmpty()) {
        cardBlocks.add {
            InfoCard(
                title = "FAMILY DETAILS",
                icon = Icons.Default.People,
                backgroundColor = Color(0xFFD5D5E5),
                iconColor = Color(0xFF6A6A8A)
            ) {
                profile.personalDetails.relationships.forEach { relationship ->
                    if (relationship.name.isNotEmpty()) {
                        InfoRow("${relationship.type}:", relationship.name)
                    }
                }
            }
        }
    }

    // Current Details Card
    val currentAddress = profile.address.current
    if (currentAddress.address.isNotEmpty() ||
        currentAddress.city.isNotEmpty()
    ) {
        cardBlocks.add {
            InfoCard(
                title = "CURRENT DETAILS",
                icon = Icons.Default.LocationOn,
                backgroundColor = Color(0xFFD4ECD4),
                iconColor = Color(0xFF5A8A5A)
            ) {
                if (currentAddress.address.isNotEmpty()) {
                    Text(
                        text = currentAddress.address,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
                val cityStateCountry = buildString {
                    if (currentAddress.city.isNotEmpty()) append(currentAddress.city)
                    if (currentAddress.state.isNotEmpty()) {
                        if (isNotEmpty()) append(", ")
                        append(currentAddress.state)
                    }
                    if (currentAddress.country.isNotEmpty()) {
                        if (isNotEmpty()) append(", ")
                        append(currentAddress.country)
                    }
                    if (currentAddress.pincode.isNotEmpty()) {
                        if (isNotEmpty()) append(" - ")
                        append(currentAddress.pincode)
                    }
                }
                if (cityStateCountry.isNotEmpty()) {
                    Text(
                        text = cityStateCountry,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Origin Details Card
    val nativeAddress = profile.address.native
    if (nativeAddress.city.isNotEmpty() ||
        profile.personalDetails.subCaste.isNotEmpty()
    ) {
        cardBlocks.add {
            InfoCard(
                title = "ORIGIN DETAILS",
                icon = Icons.Default.Home,
                backgroundColor = Color(0xFFB8E5F0),
                iconColor = Color(0xFF4A90A4)
            ) {
                val originLocation = buildString {
                    if (nativeAddress.city.isNotEmpty()) append(nativeAddress.city)
                    if (nativeAddress.state.isNotEmpty()) {
                        if (isNotEmpty()) append(", ")
                        append(nativeAddress.state)
                    }
                    if (nativeAddress.country.isNotEmpty()) {
                        if (isNotEmpty()) append(", ")
                        append(nativeAddress.country)
                    }
                }
                if (originLocation.isNotEmpty()) {
                    Text(
                        text = originLocation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (profile.personalDetails.subCaste.isNotEmpty()) {
                    InfoRow("Gotta:", profile.personalDetails.subCaste)
                }
            }
        }
    }

    // Social Media Links Card
    val social = profile.socialMedia
    if (social.facebook.isNotEmpty() ||
        social.instagram.isNotEmpty() ||
        social.twitter.isNotEmpty() ||
        social.linkedin.isNotEmpty() ||
        social.youtube.isNotEmpty()
    ) {
        cardBlocks.add {
            InfoCard(
                title = "SOCIAL MEDIA LINKS",
                icon = Icons.Default.Share,
                backgroundColor = Color(0xFFD4ECD4),
                iconColor = Color(0xFF5A8A5A)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (social.facebook.isNotEmpty()) {
                        SocialMediaLink("Facebook", social.facebook, "📘")
                    }
                    if (social.instagram.isNotEmpty()) {
                        SocialMediaLink("Instagram", social.instagram, "📷")
                    }
                    if (social.twitter.isNotEmpty()) {
                        SocialMediaLink("Twitter", social.twitter, "🐦")
                    }
                    if (social.linkedin.isNotEmpty()) {
                        SocialMediaLink("LinkedIn", social.linkedin, "💼")
                    }
                    if (social.youtube.isNotEmpty()) {
                        SocialMediaLink("YouTube", social.youtube, "▶️")
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cardBlocks.forEachIndexed { index, block ->
            Box(modifier = Modifier.fillMaxWidth()) {
                block()
            }
            if ((index + 1) % 2 == 0 && activity != null) {
                val adIndex = (index + 1) / 2
                key(adIndex) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                         NativeAdCard(activity = activity, adIndex = adIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeAdCard(activity: Activity, adIndex: Int) {
    val adTag = "native_ad_loaded_$adIndex"
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        update = { view ->
            if (view.tag != adTag) {
                view.tag = adTag
                AdSdkManager.registerSdkReadyCallback {
                    view.post {
                        AdSdkManager.showNativeAd(
                            activity = activity,
                            parent = view,
                            onAdLoaded = { Log.d("ProfileInfo", "Native ad loaded") },
                            onAdFailed = { error ->
                                Log.e("ProfileInfo", "Native ad failed: $error")
                                view.tag = null
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            // Content
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun SocialMediaLink(platform: String, link: String, emoji: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = platform,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = link,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
