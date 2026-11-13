package com.example.feature.business.model.domain

data class BusinessDetail(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val rating: Double,
    val reviewCount: Int,
    val price: String?,
    val displayAddress: String,
    val phone: String?,
    val distanceMeters: Double?,
    val categories: List<String>,
    val isClaimed: Boolean,
    val isClosed: Boolean,
    val dateOpened: String?,
    val dateClosed: String?,
    val hours: Hours?,
    val specialHours: List<SpecialHour>,
    val menu: String?
)

data class Hours(
    val open: List<Open>,
    val hoursType: String,
    val isOpenNow: Boolean
)

data class Open(
    val isOvernight: Boolean,
    val start: String,
    val end: String,
    val day: Int
)

data class SpecialHour(
    val date: String,
    val end: String,
    val isClosed: Boolean,
    val isOvernight: Boolean,
    val start: String
)