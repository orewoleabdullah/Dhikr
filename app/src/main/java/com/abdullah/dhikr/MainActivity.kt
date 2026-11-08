package com.abdullah.dhikr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.abdullah.dhikr.data.preferences.ThemeMode
import com.abdullah.dhikr.ui.DhikrApp
import com.abdullah.dhikr.ui.theme.DhikrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreferencesRepo = (application as DhikrApplication).container.userPreferencesRepository
            val themeMode by userPreferencesRepo.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val systemDark = isSystemInDarkTheme()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            DhikrTheme(darkTheme = darkTheme) {
                DhikrApp()
            }
        }
    }
}
