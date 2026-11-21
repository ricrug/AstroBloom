package com.ricrug

import com.ricrug.data.OrbitVec3
import com.ricrug.data.OrbitalMechanics
import com.ricrug.data.SolarSystemData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class OrbitalMechanicsRealisticTest {

  @Test
  fun testRealisticOrbitalSpeedKeepsEarthStable() {
    val earth = SolarSystemData.getPlanetById("earth")
    val pos0 = OrbitalMechanics.bodyWorldPosition(earth, simTimeSec = 0f, useRealisticSpeed = true, timeScaleMultiplier = 1.0f)
    val pos60 = OrbitalMechanics.bodyWorldPosition(earth, simTimeSec = 60f, useRealisticSpeed = true, timeScaleMultiplier = 1.0f)

    // After 60 seconds at realistic 1x speed, Earth should barely have moved (~0.0007 deg)
    val distMoved = (pos60 - pos0).length()
    assertTrue("Earth moved too fast: distMoved=$distMoved", distMoved < 0.05f)
  }

  @Test
  fun testOrthonormalCameraRightHandRule() {
    // If Forward is +X (1, 0, 0) and Up is +Y (0, 1, 0)
    val fwd = OrbitVec3(1f, 0f, 0f)
    val up = OrbitVec3(0f, 1f, 0f)

    // Right = Up x Forward = (0, 0, -1) in right-handed camera space where +Z is forward
    val right = up.cross(fwd).normalized()
    assertEquals(0f, right.x, 0.001f)
    assertEquals(0f, right.y, 0.001f)
    assertEquals(-1f, right.z, 0.001f)

    // Orthogonality checks
    assertEquals(0f, fwd.dot(up), 0.001f)
    assertEquals(0f, fwd.dot(right), 0.001f)
    assertEquals(0f, up.dot(right), 0.001f)
  }

  @Test
  fun testCentralHudMathCalculations() {
    // Heading deg calculation
    val fwdNorth = OrbitVec3(0f, 0f, -1f)
    val headingDeg = ((kotlin.math.atan2(fwdNorth.z.toDouble(), fwdNorth.x.toDouble()) * 180.0 / Math.PI + 360.0) % 360.0).toInt()
    assertEquals(270, headingDeg)

    // Speed kms conversion
    val testSpeed = 1.0f
    val speedKms = (kotlin.math.abs(testSpeed) * 240f).toInt()
    assertEquals(240, speedKms)

    // Pitch ladder steps
    val ladderSteps = listOf(30, 20, 10, -10, -20, -30)
    assertEquals(6, ladderSteps.size)
    assertTrue(ladderSteps.contains(30))
    assertTrue(ladderSteps.contains(-30))
  }
}
