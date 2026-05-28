package com.notai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notai.app.data.local.dao.FavoriteDao
import com.notai.app.data.local.dao.HistoryDao
import com.notai.app.data.local.entity.FavoriteEntity
import com.notai.app.data.local.entity.HistoryEntity

@Database(
    entities = [HistoryEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NotaiDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
}
