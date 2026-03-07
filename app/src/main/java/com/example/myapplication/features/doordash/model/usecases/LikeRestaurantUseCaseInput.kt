package com.example.myapplication.features.doordash.model.usecases

import com.example.myapplication.features.doordash.model.data.LikedStatus

data class LikeRestaurantUseCaseInput(
    val restaurantId: Long,
    val status: LikedStatus
)