package com.abdullah.dhikr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a Dhikr preset (built-in or user-created custom preset).
 */
@Entity(tableName = "dhikr_presets")
data class DhikrPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val arabicText: String = "",
    val transliteration: String = "",
    val translation: String = "",
    val defaultTarget: Int = 33,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        val BUILT_IN_PRESETS = listOf(
            DhikrPresetEntity(
                id = 1,
                name = "SubhanAllah",
                arabicText = "سُبْحَانَ ٱللَّٰهِ",
                transliteration = "SubhanAllah",
                translation = "Glory be to Allah",
                defaultTarget = 33,
                isBuiltIn = true
            ),
            DhikrPresetEntity(
                id = 2,
                name = "Alhamdulillah",
                arabicText = "ٱلْحَمْدُ لِلَّٰهِ",
                transliteration = "Alhamdulillah",
                translation = "Praise be to Allah",
                defaultTarget = 33,
                isBuiltIn = true
            ),
            DhikrPresetEntity(
                id = 3,
                name = "Allahu Akbar",
                arabicText = "ٱللَّٰهُ أَكْبَرُ",
                transliteration = "Allahu Akbar",
                translation = "Allah is the Greatest",
                defaultTarget = 33,
                isBuiltIn = true
            ),
            DhikrPresetEntity(
                id = 4,
                name = "Astaghfirullah",
                arabicText = "أَسْتَغْفِرُ ٱللَّٰهَ",
                transliteration = "Astaghfirullah",
                translation = "I seek forgiveness from Allah",
                defaultTarget = 100,
                isBuiltIn = true
            ),
            DhikrPresetEntity(
                id = 5,
                name = "La ilaha illa Allah",
                arabicText = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ",
                transliteration = "La ilaha illa Allah",
                translation = "There is no god but Allah",
                defaultTarget = 100,
                isBuiltIn = true
            ),
            DhikrPresetEntity(
                id = 6,
                name = "SubhanAllahi wa bihamdihi",
                arabicText = "سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِهِ",
                transliteration = "SubhanAllahi wa bihamdihi",
                translation = "Glory and praise be to Allah",
                defaultTarget = 100,
                isBuiltIn = true
            )
        )
    }
}
