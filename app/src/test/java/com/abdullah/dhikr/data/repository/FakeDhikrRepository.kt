package com.abdullah.dhikr.data.repository

import com.abdullah.dhikr.domain.model.DhikrItem
import com.abdullah.dhikr.domain.repository.DhikrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeDhikrRepository : DhikrRepository {

    private val itemsFlow = MutableStateFlow<List<DhikrItem>>(emptyList())

    override fun getAllItemsStream(): Flow<List<DhikrItem>> = itemsFlow

    override fun searchItemsStream(query: String): Flow<List<DhikrItem>> {
        return itemsFlow.map { list ->
            if (query.isBlank()) list
            else list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.arabicText.contains(query, ignoreCase = true) ||
                it.translation.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    override fun getItemStream(id: Int): Flow<DhikrItem?> {
        return itemsFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun insertItem(item: DhikrItem) {
        itemsFlow.update { current ->
            val newId = if (item.id == 0) (current.maxOfOrNull { it.id } ?: 0) + 1 else item.id
            current + item.copy(id = newId)
        }
    }

    override suspend fun deleteItem(item: DhikrItem) {
        itemsFlow.update { current -> current.filterNot { it.id == item.id } }
    }

    override suspend fun updateItem(item: DhikrItem) {
        itemsFlow.update { current ->
            current.map { if (it.id == item.id) item else it }
        }
    }

    override suspend fun seedBuiltInPresetsIfEmpty() {
        if (itemsFlow.value.none { !it.isCustom }) {
            itemsFlow.update { it + DhikrItem.BUILT_IN_PRESETS }
        }
    }
}
