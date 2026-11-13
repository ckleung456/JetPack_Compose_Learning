package com.example.feature.business.model.domain

data class Business(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val rating: Double,
    val reviewCount: Int,
    val price: String?,
    val displayAddress: String,
    val phone: String?,
    val distanceMeters: Double?
)

enum class BusinessCategory(val searchTerm: String) {
    PIZZA("pizza"),
    BEER("beer")
}

enum class BusinessFilter {
    ALL,
    PIZZA,
    BEER
}

data class NearbyBusinesses(
    val pizza: List<Business>,
    val beer: List<Business>,
    val failed: Map<BusinessCategory, ErrorData> = emptyMap()
)

interface ErrorData