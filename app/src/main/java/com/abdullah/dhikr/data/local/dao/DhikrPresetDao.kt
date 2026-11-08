package com.abdullah.dhikr.data.local.dao

import androidx.room.*
import com.abdullah.dhikr.data.local.entity.DhikrPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrPresetDao {
    @Query("SELECT * FROM dhikr_presets ORDER BY isBuiltIn DESC, id ASC")
    fun getAllPresetsStream(): Flow<List<DhikrPresetEntity>>

    @Query("SELECT * FROM dhikr_presets WHERE name LIKE '%' || :query || '%' OR transliteration LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' ORDER BY isBuiltIn DESC, id ASC")
    fun searchPresetsStream(query: String): Flow<List<DhikrPresetEntity>>

    @Query("SELECT * FROM dhikr_presets WHERE id = :id")
    fun getPresetByIdStream(id: Int): Flow<DhikrPresetEntity?>

    @Query("SELECT * FROM dhikr_presets WHERE id = :id")
    fun getPresetById(id: Int): DhikrPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPreset(preset: DhikrPresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPresets(presets: List<DhikrPresetEntity>)

    @Update
    fun updatePreset(preset: DhikrPresetEntity): Int

    @Delete
    fun deletePreset(preset: DhikrPresetEntity): Int
}
