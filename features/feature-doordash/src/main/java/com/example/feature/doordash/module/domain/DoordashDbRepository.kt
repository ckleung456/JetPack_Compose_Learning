package com.example.feature.doordash.module.domain

import com.example.feature.doordash.model.domain.LikedStatus
import javax.inject.Inject

class DoordashDbRepository @Inject constructor(
    private val likedDao: RestaurantLikedDao
) {
    fun getAllLikedStatus() = likedDao.getAll()

    suspend fun getLikedStatusById(restaurantId: Long?) : LikedStatus? {
        return restaurantId?.let {
            likedDao.getById(id = restaurantId)?.likedStatus
        }
    }

    suspend fun saveLikedStatus(restaurantId: Long, likedStatus: LikedStatus) {
        likedDao.insert(
            RestaurantLikedDb(
                id = restaurantId,
                likedStatus = likedStatus
            )
        )
    }

    suspend fun clearDb() = likedDao.clearAll()
}