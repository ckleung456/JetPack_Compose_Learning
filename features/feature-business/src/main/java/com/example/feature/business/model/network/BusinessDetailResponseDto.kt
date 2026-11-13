package com.example.feature.business.model.network

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessDetailResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("alias") val alias: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("is_claimed") val isClaimed: Boolean? = null,
    @SerializedName("is_closed") val isClosed: Boolean? = null,
    @SerializedName("date_opened") val dateOpened: String? = null,
    @SerializedName("date_closed") val dateClosed: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("display_phone") val displayPhone: String? = null,
    @SerializedName("review_count") val reviewCount: Int? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("price") val price: String? = null,
    @SerializedName("distance") val distance: Double? = null,
    @SerializedName("categories") val categories: List<CategoryDto>? = null,
    @SerializedName("coordinates") val coordinates: CoordinatesDto? = null,
    @SerializedName("location") val location: LocationDto? = null,
    @SerializedName("photos") val photos: List<String>? = null,
    @SerializedName("photo_details") val photoDetails: List<PhotoDetailDto>? = null,
    @SerializedName("photo_count") val photoCount: Int? = null,
    @SerializedName("hours") val hours: List<HoursDto>? = null,
    @SerializedName("special_hours") val specialHours: List<SpecialHourDto>? = null,
    @SerializedName("transactions") val transactions: List<String>? = null,
    @SerializedName("attributes") val attributes: AttributesDto? = null,
    @SerializedName("messaging") val messaging: MessagingDto? = null,
    @SerializedName("yelp_menu_url") val yelpMenuUrl: String? = null,
    @SerializedName("rapc") val rapc: RapcDto? = null,
    @SerializedName("raq") val raq: RaqDto? = null,
    @SerializedName("online_reservation") val onlineReservation: OnlineReservationDto? = null
)

@Serializable
data class PhotoDetailDto(
    @SerializedName("photo_id") val photoId: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("caption") val caption: String? = null,
    @SerializedName("width") val width: String? = null,
    @SerializedName("height") val height: String? = null,
    @SerializedName("is_user_submitted") val isUserSubmitted: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("label") val label: String? = null
)

@Serializable
data class HoursDto(
    @SerializedName("open") val open: List<OpenDto>? = null,
    @SerializedName("hours_type") val hoursType: String? = null,
    @SerializedName("is_open_now") val isOpenNow: Boolean? = null
)

@Serializable
data class OpenDto(
    @SerializedName("is_overnight") val isOvernight: Boolean? = null,
    @SerializedName("start") val start: String? = null,
    @SerializedName("end") val end: String? = null,
    @SerializedName("day") val day: Int? = null
)

@Serializable
data class SpecialHourDto(
    @SerializedName("date") val date: String? = null,
    @SerializedName("end") val end: String? = null,
    @SerializedName("is_closed") val isClosed: Boolean? = null,
    @SerializedName("is_overnight") val isOvernight: Boolean? = null,
    @SerializedName("start") val start: String? = null
)

@Serializable
data class AttributesDto(
    @SerializedName("business_temp_closed") val businessTempClosed: Long? = null,
    @SerializedName("outdoor_seating") val outdoorSeating: Boolean? = null,
    @SerializedName("liked_by_vegans") val likedByVegans: Boolean? = null,
    @SerializedName("liked_by_vegetarians") val likedByVegetarians: Boolean? = null,
    @SerializedName("hot_and_new") val hotAndNew: String? = null
)

@Serializable
data class MessagingDto(
    @SerializedName("url") val url: String? = null,
    @SerializedName("use_case_text") val useCaseText: String? = null,
    @SerializedName("response_rate") val responseRate: Double? = null,
    @SerializedName("response_time") val responseTime: Long? = null,
    @SerializedName("is_enabled") val isEnabled: Boolean? = null
)

@Serializable
data class RapcDto(
    @SerializedName("is_enabled") val isEnabled: Boolean? = null,
    @SerializedName("is_eligible") val isEligible: Boolean? = null
)

@Serializable
data class RaqDto(
    @SerializedName("is_enabled") val isEnabled: Boolean? = null
)

@Serializable
data class OnlineReservationDto(
    @SerializedName("url") val url: String? = null
)
