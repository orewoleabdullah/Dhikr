package com.abdullah.dhikr.data.repository

import com.abdullah.dhikr.data.local.dao.DhikrDao
import com.abdullah.dhikr.domain.model.DhikrItem
import com.abdullah.dhikr.domain.repository.DhikrRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class OfflineDhikrRepository(private val dhikrDao: DhikrDao) : DhikrRepository {
    override fun getAllItemsStream(): Flow<List<DhikrItem>> = dhikrDao.getAllItems()

    override fun searchItemsStream(query: String): Flow<List<DhikrItem>> {
        return if (query.isBlank()) {
            dhikrDao.getAllItems()
        } else {
            dhikrDao.searchItems(query.trim())
        }
    }

    override fun getItemStream(id: Int): Flow<DhikrItem?> = dhikrDao.getItem(id)

    override suspend fun insertItem(item: DhikrItem) {
        withContext(Dispatchers.IO) {
            dhikrDao.insert(item)
        }
    }

    override suspend fun deleteItem(item: DhikrItem) {
        withContext(Dispatchers.IO) {
            dhikrDao.delete(item)
        }
    }

    override suspend fun updateItem(item: DhikrItem) {
        withContext(Dispatchers.IO) {
            dhikrDao.update(item)
        }
    }

    override suspend fun seedBuiltInPresetsIfEmpty() {
        withContext(Dispatchers.IO) {
            val currentItems = dhikrDao.getAllItems().first()
            val existingTitles = currentItems.map { it.title.lowercase() }.toSet()
            val missingBuiltIns = DhikrItem.BUILT_IN_PRESETS.filter { it.title.lowercase() !in existingTitles }
            if (missingBuiltIns.isNotEmpty()) {
                dhikrDao.insertAll(missingBuiltIns)
            }
        }
    }
}
