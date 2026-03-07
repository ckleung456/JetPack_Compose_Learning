package com.example.feature.doordash.model.domain.usecase

import com.example.feature.doordash.model.domain.LikedStatus

data class LikeRestaurantUseCaseInput(
    val restaurantId: Long,
    val status: LikedStatus
)