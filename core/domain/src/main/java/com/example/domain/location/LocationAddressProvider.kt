package com.example.domain.location

import com.example.domain.Error
import com.example.domain.Result


interface LocationAddressProvider {
    suspend fun getCurrentAddress(): Result<String, LocationError>
}

enum class LocationError: Error {
    PERMISSION_DENIED,
    LOCATION_DISABLED,
    UNAVAILABLE,
    GEOCODER_FAILED
}