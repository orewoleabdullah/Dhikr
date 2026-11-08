package com.abdullah.dhikr.features.counter

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abdullah.dhikr.features.counter.presentation.CounterScreen
import com.abdullah.dhikr.ui.theme.DhikrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CounterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun counterScreen_displaysMainElements() {
        composeTestRule.setContent {
            DhikrTheme {
                CounterScreen()
            }
        }

        // Verify branding and title
        composeTestRule.onNodeWithText("Dhikr").assertIsDisplayed()
        composeTestRule.onNodeWithText("SubhanAllah").assertIsDisplayed()
        composeTestRule.onNodeWithText("TAP TO COUNT").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Reset counter").assertIsDisplayed()
    }

    @Test
    fun counterScreen_tapCountButton_incrementsDisplayedCount() {
        composeTestRule.setContent {
            DhikrTheme {
                CounterScreen()
            }
        }

        val countButton = composeTestRule.onNodeWithContentDescription(
            "Main counter button",
            substring = true
        )
        countButton.assertIsDisplayed()
        countButton.performClick()

        composeTestRule.onNodeWithText("1 / 33", substring = true).assertIsDisplayed()
    }

    @Test
    fun counterScreen_resetButton_triggersConfirmationDialog() {
        composeTestRule.setContent {
            DhikrTheme {
                CounterScreen()
            }
        }

        val resetButton = composeTestRule.onNodeWithContentDescription("Reset counter")
        resetButton.performClick()

        composeTestRule.onNodeWithText("Reset Counter?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reset").assertIsDisplayed()

        // Cancel reset
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Reset Counter?").assertDoesNotExist()
    }
}
