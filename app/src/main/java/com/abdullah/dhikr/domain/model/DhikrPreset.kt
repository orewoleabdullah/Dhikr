package com.abdullah.dhikr.domain.model

/**
 * Data class representing an individual step in a sequential Dhikr chain (e.g. After-Salah Tasbeeh).
 */
data class AfterSalahStep(
    val title: String,
    val arabicText: String,
    val translation: String,
    val target: Int = 33
)

object AfterSalahChain {
    val STEPS = listOf(
        AfterSalahStep("SubhanAllah", "سُبْحَانَ ٱللَّٰهِ", "Glory be to Allah", 33),
        AfterSalahStep("Alhamdulillah", "ٱلْحَمْدُ لِلَّٰهِ", "Praise be to Allah", 33),
        AfterSalahStep("Allahu Akbar", "ٱللَّٰهُ أَكْبَرُ", "Allah is the Greatest", 33)
    )
}

/**
 * Data class representing a preset or custom Dhikr for UI rendering and preferences.
 */
data class DhikrPreset(
    val id: String,
    val title: String,
    val arabicText: String,
    val translation: String,
    val description: String? = null,
    val defaultTarget: Int = 33,
    val isCustom: Boolean = false,
    val isAfterSalahChain: Boolean = false
) {
    companion object {
        val FREE_COUNTER = DhikrPreset(
            id = "free_counter",
            title = "Quick Counter",
            arabicText = "ذِكْر",
            translation = "General Dhikr (Free Counting)",
            description = "Count freely without selecting a specific Dhikr preset",
            defaultTarget = 33,
            isCustom = false
        )

        val AFTER_SALAH_PRESET = DhikrPreset(
            id = "after_salah",
            title = "After-Salah Tasbeeh",
            arabicText = "أَذْكَارُ الصَّلَاةِ",
            translation = "33x SubhanAllah ➔ 33x Alhamdulillah ➔ 33x Allahu Akbar",
            description = "Post-Prayer Dhikr with automatic progression across 3 steps",
            defaultTarget = 33,
            isCustom = false,
            isAfterSalahChain = true
        )

        val PRESETS = listOf(
            FREE_COUNTER,
            AFTER_SALAH_PRESET,
            DhikrPreset(
                id = "subhanallah",
                title = "SubhanAllah",
                arabicText = "سُبْحَانَ ٱللَّٰهِ",
                translation = "Glory be to Allah",
                description = "Glory be to Allah in His perfection",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrPreset(
                id = "alhamdulillah",
                title = "Alhamdulillah",
                arabicText = "ٱلْحَمْدُ لِلَّٰهِ",
                translation = "Praise be to Allah",
                description = "All praise and gratitude belong to Allah",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrPreset(
                id = "allahu_akbar",
                title = "Allahu Akbar",
                arabicText = "ٱللَّٰهُ أَكْبَرُ",
                translation = "Allah is the Greatest",
                description = "Allah is greater than everything",
                defaultTarget = 33,
                isCustom = false
            ),
            DhikrPreset(
                id = "astaghfirullah",
                title = "Astaghfirullah",
                arabicText = "أَسْتَغْفِرُ ٱللَّٰهَ",
                translation = "I seek forgiveness from Allah",
                description = "Seeking Allah's mercy and forgiveness",
                defaultTarget = 100,
                isCustom = false
            ),
            DhikrPreset(
                id = "lailahaillallah",
                title = "La ilaha illa Allah",
                arabicText = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ",
                translation = "There is no god but Allah",
                description = "The central declaration of faith in Islam",
                defaultTarget = 100,
                isCustom = false
            ),
            DhikrPreset(
                id = "subhanallahi_wa_bihamdihi",
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
