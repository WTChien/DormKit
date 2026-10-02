package com.dormkit.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun quickInventoryThenBottomHomeReturnsToDashboard() {
        composeRule.onNodeWithTag("quick_inventory").performClick()
        composeRule.onNodeWithText("搜尋物品").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_home").performClick()
        composeRule.onNodeWithText("快速功能").assertIsDisplayed()
    }

    @Test
    fun quickPackingThenBottomHomeReturnsToDashboard() {
        composeRule.onNodeWithTag("quick_packing").performClick()
        composeRule.onNodeWithText("帶回家的").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_home").performClick()
        composeRule.onNodeWithText("快速功能").assertIsDisplayed()
    }
}
