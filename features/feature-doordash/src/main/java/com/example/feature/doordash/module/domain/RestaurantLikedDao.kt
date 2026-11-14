package com.example.feature.doordash.module.domain

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantLikedDao {
    @Query("SELECT * FROM RestaurantLikedDb")
    fun getAll(): Flow<List<RestaurantLikedDb>>

    @Query("SELECT * FROM RestaurantLikedDb WHERE id = :id")
    suspend fun getById(id: Long): RestaurantLikedDb?

    @Insert(onConflict = REPLACE)
    suspend fun insert(likedDb: RestaurantLikedDb)

    @Delete
    suspend fun deleteAll(items: List<RestaurantLikedDb>): Int

    @Query("DELETE FROM RestaurantLikedDb")
    suspend fun clearAll()
}