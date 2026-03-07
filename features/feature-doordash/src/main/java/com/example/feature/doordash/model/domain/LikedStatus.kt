package com.example.feature.doordash.model.domain

import kotlinx.serialization.Serializable

@Serializable
enum class LikedStatus {
    LIKED, UN_LIKED, NO_REF
}