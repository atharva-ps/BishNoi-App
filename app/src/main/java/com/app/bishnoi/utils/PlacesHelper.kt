package com.app.bishnoi.utils

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PlacesHelper {

    private var placesClient: PlacesClient? = null

    fun initialize(context: Context) {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, getApiKey(context))
        }
        placesClient = Places.createClient(context)
    }

    private fun getApiKey(context: Context): String {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_META_DATA
        )
        return appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
    }

    suspend fun searchCities(query: String): List<CityPrediction> = suspendCancellableCoroutine { continuation ->
        if (query.isEmpty()) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }

        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setTypeFilter(TypeFilter.CITIES)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient?.findAutocompletePredictions(request)
            ?.addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions.map { prediction ->
                    // Parse the full text to extract city, state, and country
                    val parts = prediction.getFullText(null).toString().split(", ")

                    val city = parts.getOrNull(0) ?: ""
                    val state = if (parts.size >= 2) parts[parts.size - 2] else ""
                    val country = parts.lastOrNull() ?: ""

                    CityPrediction(
                        placeId = prediction.placeId,
                        primaryText = prediction.getPrimaryText(null).toString(),
                        secondaryText = prediction.getSecondaryText(null).toString(),
                        fullText = prediction.getFullText(null).toString(),
                        city = city,
                        state = state,
                        country = country
                    )
                }
                continuation.resume(predictions)
            }
            ?.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    // Optional: Get detailed place information if needed
    suspend fun getPlaceDetails(placeId: String): PlaceDetails? = suspendCancellableCoroutine { continuation ->
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS_COMPONENTS
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient?.fetchPlace(request)
            ?.addOnSuccessListener { response ->
                val place = response.place
                val addressComponents = place.addressComponents?.asList()

                var city = ""
                var state = ""
                var country = ""

                addressComponents?.forEach { component ->
                    when {
                        component.types.contains("locality") -> {
                            city = component.name
                        }
                        component.types.contains("administrative_area_level_1") -> {
                            state = component.name
                        }
                        component.types.contains("country") -> {
                            country = component.name
                        }
                    }
                }

                continuation.resume(
                    PlaceDetails(
                        placeId = place.id ?: "",
                        city = city,
                        state = state,
                        country = country
                    )
                )
            }
            ?.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}

data class CityPrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullText: String,
    val city: String = "",
    val state: String = "",
    val country: String = ""
)

data class PlaceDetails(
    val placeId: String,
    val city: String,
    val state: String,
    val country: String
)