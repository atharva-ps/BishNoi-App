package com.justbaat.mybishnoiapp.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.justbaat.mybishnoiapp.presentation.components.DatePickerField
import com.justbaat.mybishnoiapp.presentation.components.GenderDropdown
import com.justbaat.mybishnoiapp.presentation.components.ProfileTextField
import com.justbaat.mybishnoiapp.utils.ValidationHelper

@Composable
fun BasicInfoTab(
    viewModel: EditProfileViewModel,
    uiState: EditProfileUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.titleLarge
        )

        ProfileTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.updateFirstName(it) },
            label = "First Name",
            placeholder = "Enter your first name",
            errorMessage = ValidationHelper.validateName(uiState.firstName, "First name")
        )

        ProfileTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.updateLastName(it) },
            label = "Last Name",
            placeholder = "Enter your last name",
            errorMessage = ValidationHelper.validateName(uiState.lastName, "Last name")
        )

        ProfileTextField(
            value = uiState.username,
            onValueChange = { viewModel.updateUsername(it) },
            label = "Username",
            placeholder = "Enter your username",
            errorMessage = ValidationHelper.validateUsername(uiState.username)
        )

        ProfileTextField(
            value = uiState.mobileNumber,
            onValueChange = { viewModel.updateMobileNumber(it) },
            label = "Mobile Number",
            placeholder = "+91XXXXXXXXXX",
            keyboardType = KeyboardType.Phone,
            errorMessage = ValidationHelper.validateMobileNumber(uiState.mobileNumber)
        )

        GenderDropdown(
            selectedGender = uiState.gender,
            onGenderSelected = { viewModel.updateGender(it) }
        )

        DatePickerField(
            value = uiState.dob,
            onValueChange = { viewModel.updateDob(it) },
            label = "Date of Birth"
        )

        ProfileTextField(
            value = uiState.aboutMe,
            onValueChange = { viewModel.updateAboutMe(it) },
            label = "About Me",
            placeholder = "Tell us about yourself...",
            maxLines = 4
        )

        Button(
            onClick = { viewModel.saveBasicInfo() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Basic Info")
            }
        }
    }
}
