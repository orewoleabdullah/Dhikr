package com.abdullah.dhikr.features.presets

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abdullah.dhikr.features.presets.presentation.PresetsScreen
import com.abdullah.dhikr.ui.theme.DhikrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PresetsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun presetsScreen_displaysTitleAndBuiltInPresets() {
        composeTestRule.setContent {
            DhikrTheme {
                PresetsScreen()
            }
        }

        composeTestRule.onNodeWithText("Dhikr Presets").assertIsDisplayed()
        composeTestRule.onNodeWithText("SubhanAllah").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alhamdulillah").assertIsDisplayed()
        composeTestRule.onNodeWithText("Allahu Akbar").assertIsDisplayed()
    }

    @Test
    fun presetsScreen_clickAddDhikrFAB_opensAddCustomDhikrDialog() {
        composeTestRule.setContent {
            DhikrTheme {
                PresetsScreen()
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Custom Dhikr").performClick()

        composeTestRule.onNodeWithText("Add Custom Dhikr").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Add Custom Dhikr").assertDoesNotExist()
    }
}
