package com.justbaat.mybishnoiapp.utils

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
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
                    CityPrediction(
                        placeId = prediction.placeId,
                        primaryText = prediction.getPrimaryText(null).toString(),
                        secondaryText = prediction.getSecondaryText(null).toString(),
                        fullText = prediction.getFullText(null).toString()
                    )
                }
                continuation.resume(predictions)
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
    val fullText: String
)
