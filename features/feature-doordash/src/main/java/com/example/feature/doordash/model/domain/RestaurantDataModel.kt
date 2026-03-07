package com.example.feature.doordash.model.domain

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantDataModel(
    val id: Long,
    val name: String?,
    val description: String?,
    @SerializedName("cover_img_url")
    val coverImgUrl: String?,
    val status: String?,
    @SerializedName("delivery_fee")
    val deliveryFee: String?,
    @SerializedName("status_type")
    val statusType: String?
)
