package com.example.feature.doordash.model.network

object APIConstants {
    const val ADDRESS_LAT = "lat"
    const val ADDRESS_LNG = "lng"
    const val OFFSET = "offset"
    const val LIMIT = "limit"
    const val RESTAURANT_ID = "restaurant_id"

    const val ENDPOINT_RESTAURANT_LIST = "/v2/restaurant/"
    const val ENDPOINT_RESTAURANT_DETAIL = "v2/restaurant/{$RESTAURANT_ID}/"
}