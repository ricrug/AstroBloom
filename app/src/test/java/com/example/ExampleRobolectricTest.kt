package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.SolarSystemData
import com.example.model.ChibiProfile
import com.example.ui.screens.PlanetDetailScreen
import com.example.ui.theme.AstroViaggioTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AstroViaggio", appName)
  }

  @Test
  fun `solar system dataset is complete`() {
    assertEquals(11, SolarSystemData.celestialBodies.size)
    assertTrue(SolarSystemData.celestialBodies.any { it.id == "moon" })
    assertTrue(SolarSystemData.curiosities.size >= 16)
    assertTrue(SolarSystemData.quizQuestions.size >= 18)
    assertTrue(SolarSystemData.celestialBodies.all { it.comicStories.size == 4 })
    assertTrue(SolarSystemData.celestialBodies.all { it.diameterKm > 0 })
  }

  @Test
  fun `planet detail proportional comparison shows true scale summary and targets`() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      AstroViaggioTheme {
        PlanetDetailScreen(
          planetId = "mars",
          chibiProfile = ChibiProfile(),
          userWeightKg = 35f,
          onUserWeightChange = {},
          onMarkExplored = {},
          onBack = {},
          onStartPlanetQuiz = {},
          onOpenLandingSimulator = {},
        )
      }
    }

    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("compare_scale_chip").performScrollTo().performClick()
    repeat(30) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("compare_scale_summary").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("1,9x più piccolo", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("compare_target_earth").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("compare_target_jupiter").performScrollTo().performClick()
    repeat(15) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithText("21x più piccolo", substring = true).assertIsDisplayed()
  }
}
