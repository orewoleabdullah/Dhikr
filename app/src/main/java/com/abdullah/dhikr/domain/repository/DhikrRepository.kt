package com.abdullah.dhikr.domain.repository

import com.abdullah.dhikr.domain.model.DhikrItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, search, and retrieve of [DhikrItem].
 */
interface DhikrRepository {
    /**
     * Retrieve all items from the data source.
     */
    fun getAllItemsStream(): Flow<List<DhikrItem>>

    /**
     * Search items matching query in title, arabicText, translation, or description.
     */
    fun searchItemsStream(query: String): Flow<List<DhikrItem>>

    /**
     * Retrieve an item matching [id].
     */
    fun getItemStream(id: Int): Flow<DhikrItem?>

    /**
     * Insert item into the data source.
     */
    suspend fun insertItem(item: DhikrItem)

    /**
     * Delete item from the data source.
     */
    suspend fun deleteItem(item: DhikrItem)

    /**
     * Update item in the data source.
     */
    suspend fun updateItem(item: DhikrItem)

    /**
     * Ensures built-in presets are seeded safely.
     */
    suspend fun seedBuiltInPresetsIfEmpty()
}
