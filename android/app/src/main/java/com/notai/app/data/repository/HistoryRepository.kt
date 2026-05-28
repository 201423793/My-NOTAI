package com.notai.app.data.repository

import com.notai.app.data.local.dao.HistoryDao
import com.notai.app.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun getAll(offset: Int, limit: Int = 20): Flow<List<HistoryEntity>> =
        historyDao.getAll(offset, limit)

    suspend fun getById(id: Long): HistoryEntity? = historyDao.getById(id)

    suspend fun insert(entity: HistoryEntity): Long = historyDao.insert(entity)

    suspend fun delete(id: Long) = historyDao.delete(id)

    suspend fun clearAll() = historyDao.clearAll()

    suspend fun getCount(): Int = historyDao.getCount()
}
