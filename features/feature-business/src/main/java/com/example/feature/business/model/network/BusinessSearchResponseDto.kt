package com.example.feature.business.model.network

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessSearchResponseDto(
    @SerializedName("businesses") val businesses: List<BusinessDto> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

@Serializable
data class BusinessDto(
    @SerializedName("id") val id: String,
    @SerializedName("alias") val alias: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("is_closed") val isClosed: Boolean = false,
    @SerializedName("url") val url: String? = null,
    @SerializedName("review_count") val reviewCount: Int = 0,
    @SerializedName("categories") val categories: List<CategoryDto> = emptyList(),
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("coordinates") val coordinates: CoordinatesDto? = null,
    @SerializedName("price") val price: String? = null,
    @SerializedName("location") val location: LocationDto? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("display_phone") val displayPhone: String? = null,
    @SerializedName("distance") val distance: Double? = null
)

@Serializable
data class CategoryDto(
    @SerializedName("alias") val alias: String? = null,
    @SerializedName("title") val title: String? = null
)

@Serializable
data class CoordinatesDto(
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

@Serializable
data class LocationDto(
    @SerializedName("address1") val address1: String? = null,
    @SerializedName("address2") val address2: String? = null,
    @SerializedName("address3") val address3: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("zip_code") val zipCode: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("display_address") val displayAddress: List<String> = emptyList()
)
