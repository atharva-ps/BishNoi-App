package com.app.bishnoi.presentation.screens.profile.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.bishnoi.domain.model.Relationship
import com.app.bishnoi.presentation.components.MaritalStatusDropdown
import com.app.bishnoi.presentation.components.ProfileTextField
import com.app.bishnoi.presentation.components.RelationshipCard
import com.app.bishnoi.presentation.components.RelationshipTypeDropdown
import com.app.bishnoi.presentation.screens.profile.EditProfileUiState
import com.app.bishnoi.presentation.screens.profile.EditProfileViewModel

@Composable
fun PersonalDetailsTab(
    viewModel: EditProfileViewModel,
    uiState: EditProfileUiState
) {
    var showAddRelationshipDialog by remember { mutableStateOf(false) }
    var editingRelationship by remember { mutableStateOf<Relationship?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Details",
            style = MaterialTheme.typography.titleLarge
        )

        MaritalStatusDropdown(
            selectedStatus = uiState.personalDetails.maritalStatus,
            onStatusSelected = {
                viewModel.updatePersonalDetails(
                    uiState.personalDetails.copy(maritalStatus = it)
                )
            }
        )

        ProfileTextField(
            value = uiState.personalDetails.subCaste,
            onValueChange = {
                viewModel.updatePersonalDetails(
                    uiState.personalDetails.copy(subCaste = it)
                )
            },
            label = "Sub-Caste",
            placeholder = "Enter your sub-caste"
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Relationships Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Family Relationships",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = { showAddRelationshipDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Relationship",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (uiState.personalDetails.relationships.isEmpty()) {
            Text(
                text = "No relationships added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            uiState.personalDetails.relationships.forEachIndexed { index, relationship ->
                RelationshipCard(
                    relationship = relationship,
                    onEdit = {
                        editingRelationship = relationship
                        showAddRelationshipDialog = true
                    },
                    onDelete = {
                        val updatedList = uiState.personalDetails.relationships.toMutableList()
                        updatedList.removeAt(index)
                        viewModel.updatePersonalDetails(
                            uiState.personalDetails.copy(relationships = updatedList)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Button(
            onClick = { viewModel.savePersonalDetails() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Personal Details")
            }
        }
    }

    // Add/Edit Relationship Dialog
    if (showAddRelationshipDialog) {
        AddRelationshipDialog(
            existingRelationship = editingRelationship,
            onDismiss = {
                showAddRelationshipDialog = false
                editingRelationship = null
            },
            onSave = { relationship ->
                val updatedList = uiState.personalDetails.relationships.toMutableList()

                if (editingRelationship != null) {
                    // Edit existing
                    val index = updatedList.indexOf(editingRelationship)
                    if (index != -1) {
                        updatedList[index] = relationship
                    }
                } else {
                    // Add new
                    updatedList.add(relationship)
                }

                viewModel.updatePersonalDetails(
                    uiState.personalDetails.copy(relationships = updatedList)
                )

                showAddRelationshipDialog = false
                editingRelationship = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRelationshipDialog(
    existingRelationship: Relationship?,
    onDismiss: () -> Unit,
    onSave: (Relationship) -> Unit
) {
    var relationshipType by remember {
        mutableStateOf(existingRelationship?.type ?: "")
    }
    var name by remember {
        mutableStateOf(existingRelationship?.name ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingRelationship != null) "Edit Relationship"
                else "Add Relationship"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Relationship Type Dropdown
                RelationshipTypeDropdown(
                    selectedType = relationshipType,
                    onTypeSelected = { relationshipType = it }
                )

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("Enter name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (relationshipType.isNotEmpty() && name.isNotEmpty()) {
                        onSave(
                            Relationship(
                                type = relationshipType,
                                name = name,
                                userId = existingRelationship?.userId
                            )
                        )
                    }
                },
                enabled = relationshipType.isNotEmpty() && name.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
