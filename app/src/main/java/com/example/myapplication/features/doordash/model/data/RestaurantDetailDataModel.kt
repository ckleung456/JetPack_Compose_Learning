package com.example.myapplication.features.doordash.model.data

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantDetailDataModel(
    val id: Long = Long.MIN_VALUE,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    @SerializedName("delivery_fee")
    val deliveryFee: Long? = Long.MIN_VALUE,
    @SerializedName("average_rating")
    val averageRating: Double? = Double.MIN_VALUE,
    @SerializedName("status_type")
    val statusType: String? = null,
    val status: String? = null,
    @SerializedName("yelp_rating")
    val yelpRating: Double? = Double.MIN_VALUE,
    @SerializedName("cover_img_url")
    val coverImgUrl: String? = null,
    val name: String? = null,
    val description: String? = null
)
