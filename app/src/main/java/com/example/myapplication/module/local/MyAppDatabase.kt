package com.example.myapplication.module.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.feature.doordash.module.domain.LikedDao
import com.example.feature.doordash.module.domain.LikedDb


@Database(
    entities = [LikedDb::class],
    version = 1,
    exportSchema = false
)
abstract class MyAppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        fun getDatabase(context: Context): MyAppDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context = context.applicationContext,
                MyAppDatabase::class.java,
                "MY_APP_DB"
            ).fallbackToDestructiveMigration().build()
            INSTANCE = instance
            instance
        }
    }

    abstract fun likedDao() : LikedDao
}