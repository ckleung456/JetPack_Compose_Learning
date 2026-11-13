package com.example.feature.doordash.module.domain

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedDao {
    @Query("SELECT * FROM LikedDb")
    fun getAll(): Flow<List<LikedDb>>

    @Query("SELECT * FROM LikedDb WHERE id = :id")
    suspend fun getById(id: Long): LikedDb?

    @Insert(onConflict = REPLACE)
    suspend fun insert(likedDb: LikedDb)

    @Delete
    suspend fun deleteAll(items: List<LikedDb>): Int

    @Query("DELETE FROM LikedDb")
    suspend fun clearAll()
}