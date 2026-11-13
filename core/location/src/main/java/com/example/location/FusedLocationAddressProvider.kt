package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.example.domain.Result
import kotlinx.coroutines.CoroutineDispatcher
import com.example.domain.location.LocationAddressProvider
import com.example.domain.location.LocationError
import com.example.shared.lib.hasLocationPermissions
import com.example.shared.lib.isLocationEnabled
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class FusedLocationAddressProvider(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val ioDispatcher: CoroutineDispatcher
): LocationAddressProvider {

    override suspend fun getCurrentAddress(): Result<String, LocationError> {
        if (!context.hasLocationPermissions()) return Result.Error(LocationError.PERMISSION_DENIED)
        if (!context.isLocationEnabled()) return Result.Error(LocationError.LOCATION_DISABLED)

        val location = try {
            currentLocation()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        } ?: return Result.Error(LocationError.UNAVAILABLE)

        val address = try {
            reverseGeocode(location)
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } ?: return Result.Error(LocationError.GEOCODER_FAILED)

        val formatted = address.format()
        return if (formatted.isBlank()) {
            Result.Error(LocationError.GEOCODER_FAILED)
        } else {
            Result.Success(formatted)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(): Location? {
        val cancellationTokenSource = CancellationTokenSource()
        return try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
                .await()
                ?: fusedLocationClient.lastLocation.await()
        } finally {
            cancellationTokenSource.cancel()
        }
    }

    private suspend fun reverseGeocode(location: Location): Address? {
        if (!Geocoder.isPresent()) return null

        val geocoder = Geocoder(context , Locale.getDefault())
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: List<Address?>) {
                            continuation.resume(addresses.firstOrNull()) { _, _, _ -> }
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(null) { _, _, _ ->}
                        }
                    }
                )
            }
            else -> withContext(ioDispatcher) {
                @SuppressLint("Deprecated")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            }
        }
    }

    private fun Address.format(): String {
        val street = listOfNotNull(subThoroughfare, thoroughfare).joinToString(" ").ifBlank { null }
        val parts = listOfNotNull(
            street,
            locality ?: subAdminArea,
            adminArea
        ).filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            getAddressLine(0).orEmpty()
        }
    }
}