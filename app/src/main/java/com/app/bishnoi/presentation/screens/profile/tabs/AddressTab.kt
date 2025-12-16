package com.app.bishnoi.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.bishnoi.presentation.components.CityAutocompleteField
import com.app.bishnoi.presentation.components.ProfileTextField
import com.app.bishnoi.utils.ValidationHelper

@Composable
fun AddressTab(
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
            text = "Address Information",
            style = MaterialTheme.typography.titleLarge
        )

        // Current Address
        Text(
            text = "Current Address",
            style = MaterialTheme.typography.titleMedium
        )

        ProfileTextField(
            value = uiState.currentAddress.address,
            onValueChange = {
                viewModel.updateCurrentAddress(
                    uiState.currentAddress.copy(address = it)
                )
            },
            label = "Address",
            placeholder = "Street, Area, Landmark",
            maxLines = 3
        )

        CityAutocompleteField(
            value = uiState.currentAddress.city,
            onValueChange = {
                viewModel.updateCurrentAddress(
                    uiState.currentAddress.copy(city = it)
                )
            },
            onPlaceSelected = { city, state, country ->
                // Auto-fill city, state, and country when a place is selected
                viewModel.updateCurrentAddress(
                    uiState.currentAddress.copy(
                        city = city,
                        state = state,
                        country = country
                    )
                )
            },
            label = "City",
            placeholder = "Search for your city"
        )

        ProfileTextField(
            value = uiState.currentAddress.state,
            onValueChange = {
                viewModel.updateCurrentAddress(
                    uiState.currentAddress.copy(state = it)
                )
            },
            label = "State",
            placeholder = "Enter state"
        )

        ProfileTextField(
            value = uiState.currentAddress.country,
            onValueChange = {
                viewModel.updateCurrentAddress(
                    uiState.currentAddress.copy(country = it)
                )
            },
            label = "Country",
            placeholder = "Enter country"
        )

        ProfileTextField(
            value = uiState.currentAddress.pincode,
            onValueChange = {
                viewModel.updateCurrentAddress(
                    uiState.currentAddress.copy(pincode = it)
                )
            },
            label = "Pincode",
            placeholder = "Enter pincode",
            errorMessage = ValidationHelper.validatePincode(uiState.currentAddress.pincode)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Native Address
        Text(
            text = "Native Address",
            style = MaterialTheme.typography.titleMedium
        )

        ProfileTextField(
            value = uiState.nativeAddress.address,
            onValueChange = {
                viewModel.updateNativeAddress(
                    uiState.nativeAddress.copy(address = it)
                )
            },
            label = "Address",
            placeholder = "Street, Area, Landmark",
            maxLines = 3
        )

        CityAutocompleteField(
            value = uiState.nativeAddress.city,
            onValueChange = {
                viewModel.updateNativeAddress(
                    uiState.nativeAddress.copy(city = it)
                )
            },
            onPlaceSelected = { city, state, country ->
                // Auto-fill city, state, and country when a place is selected
                viewModel.updateNativeAddress(
                    uiState.nativeAddress.copy(
                        city = city,
                        state = state,
                        country = country
                    )
                )
            },
            label = "City",
            placeholder = "Search for your city"
        )

        ProfileTextField(
            value = uiState.nativeAddress.state,
            onValueChange = {
                viewModel.updateNativeAddress(
                    uiState.nativeAddress.copy(state = it)
                )
            },
            label = "State",
            placeholder = "Enter state"
        )

        ProfileTextField(
            value = uiState.nativeAddress.country,
            onValueChange = {
                viewModel.updateNativeAddress(
                    uiState.nativeAddress.copy(country = it)
                )
            },
            label = "Country",
            placeholder = "Enter country"
        )

        ProfileTextField(
            value = uiState.nativeAddress.pincode,
            onValueChange = {
                viewModel.updateNativeAddress(
                    uiState.nativeAddress.copy(pincode = it)
                )
            },
            label = "Pincode",
            placeholder = "Enter pincode",
            errorMessage = ValidationHelper.validatePincode(uiState.nativeAddress.pincode)
        )

        Button(
            onClick = { viewModel.saveAddress() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Address")
            }
        }
    }
}