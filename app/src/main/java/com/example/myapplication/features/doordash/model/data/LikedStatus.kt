package com.example.myapplication.features.doordash.model.data

import kotlinx.serialization.Serializable

@Serializable
enum class LikedStatus {
    LIKED, UN_LIKED, NO_REF
}