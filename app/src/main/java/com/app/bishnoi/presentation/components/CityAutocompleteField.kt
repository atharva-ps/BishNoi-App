package com.app.bishnoi.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.bishnoi.utils.CityPrediction
import com.app.bishnoi.utils.PlacesHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onPlaceSelected: ((city: String, state: String, country: String) -> Unit)? = null,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Search city...",
    errorMessage: String? = null
) {
    var query by remember { mutableStateOf(value) }
    var predictions by remember { mutableStateOf<List<CityPrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var searchJob: Job? by remember { mutableStateOf(null) }

    // Update query when value changes externally
    LaunchedEffect(value) {
        if (value != query) {
            query = value
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { newValue ->
                query = newValue
                showDropdown = true

                // Cancel previous search
                searchJob?.cancel()

                // Debounce search
                searchJob = scope.launch {
                    delay(500) // Wait 500ms after user stops typing
                    if (newValue.isNotEmpty()) {
                        isSearching = true
                        try {
                            predictions = PlacesHelper.searchCities(newValue)
                        } catch (e: Exception) {
                            predictions = emptyList()
                        }
                        isSearching = false
                    } else {
                        predictions = emptyList()
                    }
                }
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null,
            supportingText = {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        predictions = emptyList()
                        showDropdown = false
                        onValueChange("")
                    }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors()
        )

        // Dropdown with predictions
        if (showDropdown && (predictions.isNotEmpty() || isSearching)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    LazyColumn {
                        items(predictions) { prediction ->
                            CityPredictionItem(
                                prediction = prediction,
                                onClick = {
                                    query = prediction.primaryText
                                    onValueChange(prediction.primaryText)

                                    // Notify parent about selected place with state and country
                                    onPlaceSelected?.invoke(
                                        prediction.city,
                                        prediction.state,
                                        prediction.country
                                    )

                                    showDropdown = false
                                    predictions = emptyList()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityPredictionItem(
    prediction: CityPrediction,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = prediction.primaryText,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = prediction.secondaryText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider()
}