package com.notai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notai.app.data.local.entity.FavoriteEntity
import com.notai.app.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE historyId = :historyId LIMIT 1")
    suspend fun getByHistoryId(historyId: Long): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE historyId = :historyId")
    suspend fun deleteByHistoryId(historyId: Long)

    @Query("""
        SELECT h.* FROM history h
        INNER JOIN favorites f ON h.id = f.historyId
        ORDER BY f.createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getFavorites(offset: Int, limit: Int = 20): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getCount(): Int
}
