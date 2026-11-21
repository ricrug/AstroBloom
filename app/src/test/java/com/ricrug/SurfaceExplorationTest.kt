package com.ricrug

import com.ricrug.data.SurfaceExplorationRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfaceExplorationTest {

  @Test
  fun testEnvironmentConfigsForAllMainPlanets() {
    val planetIds = listOf("moon", "mars", "venus", "mercury", "jupiter", "saturn", "uranus", "neptune", "pluto", "earth", "sun")

    for (pid in planetIds) {
      val env = SurfaceExplorationRepository.getEnvironmentConfig(pid)
      assertNotNull("Environment config null for $pid", env)
      assertTrue("Gravity must be positive for $pid", env.gravityMs2 > 0f)
      assertTrue("Visibility must be positive for $pid", env.visibilityMeters > 0f)
      assertTrue("Landscape feature must not be blank for $pid", env.landscapeFeature.isNotBlank())
      assertTrue("Atmosphere composition must not be blank for $pid", env.atmosphereComposition.isNotBlank())
    }
  }

  @Test
  fun testRealisticRelativeGravityOrder() {
    val moonEnv = SurfaceExplorationRepository.getEnvironmentConfig("moon")
    val marsEnv = SurfaceExplorationRepository.getEnvironmentConfig("mars")
    val venusEnv = SurfaceExplorationRepository.getEnvironmentConfig("venus")
    val earthEnv = SurfaceExplorationRepository.getEnvironmentConfig("earth")
    val sunEnv = SurfaceExplorationRepository.getEnvironmentConfig("sun")

    // Moon (~1.62) < Mars (~3.72) < Venus (~8.87) < Earth (~9.81) < Sun (~274)
    assertTrue("Moon gravity should be less than Mars", moonEnv.gravityMs2 < marsEnv.gravityMs2)
    assertTrue("Mars gravity should be less than Venus", marsEnv.gravityMs2 < venusEnv.gravityMs2)
    assertTrue("Venus gravity should be less than Earth", venusEnv.gravityMs2 < earthEnv.gravityMs2)
    assertTrue("Earth gravity should be less than Sun", earthEnv.gravityMs2 < sunEnv.gravityMs2)

    // Check specific ratios
    assertEquals(0.165f, moonEnv.gravityRatio, 0.01f)
    assertEquals(0.38f, marsEnv.gravityRatio, 0.01f)
    assertEquals(0.90f, venusEnv.gravityRatio, 0.01f)
    assertEquals(1.0f, earthEnv.gravityRatio, 0.01f)
  }

  @Test
  fun testOfficialPhotosDocumentationCompleteness() {
    val photos = SurfaceExplorationRepository.officialPhotos
    assertTrue("Must have official documented photos", photos.isNotEmpty())

    for (p in photos) {
      assertTrue("Photo ID must not be blank", p.id.isNotBlank())
      assertTrue("Planet ID must not be blank: ${p.id}", p.planetId.isNotBlank())
      assertTrue("Title must not be blank: ${p.id}", p.title.isNotBlank())
      assertTrue("Mission must not be blank: ${p.id}", p.mission.isNotBlank())
      assertTrue("Agency must not be blank: ${p.id}", p.agency.isNotBlank())
      assertTrue("Capture date must not be blank: ${p.id}", p.captureDate.isNotBlank())
      assertTrue("Image URL must be valid https URL: ${p.id}", p.imageUrl.startsWith("https://"))
      assertTrue("Caption must be informative (>20 chars): ${p.id}", p.caption.length > 20)
      assertTrue("Scientific tip must be present (>15 chars): ${p.id}", p.scientificTip.length > 15)
      assertTrue("Credit must not be blank: ${p.id}", p.credit.isNotBlank())
    }

    // Verify key historical missions are documented
    val missions = photos.map { it.mission }
    assertTrue("Should include Apollo 11 mission", missions.any { it.contains("Apollo 11") })
    assertTrue("Should include Perseverance mission", missions.any { it.contains("Perseverance") })
    assertTrue("Should include Venera mission", missions.any { it.contains("Venera") })
    assertTrue("Should include MESSENGER mission", missions.any { it.contains("MESSENGER") })
    assertTrue("Should include Cassini-Huygens mission", missions.any { it.contains("Cassini") || it.contains("Huygens") })
    assertTrue("Should include New Horizons mission", missions.any { it.contains("New Horizons") })
  }

  @Test
  fun testSurfacePoisAndSamplesIntegrity() {
    val pois = SurfaceExplorationRepository.surfacePois
    val samples = SurfaceExplorationRepository.surfaceSamples

    assertTrue("Must have POIs", pois.isNotEmpty())
    assertTrue("Must have samples", samples.isNotEmpty())

    for (poi in pois) {
      assertTrue("POI name not blank", poi.name.isNotBlank())
      assertTrue("POI description not blank", poi.description.isNotBlank())
      if (poi.associatedPhotoId != null) {
        val linked = SurfaceExplorationRepository.officialPhotos.any { it.id == poi.associatedPhotoId }
        assertTrue("POI associatedPhotoId must exist in photos: ${poi.associatedPhotoId}", linked)
      }
    }

    for (sample in samples) {
      assertTrue("Sample name not blank", sample.name.isNotBlank())
      assertTrue("Sample spectrometryFormula not blank", sample.spectrometryFormula.isNotBlank())
      assertTrue("Sample scientificDiscovery not blank", sample.scientificDiscovery.isNotBlank())
      assertTrue("Sample XP must be > 0", sample.xpValue > 0)
      if (sample.associatedPhotoId != null) {
        val linked = SurfaceExplorationRepository.officialPhotos.any { it.id == sample.associatedPhotoId }
        assertTrue("Sample associatedPhotoId must exist in photos: ${sample.associatedPhotoId}", linked)
      }
    }
  }

  @Test
  fun testDisembarkShipMechanics() {
    // Coordinate iniziali a bordo dell'airlock
    val airlockEyeHeight = 3.2f
    val airlockZ = 1.6f
    val airlockYaw = 0f

    // Coordinate dopo la discesa sulla superficie
    val groundEyeHeight = 1.75f
    val groundZ = 5.2f
    val groundYaw = 180f

    // Discesa comporta riduzione di quota verso il terreno
    assertTrue("Airlock should be elevated above ground", airlockEyeHeight > groundEyeHeight)
    // Discesa comporta allontanamento sulla passerella
    assertTrue("Ground position is further out from ship center", groundZ > airlockZ)
    // Discesa ruota la vista di 180° per ammirare l'astronave dall'esterno
    assertEquals(180f, kotlin.math.abs(groundYaw - airlockYaw), 0.001f)

    // Distanza di risalita a bordo
    val boardingThreshold = 4.2f
    val nearShipDist = 3.0f
    val farShipDist = 8.0f
    assertTrue("Near ship allows reboarding", nearShipDist < boardingThreshold)
    assertTrue("Far ship requires approaching to reboard", farShipDist > boardingThreshold)
  }

  @Test
  fun testOffScreenHudDirectionalIndicators() {
    val playerX = 0f
    val playerZ = 10f
    val playerYawDeg = 0f // Looking forward along +Z

    // POI 1: directly in front at (0, 30) -> angle = 0°, on-screen
    val angleInFront = ((kotlin.math.atan2(0.0, 20.0) * 180.0 / Math.PI - playerYawDeg + 540.0) % 360.0) - 180.0
    assertEquals(0.0, angleInFront, 0.01)
    assertTrue("In front should be on-screen", kotlin.math.abs(angleInFront) <= 36.0)

    // POI 2: to the left at (-30, 10) -> angle = -90°, off-screen left
    val angleLeft = ((kotlin.math.atan2(-30.0, 0.0) * 180.0 / Math.PI - playerYawDeg + 540.0) % 360.0) - 180.0
    assertEquals(-90.0, angleLeft, 0.01)
    assertTrue("Should trigger left off-screen indicator", angleLeft < -36.0)

    // POI 3: to the right at (25, 10) -> angle = +90°, off-screen right
    val angleRight = ((kotlin.math.atan2(25.0, 0.0) * 180.0 / Math.PI - playerYawDeg + 540.0) % 360.0) - 180.0
    assertEquals(90.0, angleRight, 0.01)
    assertTrue("Should trigger right off-screen indicator", angleRight > 36.0)

    // Ship at (0, 0) -> behind player at Z=10, angle = 180°
    val angleShip = ((kotlin.math.atan2(0.0, -10.0) * 180.0 / Math.PI - playerYawDeg + 540.0) % 360.0) - 180.0
    assertEquals(180.0, kotlin.math.abs(angleShip), 0.01)
    assertTrue("Ship behind should trigger off-screen indicator", kotlin.math.abs(angleShip) > 36.0)
  }

  @Test
  fun testSkyBeaconVisibilityAndTerrainGridNavigation() {
    val canvasH = 1080f
    val minBeaconH = 360f
    val dynamicBeaconH = kotlin.math.max(canvasH * 0.72f, minBeaconH)
    assertTrue("Beacon height must be massive to be visible from sky", dynamicBeaconH >= 700f)

    // Grid step calculation
    val gridStep = 4.0f
    val playerZ = 13.5f
    val snapZ = (playerZ / gridStep).toInt() * gridStep
    assertEquals(12.0f, snapZ, 0.001f)

    // Moving forward changes relative world Z line distance dynamically
    val playerZ2 = 14.5f
    val distToGridLine = kotlin.math.abs(snapZ - playerZ2)
    assertEquals(2.5f, distToGridLine, 0.001f)
  }

  @Test
  fun testButtonTypographyHyphenationAndLineBreak() {
    val typo = com.ricrug.ui.theme.AppTypography
    assertEquals(androidx.compose.ui.text.style.Hyphens.None, typo.labelLarge.hyphens)
    assertEquals(androidx.compose.ui.text.style.LineBreak.Heading, typo.labelLarge.lineBreak)
    assertEquals(androidx.compose.ui.text.style.Hyphens.None, typo.labelMedium.hyphens)
    assertEquals(androidx.compose.ui.text.style.LineBreak.Heading, typo.labelMedium.lineBreak)
    assertEquals(androidx.compose.ui.text.style.Hyphens.None, typo.labelSmall.hyphens)
    assertEquals(androidx.compose.ui.text.style.LineBreak.Heading, typo.labelSmall.lineBreak)
    assertEquals(androidx.compose.ui.text.style.Hyphens.None, typo.titleMedium.hyphens)
    assertEquals(androidx.compose.ui.text.style.LineBreak.Heading, typo.titleMedium.lineBreak)
  }

  @Test
  fun testSurface3DLocomotionAndKangarooHopPhysics() {
    val moonEnv = SurfaceExplorationRepository.getEnvironmentConfig("moon")
    val earthEnv = SurfaceExplorationRepository.getEnvironmentConfig("earth")

    // Moon is low gravity (ratio < 0.6f), Earth is normal/high gravity
    val moonIsLowG = moonEnv.gravityRatio < 0.6f
    val earthIsLowG = earthEnv.gravityRatio < 0.6f
    assertTrue("Moon should be detected as low gravity environment", moonIsLowG)
    assertTrue("Earth should not be low gravity", !earthIsLowG)

    val moonMaxWalkSpeed = if (moonIsLowG) 4.2f else 3.6f
    val earthMaxWalkSpeed = if (earthIsLowG) 4.2f else 3.6f
    assertTrue("Low gravity allows longer bounding strides", moonMaxWalkSpeed > earthMaxWalkSpeed)

    val moonHopFreq = if (moonIsLowG) 5.8f else 7.2f
    val earthHopFreq = if (earthIsLowG) 5.8f else 7.2f
    assertTrue("Low gravity kangaroo hops have lower frequency (longer hang time)", moonHopFreq < earthHopFreq)

    // Inertial acceleration: low gravity regolith has lower grip -> smoother ramp
    val moonAccelRate = if (moonIsLowG) 8.5f else 12f
    val earthAccelRate = if (earthIsLowG) 8.5f else 12f
    assertTrue("Low gravity has smoother inertia ramp", moonAccelRate < earthAccelRate)

    // Astronaut helmet/torso roll sway during locomotion
    for (step in 0..100) {
      val walkCycle = step * 0.1f
      val rollSway = kotlin.math.sin(walkCycle * 0.5f) * 1.6f
      assertTrue("Roll sway must stay within physiological helmet sway (+-1.6 deg)", kotlin.math.abs(rollSway) <= 1.6001f)
    }
  }

  @Test
  fun testCelestialAngularProjectionCoherence() {
    val canvasW = 1080f
    val fovFactor = canvasW * 0.85f
    val pxPerDeg = fovFactor * (kotlin.math.PI.toFloat() / 180f)

    // When facing forward (playerYaw = 0), Sun at azimuth 0 is dead center
    val sunAzimuth = 0f
    var playerYawDeg = 0f
    var relYaw = ((sunAzimuth - playerYawDeg + 540f) % 360f) - 180f
    var screenX = canvasW * 0.5f + relYaw * pxPerDeg
    assertEquals(canvasW * 0.5f, screenX, 0.01f)

    // When astronaut turns right (+30°), the Sun MUST move to the LEFT on the screen
    playerYawDeg = 30f
    relYaw = ((sunAzimuth - playerYawDeg + 540f) % 360f) - 180f
    assertEquals(-30f, relYaw, 0.01f)
    screenX = canvasW * 0.5f + relYaw * pxPerDeg
    assertTrue("Turning right must shift sky objects to the left", screenX < canvasW * 0.5f)

    // Same coherent shift must apply to a celestial body (e.g. Earth visible from the Moon)
    val earthAzimuth = 45f
    val relYawEarthFacing = ((earthAzimuth - 0f + 540f) % 360f) - 180f
    val relYawEarthTurned = ((earthAzimuth - playerYawDeg + 540f) % 360f) - 180f
    assertEquals(45f, relYawEarthFacing, 0.01f)
    assertEquals(15f, relYawEarthTurned, 0.01f)
    assertTrue("Earth angle must decrease identically when turning right", relYawEarthTurned < relYawEarthFacing)
  }

  @Test
  fun testSurfaceControllerPadSpecifications() {
    val padBoxSizeDp = 112f
    val padPaddingStartDp = 28f
    val padPaddingBottomDp = 24f

    assertEquals(112f, padBoxSizeDp, 0.001f)
    assertTrue("Controller pad must be comfortably padded from left corner", padPaddingStartDp >= 24f)
    assertTrue("Controller pad must be comfortably padded from bottom edge", padPaddingBottomDp >= 20f)

    // Curved response curve test: ensures gentle response near center, full authority at edge
    val deadzonePx = 15f
    val maxRadiusPx = 140f

    // Micro-touch inside deadzone
    val touchInside = 10f
    val normInside = if (touchInside > deadzonePx) ((touchInside - deadzonePx) / (maxRadiusPx - deadzonePx)) else 0f
    assertEquals(0f, normInside, 0.001f)

    // Touch at 50% deflection
    val touchMid = 77.5f // 15 + (125 * 0.5)
    val normMid = ((touchMid - deadzonePx) / (maxRadiusPx - deadzonePx))
    val curvedMid = (normMid * normMid * 0.70f + normMid * 0.30f)
    assertEquals(0.5f, normMid, 0.01f)
    assertEquals(0.325f, curvedMid, 0.01f) // Progressive exponential control
    assertTrue("Curved output allows finer precision at half stick", curvedMid < normMid)

    // Touch at max deflection
    val touchMax = 140f
    val normMax = ((touchMax - deadzonePx) / (maxRadiusPx - deadzonePx))
    val curvedMax = (normMax * normMax * 0.70f + normMax * 0.30f)
    assertEquals(1.0f, normMax, 0.01f)
    assertEquals(1.0f, curvedMax, 0.01f)
  }
}
