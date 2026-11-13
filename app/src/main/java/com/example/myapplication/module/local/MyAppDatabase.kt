package com.example.myapplication.module.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feature.doordash.module.domain.LikedDao
import com.example.feature.doordash.module.domain.LikedDb

@Database(
    entities = [LikedDb::class],
    version = 2,
    exportSchema = true
)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun likedDao(): LikedDao
}
