package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.OrbitalMechanics
import com.example.data.SolarSystemData
import com.example.model.ChibiProfile
import com.example.ui.screens.FirstPersonSimulatorScreen
import com.example.ui.screens.FreeExploreScreen
import com.example.ui.screens.LabsScreen
import com.example.ui.screens.PlanetDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.labs.CosmicBathtubLab
import com.example.ui.screens.labs.OrbitsAndSeasonsLab
import com.example.ui.screens.labs.SpaceThermometerLab
import com.example.ui.theme.AstroViaggioTheme
import kotlin.math.abs
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

  @Test
  fun `labs screen switches across interactive space labs`() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      AstroViaggioTheme {
        LabsScreen(
          chibiProfile = ChibiProfile(),
          userWeightKg = 35f,
          userAgeYears = 10f,
          onUserWeightChange = {},
          onUserAgeChange = {},
        )
      }
    }

    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("lab_panel_jump").assertIsDisplayed()

    composeTestRule.onNodeWithTag("lab_chip_moon").performClick()
    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("lab_panel_moon").assertIsDisplayed()

    composeTestRule.onNodeWithTag("lab_chip_light").performClick()
    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("lab_panel_light").assertIsDisplayed()

    composeTestRule.onNodeWithTag("labs_show_all_toggle").performClick()
    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("lab_panel_jump").assertExists()
  }

  @Test
  fun `density orbits and weather labs render interactive states`() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      AstroViaggioTheme {
        androidx.compose.foundation.layout.Column {
          CosmicBathtubLab(chibiProfile = ChibiProfile())
          OrbitsAndSeasonsLab(chibiProfile = ChibiProfile())
          SpaceThermometerLab(chibiProfile = ChibiProfile())
        }
      }
    }

    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("lab_bathtub_planet_saturn").performClick()
    repeat(10) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithText("Galleggia!", substring = true, ignoreCase = true).assertExists()

    composeTestRule.onNodeWithTag("lab_orbits_tab_seasons").performClick()
    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithText("Estate in Italia", substring = true).assertExists()

    composeTestRule.onNodeWithText("+465°C").assertExists()
  }

  @Test
  fun `splash screen displays school logo title credits and completes`() {
    composeTestRule.mainClock.autoAdvance = false
    var finished = false
    composeTestRule.setContent {
      AstroViaggioTheme {
        SplashScreen(onSplashFinished = { finished = true })
      }
    }

    repeat(10) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("splash_school_logo").assertExists()
    composeTestRule.onNodeWithTag("splash_school_name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("splash_app_name").assertIsDisplayed()
    composeTestRule.onNodeWithText("Credits: AstroBloom", substring = true).assertIsDisplayed()

    composeTestRule.mainClock.advanceTimeBy(3700)
    assertTrue(finished)
  }

  @Test
  fun `orbital mechanics models distinct ecliptic inclinations`() {
    val earth = SolarSystemData.getPlanetById("earth")
    val mercury = SolarSystemData.getPlanetById("mercury")
    val pluto = SolarSystemData.getPlanetById("pluto")

    assertEquals(0.0f, OrbitalMechanics.elementsFor("earth").inclinationDeg, 0.01f)
    assertEquals(7.0f, OrbitalMechanics.elementsFor("mercury").inclinationDeg, 0.01f)
    assertEquals(17.2f, OrbitalMechanics.elementsFor("pluto").inclinationDeg, 0.01f)

    val earthMaxY = OrbitalMechanics.sampleOrbitLoop(earth, 0f).maxOf { abs(it.y) }
    val mercuryMaxY = OrbitalMechanics.sampleOrbitLoop(mercury, 0f).maxOf { abs(it.y) }
    val plutoMaxY = OrbitalMechanics.sampleOrbitLoop(pluto, 0f).maxOf { abs(it.y) }

    assertTrue(earthMaxY < 0.01f)
    assertTrue(mercuryMaxY > 5f)
    assertTrue(plutoMaxY > 100f)
  }

  @Test
  fun `free explore screen launches first person cockpit simulator`() {
    composeTestRule.mainClock.autoAdvance = false
    var launchedCockpit = false
    composeTestRule.setContent {
      AstroViaggioTheme {
        FreeExploreScreen(
          chibiProfile = ChibiProfile(),
          onOpenPlanetDetail = {},
          onOpenLandingSimulator = {},
          onOpenFirstPersonCockpit = { launchedCockpit = true },
        )
      }
    }

    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("launch_cockpit_simulator_button").assertIsDisplayed().performClick()
    assertTrue(launchedCockpit)
  }

  @Test
  @Config(sdk = [36], qualifiers = "w891dp-h411dp")
  fun `first person flight simulator runs in landscape and exits via cockpit button`() {
    composeTestRule.mainClock.autoAdvance = false
    var exited = false
    composeTestRule.setContent {
      AstroViaggioTheme {
        FirstPersonSimulatorScreen(
          chibiProfile = ChibiProfile(),
          onOpenPlanetDetail = {},
          onOpenLandingSimulator = {},
          onExitSimulator = { exited = true },
        )
      }
    }

    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("first_person_flight_sim").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cockpit_exit_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cockpit_flight_stick").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cockpit_throttle_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cockpit_warp_chip").performClick()
    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("cockpit_stop_button").performClick()
    repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
    composeTestRule.onNodeWithTag("cockpit_speed_readout").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cockpit_exit_button").performClick()
    assertTrue(exited)
  }
}
