package com.example.feature.business.model.domain

import androidx.compose.runtime.Immutable
import com.example.feature.business.R
import java.util.Locale

@Immutable
data class BusinessDetailUi(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val ratingText: String,
    val reviewCount: Int,
    val priceText: String?,
    val distanceText: String?,
    val address: String,
    val phone: String?,
    val categories: List<String>,
    val isClaimed: Boolean,
    val isClosed: Boolean,
    val dateOpened: String?,
    val dateClosed: String?,
    val menu: String?,
    val specialHours: List<SpecialHourUi>,
    val hours: HoursUi?
)

@Immutable
data class SpecialHourUi(
    val date: String,
    val end: String,
    val isClosed: Boolean,
    val isOvernight: Boolean,
    val start: String
)

@Immutable
data class HoursUi(
    val open: List<OpenUi>,
    val hoursType: String,
    val isOpenNow: Boolean
)

@Immutable
data class OpenUi(
    val isOvernight: Boolean,
    val start: String,
    val end: String,
    val day: Int
)

fun BusinessDetail.toBusinessDetailUi() = BusinessDetailUi(
    id = id,
    name = name,
    imageUrl = imageUrl,
    ratingText = String.format(Locale.getDefault(), "%.1f", rating),
    reviewCount = reviewCount,
    priceText = price,
    distanceText = distanceMeters?.let { String.format(Locale.getDefault(), "%.1f mi", it / METERS_PER_MILE) },
    address = displayAddress,
    phone = phone,
    categories = categories,
    isClaimed = isClaimed,
    isClosed = isClosed,
    dateOpened = dateOpened,
    dateClosed = dateClosed,
    menu = menu,
    specialHours = specialHours.map { it.toSpecialHourUi() },
    hours = hours?.toHoursUi()
)

fun Hours.toHoursUi() = HoursUi(
    open = open.map { it.toOpenUi() },
    hoursType = hoursType,
    isOpenNow = isOpenNow
)

fun Open.toOpenUi() = OpenUi(
    isOvernight = isOvernight,
    start = start,
    end = end,
    day = day.mapDayOfWeek()
)

private fun Int.mapDayOfWeek() = when(this) {
    0 -> R.string.Sunday
    1 -> R.string.Monday
    2 -> R.string.Tuesday
    3 -> R.string.Wednesday
    4 -> R.string.Thursday
    5 -> R.string.Friday
    6 -> R.string.Saturday
    else -> throw IllegalArgumentException("Invalid day of week: $this")
}

fun SpecialHour.toSpecialHourUi() = SpecialHourUi(
    date = date,
    end = end,
    isClosed = isClosed,
    isOvernight = isOvernight,
    start = start
)

