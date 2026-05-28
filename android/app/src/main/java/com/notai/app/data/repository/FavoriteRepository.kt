package com.notai.app.data.repository

import com.notai.app.data.local.dao.FavoriteDao
import com.notai.app.data.local.dao.HistoryDao
import com.notai.app.data.local.entity.FavoriteEntity
import com.notai.app.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao
) {
    suspend fun toggle(historyId: Long): Boolean {
        val existing = favoriteDao.getByHistoryId(historyId)
        return if (existing != null) {
            favoriteDao.deleteByHistoryId(historyId)
            historyDao.getById(historyId)?.let {
                historyDao.insert(it.copy(isFavorited = false))
            }
            false
        } else {
            favoriteDao.insert(FavoriteEntity(historyId = historyId))
            historyDao.getById(historyId)?.let {
                historyDao.insert(it.copy(isFavorited = true))
            }
            true
        }
    }

    suspend fun isFavorited(historyId: Long): Boolean =
        favoriteDao.getByHistoryId(historyId) != null

    fun getFavorites(offset: Int, limit: Int = 20): Flow<List<HistoryEntity>> =
        favoriteDao.getFavorites(offset, limit)

    suspend fun getCount(): Int = favoriteDao.getCount()
}
