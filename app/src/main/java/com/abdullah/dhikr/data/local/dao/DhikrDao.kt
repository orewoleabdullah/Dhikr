package com.abdullah.dhikr.data.local.dao

import androidx.room.*
import com.abdullah.dhikr.domain.model.DhikrItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {
    @Query("SELECT * from dhikr_items ORDER BY isCustom ASC, id ASC")
    fun getAllItems(): Flow<List<DhikrItem>>

    @Query("SELECT * from dhikr_items WHERE title LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY isCustom ASC, id ASC")
    fun searchItems(query: String): Flow<List<DhikrItem>>

    @Query("SELECT * from dhikr_items WHERE id = :id")
    fun getItem(id: Int): Flow<DhikrItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DhikrItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(items: List<DhikrItem>)

    @Update
    fun update(item: DhikrItem)

    @Delete
    fun delete(item: DhikrItem)
}
