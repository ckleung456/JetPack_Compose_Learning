package com.example.feature.doordash.module.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.feature.doordash.model.domain.LikedStatus

private const val LIKE_TABLE_NAME = "LikedDb"
private const val LIKE_STATUS = "Liked_Status"

@Entity(tableName = LIKE_TABLE_NAME)
@TypeConverters(LikeStatusTypeConverter::class)
data class LikedDb(
    @PrimaryKey
    var id: Long?,
    @ColumnInfo(name = LIKE_STATUS)
    var likedStatus: LikedStatus
)

class LikeStatusTypeConverter {
    @TypeConverter
    fun typeToInt(likedStatus: LikedStatus): Int =
        when (likedStatus) {
            LikedStatus.NO_REF -> -1
            LikedStatus.LIKED -> 1
            LikedStatus.UN_LIKED  -> 0
        }

    @TypeConverter
    fun intToType(int: Int): LikedStatus =
        when(int) {
            0 -> LikedStatus.UN_LIKED
            1 -> LikedStatus.LIKED
            else -> LikedStatus.NO_REF
        }
}