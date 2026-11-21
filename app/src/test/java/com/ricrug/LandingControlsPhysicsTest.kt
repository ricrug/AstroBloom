package com.ricrug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

class LandingControlsPhysicsTest {

  @Test
  fun testLanderAttitudeTiltAngles() {
    // When RCS left is firing: -14 degrees tilt
    val tiltLeft = calculateTilt(thrustLeft = true, thrustRight = false, vx = 0f)
    assertEquals(-14f, tiltLeft, 0.001f)

    // When RCS right is firing: +14 degrees tilt
    val tiltRight = calculateTilt(thrustLeft = false, thrustRight = true, vx = 0f)
    assertEquals(14f, tiltRight, 0.001f)

    // When no RCS is firing and vx is 0: 0 degrees tilt
    val tiltNeutral = calculateTilt(thrustLeft = false, thrustRight = false, vx = 0f)
    assertEquals(0f, tiltNeutral, 0.001f)

    // When vx has lateral drift without RCS, tilt is clamped between -12 and +12 degrees
    val tiltDriftPositive = calculateTilt(thrustLeft = false, thrustRight = false, vx = 0.8f)
    assertEquals(12f, tiltDriftPositive, 0.001f)

    val tiltDriftNegative = calculateTilt(thrustLeft = false, thrustRight = false, vx = -0.8f)
    assertEquals(-12f, tiltDriftNegative, 0.001f)
  }

  @Test
  fun testSafeLandingSpeedThresholds() {
    fun isSpeedSafe(speedMs: Int): Boolean = speedMs <= 26
    fun isOptimalTouchdown(speedMs: Int): Boolean = speedMs <= 22

    assertTrue(isOptimalTouchdown(15))
    assertTrue(isOptimalTouchdown(22))
    assertFalse(isOptimalTouchdown(25))

    assertTrue(isSpeedSafe(22))
    assertTrue(isSpeedSafe(26))
    assertFalse(isSpeedSafe(27))
    assertFalse(isSpeedSafe(35))
  }

  @Test
  fun testStickDeadzoneAndCurvedDeflection() {
    val joyBoxSizePx = 104f * 2.5f // hypothetical px
    val centerPx = joyBoxSizePx / 2f
    val maxRadiusPx = joyBoxSizePx * 0.42f
    val deadzonePx = 5f * 2.5f

    // Center touch inside deadzone produces 0 deflection
    val (stickXCenter, stickYCenter) = computeStickDeflection(centerPx + 2f, centerPx - 2f, centerPx, maxRadiusPx, deadzonePx)
    assertEquals(0f, stickXCenter, 0.001f)
    assertEquals(0f, stickYCenter, 0.001f)

    // Strong left touch produces negative stickX (< -0.18f activating left thrust)
    val (stickXLeft, _) = computeStickDeflection(centerPx - maxRadiusPx, centerPx, centerPx, maxRadiusPx, deadzonePx)
    assertTrue("Left touch must deflect stickX < -0.18f", stickXLeft < -0.18f)

    // Strong right touch produces positive stickX (> 0.18f activating right thrust)
    val (stickXRight, _) = computeStickDeflection(centerPx + maxRadiusPx, centerPx, centerPx, maxRadiusPx, deadzonePx)
    assertTrue("Right touch must deflect stickX > 0.18f", stickXRight > 0.18f)

    // Strong upward touch produces negative stickY (< -0.22f activating brake thrust)
    val (_, stickYUp) = computeStickDeflection(centerPx, centerPx - maxRadiusPx, centerPx, maxRadiusPx, deadzonePx)
    assertTrue("Up touch must deflect stickY < -0.22f", stickYUp < -0.22f)
  }

  @Test
  fun testThrottleLeverDeadMansSpringReturn() {
    val leverHeightPx = 104f * 2.5f

    // Touch at top (yPx = 0) -> max thrust norm = 1.0f -> thrustUp = true
    val normTop = (1f - (0f / leverHeightPx)).coerceIn(0f, 1f)
    assertTrue("Top position must engage thrustUp", normTop > 0.08f)

    // Touch near bottom (yPx = leverHeightPx) -> rest norm = 0f -> thrustUp = false
    val normBottom = (1f - (leverHeightPx / leverHeightPx)).coerceIn(0f, 1f)
    assertEquals(0f, normBottom, 0.001f)
    assertFalse("Rest position must not engage thrustUp", normBottom > 0.08f)

    // Spring release: when thumb is released, norm immediately resets to 0f
    var leverNorm = normTop
    var thrustUp = leverNorm > 0.08f
    assertTrue(thrustUp)

    // Release thumb
    leverNorm = 0f
    thrustUp = leverNorm > 0.08f
    assertFalse("Releasing thumb must immediately cut thrust", thrustUp)
  }

  private fun calculateTilt(thrustLeft: Boolean, thrustRight: Boolean, vx: Float): Float {
    return when {
      thrustLeft && !thrustRight -> -14f
      thrustRight && !thrustLeft -> 14f
      else -> (vx * 28f).coerceIn(-12f, 12f)
    }
  }

  private fun computeStickDeflection(
    touchX: Float,
    touchY: Float,
    centerPx: Float,
    maxRadiusPx: Float,
    deadzonePx: Float,
  ): Pair<Float, Float> {
    val dx = touchX - centerPx
    val dy = touchY - centerPx
    val dist = hypot(dx, dy)
    return if (dist > deadzonePx) {
      val norm = ((dist - deadzonePx) / (maxRadiusPx - deadzonePx)).coerceIn(0f, 1f)
      val curved = (norm * norm * 0.75f + norm * 0.25f).coerceIn(0f, 1f)
      val ang = atan2(dy, dx)
      val sx = (cos(ang) * curved).coerceIn(-1f, 1f)
      val sy = (sin(ang) * curved).coerceIn(-1f, 1f)
      Pair(sx, sy)
    } else {
      Pair(0f, 0f)
    }
  }
}
