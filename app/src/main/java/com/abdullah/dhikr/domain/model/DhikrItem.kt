package com.abdullah.dhikr.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a Dhikr phrase (built-in preset or custom user entry) in Room database.
 */
@Entity(tableName = "dhikr_items")
data class DhikrItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val arabicText: String = "",
    val translation: String = "",
    val description: String? = null,
    val defaultTarget: Int = 33,
    val isCustom: Boolean = false,
    val count: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        val BUILT_IN_PRESETS = listOf(
            DhikrItem(
                id = 100,
                title = "Quick Counter",
                arabicText = "ذِكْر",
                translation = "General Dhikr (Free Counting)",
                description = "Count freely without selecting a specific Dhikr preset",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrItem(
                id = 101,
                title = "After-Salah Tasbeeh",
                arabicText = "أَذْكَارُ الصَّلَاةِ",
                translation = "33x SubhanAllah ➔ 33x Alhamdulillah ➔ 33x Allahu Akbar",
                description = "Post-Prayer Dhikr with automatic 3-step progression",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrItem(
                id = 1,
                title = "SubhanAllah",
                arabicText = "سُبْحَانَ ٱللَّٰهِ",
                translation = "Glory be to Allah",
                description = "Glory be to Allah in His perfection",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrItem(
                id = 2,
                title = "Alhamdulillah",
                arabicText = "ٱلْحَمْدُ لِلَّٰهِ",
                translation = "Praise be to Allah",
                description = "All praise and gratitude belong to Allah",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrItem(
                id = 3,
                title = "Allahu Akbar",
                arabicText = "ٱللَّٰهُ أَكْبَرُ",
                translation = "Allah is the Greatest",
                description = "Allah is greater than everything",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrItem(
                id = 4,
                title = "Astaghfirullah",
                arabicText = "أَسْتَغْفِرُ ٱللَّٰهَ",
                translation = "I seek forgiveness from Allah",
                description = "Seeking Allah's mercy and forgiveness",
                defaultTarget = 100,
                isCustom = false
            ),
            DhikrItem(
                id = 5,
                title = "La ilaha illa Allah",
                arabicText = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ",
                translation = "There is no god but Allah",
                description = "The central declaration of faith in Islam",
                defaultTarget = 100,
                isCustom = false
            ),
            DhikrItem(
                id = 6,
                title = "SubhanAllahi wa bihamdihi",
                arabicText = "سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِهِ",
                translation = "Glory and praise be to Allah",
                description = "Glorifying Allah and praising Him",
                defaultTarget = 100,
                isCustom = false
            )
        )
    }
}
