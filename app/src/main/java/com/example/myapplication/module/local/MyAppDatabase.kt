package com.example.myapplication.module.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.doordash.module.domain.RestaurantLikedDao
import com.example.feature.doordash.module.domain.RestaurantLikedDb

@Database(
    entities = [
        RestaurantLikedDb::class,
    ],
    version = 2,
    exportSchema = true
)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun restaurantLikedDao(): RestaurantLikedDao
}
