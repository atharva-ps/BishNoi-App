package com.app.bishnoi.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.bishnoi.presentation.components.ProfileTextField

@Composable
fun ProfessionalTab(
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
            text = "Professional Details",
            style = MaterialTheme.typography.titleLarge
        )

        ProfileTextField(
            value = uiState.professionalDetails.occupation,
            onValueChange = {
                viewModel.updateProfessionalDetails(
                    uiState.professionalDetails.copy(occupation = it)
                )
            },
            label = "Occupation",
            placeholder = "e.g., Software Engineer, Doctor"
        )

        ProfileTextField(
            value = uiState.professionalDetails.companyName,
            onValueChange = {
                viewModel.updateProfessionalDetails(
                    uiState.professionalDetails.copy(companyName = it)
                )
            },
            label = "Company Name",
            placeholder = "Enter company name"
        )

        ProfileTextField(
            value = uiState.professionalDetails.designation,
            onValueChange = {
                viewModel.updateProfessionalDetails(
                    uiState.professionalDetails.copy(designation = it)
                )
            },
            label = "Designation",
            placeholder = "e.g., Senior Developer, Manager"
        )

        ProfileTextField(
            value = uiState.professionalDetails.industry,
            onValueChange = {
                viewModel.updateProfessionalDetails(
                    uiState.professionalDetails.copy(industry = it)
                )
            },
            label = "Industry",
            placeholder = "e.g., IT, Healthcare, Education"
        )

        Text(
            text = "Education",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Add your education details (Coming soon)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { viewModel.saveProfessionalDetails() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Professional Details")
            }
        }
    }
}
