package com.example.myapplication.features.doordash.module.local

import com.example.myapplication.features.doordash.model.data.LikedStatus
import javax.inject.Inject

class DoordashDbRepository @Inject constructor(
    private val likedDao: LikedDao
) {
    suspend fun getAllLikedStatus() = likedDao.getAll()

    suspend fun getLikedStatusById(restaurantId: Long?) : LikedStatus? {
        return restaurantId?.let {
            likedDao.getById(id = restaurantId)?.likedStatus
        }
    }

    suspend fun saveLikedStatus(restaurantId: Long, likedStatus: LikedStatus) {
        likedDao.insert(
            LikedDb(
                id = restaurantId,
                likedStatus = likedStatus
            )
        )
    }

    suspend fun clearDb() {
        getAllLikedStatus().collect {
            likedDao.deleteAll(items = it)
        }
    }
}