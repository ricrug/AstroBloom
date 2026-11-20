package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SolarSystemData
import com.example.model.CelestialBody
import com.example.model.PlanetSurfaceStyle
import com.example.ui.theme.CosmicAmber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.StarlightWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class SphereVec3(val x: Float, val y: Float, val z: Float)

private fun latLonToVec3(latDeg: Float, lonDeg: Float): SphereVec3 {
  val lat = Math.toRadians(latDeg.toDouble())
  val lon = Math.toRadians(lonDeg.toDouble())
  val cosLat = cos(lat)
  return SphereVec3(
    x = (cosLat * sin(lon)).toFloat(),
    y = (-sin(lat)).toFloat(),
    z = (cosLat * cos(lon)).toFloat(),
  )
}

private fun rotateVec3(v: SphereVec3, yawDeg: Float, pitchDeg: Float, tiltDeg: Float = 0f): SphereVec3 {
  val yaw = Math.toRadians(yawDeg.toDouble())
  val cy = cos(yaw).toFloat()
  val sy = sin(yaw).toFloat()
  val x1 = v.x * cy + v.z * sy
  val y1 = v.y
  val z1 = -v.x * sy + v.z * cy

  val tilt = Math.toRadians(tiltDeg.toDouble())
  val ct = cos(tilt).toFloat()
  val st = sin(tilt).toFloat()
  val x2 = x1 * ct - y1 * st
  val y2 = x1 * st + y1 * ct
  val z2 = z1

  val pitch = Math.toRadians(pitchDeg.toDouble())
  val cp = cos(pitch).toFloat()
  val sp = sin(pitch).toFloat()
  val y3 = y2 * cp - z2 * sp
  val z3 = y2 * sp + z2 * cp

  return SphereVec3(x2, y3, z3)
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
  start + (stop - start) * fraction

@Composable
fun Interactive3DPlanetCanvas(
  body: CelestialBody,
  autoSpin: Boolean,
  showAxialTilt: Boolean,
  compareWithPlanetId: String?,
  externalZoom: Float,
  onZoomChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  var yawDeg by remember(body.id) { mutableFloatStateOf(20f) }
  var pitchDeg by remember(body.id) { mutableFloatStateOf(16f) }
  var moonPhase by remember(body.id) { mutableFloatStateOf(0f) }
  var spinVelocity by remember(body.id) { mutableFloatStateOf(0f) }
  val textMeasurer = rememberTextMeasurer()

  val isComparing = compareWithPlanetId != null && compareWithPlanetId != body.id
  val compareBody = remember(compareWithPlanetId) {
    compareWithPlanetId?.let { SolarSystemData.getPlanetById(it) }
  }

  val animatedCompare by
    animateFloatAsState(
      targetValue = if (isComparing) 1f else 0f,
      animationSpec = spring(dampingRatio = 0.78f),
      label = "compareAnim",
    )

  LaunchedEffect(body.id, autoSpin) {
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          if (autoSpin) {
            yawDeg = (yawDeg + dt * 24f) % 360f
          }
          if (kotlin.math.abs(spinVelocity) > 0.2f) {
            yawDeg = (yawDeg + spinVelocity * dt) % 360f
            spinVelocity *= 0.94f
          }
          moonPhase = (moonPhase + dt * 0.9f) % (2f * PI.toFloat())
        }
        lastNanos = nanos
      }
    }
  }

  val starSeeds = remember {
    List(65) { i ->
      val sx = ((i * 73 + 19) % 100) / 100f
      val sy = ((i * 137 + 41) % 100) / 100f
      val sr = 0.8f + (i % 3) * 0.7f
      val sa = 0.25f + (i % 5) * 0.14f
      Triple(sx, sy, sr to sa)
    }
  }

  Box(
    modifier =
      modifier.pointerInput(body.id, externalZoom) {
        detectTransformGestures { _, pan, zoom, _ ->
          yawDeg = (yawDeg + pan.x * 0.42f) % 360f
          pitchDeg = (pitchDeg - pan.y * 0.35f).coerceIn(-68f, 68f)
          spinVelocity = pan.x * 1.4f
          if (zoom != 1f) {
            onZoomChange((externalZoom * zoom).coerceIn(0.65f, 1.85f))
          }
        }
      },
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      starSeeds.forEachIndexed { idx, (sx, sy, sizeAlpha) ->
        val twinkle = 0.65f + 0.35f * sin(moonPhase * 1.5f + idx)
        drawCircle(
          color = Color.White.copy(alpha = (sizeAlpha.second * twinkle).coerceIn(0.1f, 0.95f)),
          radius = sizeAlpha.first,
          center = Offset(sx * size.width, sy * size.height),
        )
      }

      val normalRadius = size.minDimension * 0.28f * externalZoom
      val effectiveTilt = if (showAxialTilt) body.axialTiltDeg else 0f

      // Calculate exact proportional radii & non-overlapping centered positions when comparing
      val mainRadius: Float
      val mainCenter: Offset
      var compRadius = 0f
      var compCenter = Offset.Zero

      if (animatedCompare > 0.01f && compareBody != null) {
        val trueRatio = body.diameterKm.toFloat() / compareBody.diameterKm.toFloat()
        val uMain = if (trueRatio >= 1f) 1f else trueRatio
        val uComp = if (trueRatio >= 1f) 1f / trueRatio else 1f

        val mainHorizMult =
          when {
            body.hasRings && body.surfaceStyle != PlanetSurfaceStyle.URANUS -> 2.25f
            body.hasRings -> 1.55f
            else -> 1.0f
          }
        val compHorizMult =
          when {
            compareBody.hasRings && compareBody.surfaceStyle != PlanetSurfaceStyle.URANUS -> 2.25f
            compareBody.hasRings -> 1.55f
            else -> 1.0f
          }
        val mainVertMult = if (body.hasRings && body.surfaceStyle == PlanetSurfaceStyle.URANUS) 1.80f else 1.0f
        val compVertMult = if (compareBody.hasRings && compareBody.surfaceStyle == PlanetSurfaceStyle.URANUS) 1.80f else 1.0f

        val gapPx = (size.width * 0.07f).coerceAtLeast(24f)
        val availW = (size.width * 0.88f - gapPx).coerceAtLeast(80f)
        val availH = size.height * 0.56f

        val unitPxW = availW / (2f * (uMain * mainHorizMult + uComp * compHorizMult))
        val unitPxH = availH / (2f * maxOf(uMain * mainVertMult, uComp * compVertMult))
        val unitPx = minOf(unitPxW, unitPxH) * externalZoom.coerceIn(0.8f, 1.15f)

        val targetMainR = uMain * unitPx
        val targetCompR = uComp * unitPx

        val leftSpan = targetMainR * mainHorizMult
        val rightSpan = targetCompR * compHorizMult
        val totalPairWidth = 2f * leftSpan + gapPx + 2f * rightSpan

        val targetMainCenterX = (size.width * 0.5f - totalPairWidth * 0.5f) + leftSpan
        val targetCompCenterX = (size.width * 0.5f + totalPairWidth * 0.5f) - rightSpan
        val pairCenterY = size.height * 0.42f

        mainRadius = lerpFloat(normalRadius, targetMainR, animatedCompare)
        mainCenter = Offset(
          x = lerpFloat(size.width * 0.5f, targetMainCenterX, animatedCompare),
          y = lerpFloat(size.height * 0.5f, pairCenterY, animatedCompare),
        )
        compRadius = targetCompR * animatedCompare
        compCenter = Offset(targetCompCenterX, pairCenterY)
      } else {
        mainRadius = normalRadius
        mainCenter = Offset(size.width * 0.5f, size.height * 0.5f)
      }

      // Moons behind planet (hidden in comparison mode so scale stays clean)
      if (body.moonsCount > 0 && animatedCompare < 0.15f) {
        drawOrbitingMoons(
          body = body,
          center = mainCenter,
          planetRadius = mainRadius,
          moonPhase = moonPhase,
          pitchDeg = pitchDeg,
          drawFront = false,
        )
      }

      val renderMainRadius =
        if (animatedCompare > 0.05f && compareBody != null) mainRadius.coerceAtLeast(2.6f) else mainRadius

      // Back half of 3D tilted rings
      if (body.hasRings) {
        draw3DRingHalf(
          center = mainCenter,
          planetRadius = renderMainRadius,
          ringColor = body.ringColor,
          pitchDeg = pitchDeg,
          tiltDeg = effectiveTilt,
          isUranus = body.surfaceStyle == PlanetSurfaceStyle.URANUS,
          drawFront = false,
        )
      }

      if (showAxialTilt) {
        drawAxialRod(mainCenter, renderMainRadius, effectiveTilt, pitchDeg, front = false)
      }

      // Main 3D planet sphere
      draw3DPlanetSphere(
        body = body,
        center = mainCenter,
        radius = renderMainRadius,
        yawDeg = yawDeg,
        pitchDeg = pitchDeg,
        tiltDeg = effectiveTilt,
        animPhase = moonPhase,
      )

      if (showAxialTilt) {
        drawAxialRod(mainCenter, renderMainRadius, effectiveTilt, pitchDeg, front = true)
      }

      if (body.hasRings) {
        draw3DRingHalf(
          center = mainCenter,
          planetRadius = renderMainRadius,
          ringColor = body.ringColor,
          pitchDeg = pitchDeg,
          tiltDeg = effectiveTilt,
          isUranus = body.surfaceStyle == PlanetSurfaceStyle.URANUS,
          drawFront = true,
        )
      }

      if (body.moonsCount > 0 && animatedCompare < 0.15f) {
        drawOrbitingMoons(
          body = body,
          center = mainCenter,
          planetRadius = renderMainRadius,
          moonPhase = moonPhase,
          pitchDeg = pitchDeg,
          drawFront = true,
        )
      }

      // Comparison Planet Sphere in EXACT diameter proportion
      if (animatedCompare > 0.05f && compareBody != null && compRadius > 0.01f) {
        val visibleCompR = compRadius.coerceAtLeast(2.6f)
        if (compareBody.hasRings) {
          draw3DRingHalf(
            center = compCenter,
            planetRadius = visibleCompR,
            ringColor = compareBody.ringColor,
            pitchDeg = pitchDeg,
            tiltDeg = 0f,
            isUranus = compareBody.surfaceStyle == PlanetSurfaceStyle.URANUS,
            drawFront = false,
          )
        }

        draw3DPlanetSphere(
          body = compareBody,
          center = compCenter,
          radius = visibleCompR,
          yawDeg = yawDeg * 1.15f,
          pitchDeg = pitchDeg,
          tiltDeg = 0f,
          animPhase = moonPhase,
        )

        if (compareBody.hasRings) {
          draw3DRingHalf(
            center = compCenter,
            planetRadius = visibleCompR,
            ringColor = compareBody.ringColor,
            pitchDeg = pitchDeg,
            tiltDeg = 0f,
            isUranus = compareBody.surfaceStyle == PlanetSurfaceStyle.URANUS,
            drawFront = true,
          )
        }

        // If one of the spheres is tiny (e.g. Earth vs Sun 1:109 or Pluto vs Jupiter), draw a subtle locator ring!
        if (compRadius < 9f) {
          drawCircle(
            color = CosmicCyan.copy(alpha = 0.85f * animatedCompare),
            radius = 12f,
            center = compCenter,
            style = Stroke(width = 2f),
          )
        }
        if (mainRadius < 9f) {
          drawCircle(
            color = CosmicAmber.copy(alpha = 0.85f * animatedCompare),
            radius = 12f,
            center = mainCenter,
            style = Stroke(width = 2f),
          )
        }

        // Crisp side-by-side labels & exact km diameters underneath both spheres
        if (animatedCompare > 0.65f) {
          val labelAlpha = ((animatedCompare - 0.65f) / 0.35f).coerceIn(0f, 1f)
          val baselineY = maxOf(mainCenter.y + renderMainRadius, compCenter.y + visibleCompR) + 18f
          val clampedBaselineY = baselineY.coerceAtMost(size.height - 46f)

          drawScaleLabel(
            textMeasurer = textMeasurer,
            name = body.name,
            diameterText = "${formatKm(body.diameterKm)} km",
            centerX = mainCenter.x,
            topY = clampedBaselineY,
            accent = CosmicAmber.copy(alpha = labelAlpha),
            subColor = StarlightMuted.copy(alpha = labelAlpha),
          )
          drawScaleLabel(
            textMeasurer = textMeasurer,
            name = compareBody.name,
            diameterText = "${formatKm(compareBody.diameterKm)} km",
            centerX = compCenter.x,
            topY = clampedBaselineY,
            accent = CosmicCyan.copy(alpha = labelAlpha),
            subColor = StarlightMuted.copy(alpha = labelAlpha),
          )
        }
      }
    }
  }
}

private fun formatKm(km: Int): String =
  String.format(java.util.Locale.ITALIAN, "%,d", km)

private fun DrawScope.drawScaleLabel(
  textMeasurer: androidx.compose.ui.text.TextMeasurer,
  name: String,
  diameterText: String,
  centerX: Float,
  topY: Float,
  accent: Color,
  subColor: Color,
) {
  val nameMeasured =
    textMeasurer.measure(
      text = name,
      style = TextStyle(fontFamily = FredokaFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent),
    )
  val kmMeasured =
    textMeasurer.measure(
      text = diameterText,
      style = TextStyle(fontFamily = SpaceMonoFontFamily, fontSize = 10.sp, color = subColor),
    )
  val nx = (centerX - nameMeasured.size.width / 2f).coerceIn(6f, size.width - nameMeasured.size.width - 6f)
  val kx = (centerX - kmMeasured.size.width / 2f).coerceIn(6f, size.width - kmMeasured.size.width - 6f)
  drawText(nameMeasured, topLeft = Offset(nx, topY))
  drawText(kmMeasured, topLeft = Offset(kx, topY + nameMeasured.size.height + 2f))
}

@Composable
fun MiniPlanet3DOrb(
  body: CelestialBody,
  modifier: Modifier = Modifier,
  size: Dp = 64.dp,
) {
  Canvas(modifier = modifier.size(size)) {
    val r = this.size.minDimension * (if (body.hasRings) 0.32f else 0.42f)
    val c = Offset(this.size.width / 2f, this.size.height / 2f)
    if (body.hasRings) {
      draw3DRingHalf(
        center = c,
        planetRadius = r,
        ringColor = body.ringColor,
        pitchDeg = 22f,
        tiltDeg = if (body.surfaceStyle == PlanetSurfaceStyle.URANUS) 75f else -14f,
        isUranus = body.surfaceStyle == PlanetSurfaceStyle.URANUS,
        drawFront = false,
      )
    }
    draw3DPlanetSphere(
      body = body,
      center = c,
      radius = r,
      yawDeg = 25f,
      pitchDeg = 14f,
      tiltDeg = 0f,
      animPhase = 0f,
    )
    if (body.hasRings) {
      draw3DRingHalf(
        center = c,
        planetRadius = r,
        ringColor = body.ringColor,
        pitchDeg = 22f,
        tiltDeg = if (body.surfaceStyle == PlanetSurfaceStyle.URANUS) 75f else -14f,
        isUranus = body.surfaceStyle == PlanetSurfaceStyle.URANUS,
        drawFront = true,
      )
    }
  }
}

private fun DrawScope.draw3DPlanetSphere(
  body: CelestialBody,
  center: Offset,
  radius: Float,
  yawDeg: Float,
  pitchDeg: Float,
  tiltDeg: Float,
  animPhase: Float,
) {
  if (radius <= 0.5f) return

  body.atmosphereColor?.let { glowColor ->
    val pulse = if (body.surfaceStyle == PlanetSurfaceStyle.SUN) 1.32f + 0.05f * sin(animPhase * 2f) else 1.16f
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(glowColor.copy(alpha = 0.42f), Color.Transparent),
          center = center,
          radius = radius * pulse,
        ),
      radius = radius * pulse,
      center = center,
    )
  }

  val lightCenter = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
  drawCircle(
    brush =
      Brush.radialGradient(
        colors = listOf(body.accentColor, body.primaryColor, body.secondaryColor),
        center = lightCenter,
        radius = radius * 1.45f,
      ),
    radius = radius,
    center = center,
  )

  if (radius < 4f) return

  val clipCircle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)) }

  clipPath(clipCircle) {
    when (body.surfaceStyle) {
      PlanetSurfaceStyle.SUN -> {
        val sunSpots = listOf(-12f to 15f, 18f to 95f, -25f to 210f, 8f to 300f, 34f to 160f)
        sunSpots.forEachIndexed { idx, (lat, lon) ->
          val v = rotateVec3(latLonToVec3(lat, lon), yawDeg, pitchDeg, tiltDeg)
          if (v.z > 0.05f) {
            val sx = center.x + v.x * radius
            val sy = center.y + v.y * radius
            val spotR = radius * (0.12f + (idx % 2) * 0.06f) * v.z
            drawOval(
              color = Color(0xFFFFE57F).copy(alpha = 0.6f * v.z),
              topLeft = Offset(sx - spotR, sy - spotR * 0.85f),
              size = Size(spotR * 2f, spotR * 1.7f),
            )
          }
        }
        listOf(10f to 40f, -15f to 180f).forEach { (lat, lon) ->
          val v = rotateVec3(latLonToVec3(lat, lon), yawDeg, pitchDeg, tiltDeg)
          if (v.z > 0.1f) {
            val sx = center.x + v.x * radius
            val sy = center.y + v.y * radius
            drawCircle(
              color = Color(0xFF9E2A00).copy(alpha = 0.65f * v.z),
              radius = radius * 0.08f * v.z,
              center = Offset(sx, sy),
            )
          }
        }
      }
      PlanetSurfaceStyle.EARTH -> {
        val continents =
          listOf(
            Triple(22f, -95f, 0.26f),
            Triple(-15f, -60f, 0.22f),
            Triple(42f, 15f, 0.20f),
            Triple(5f, 22f, 0.25f),
            Triple(36f, 85f, 0.32f),
            Triple(-25f, 135f, 0.16f),
            Triple(-82f, 0f, 0.28f),
          )
        continents.forEach { (lat, lon, sizeRatio) ->
          val v = rotateVec3(latLonToVec3(lat, lon), yawDeg, pitchDeg, tiltDeg)
          if (v.z > 0.02f) {
            val sx = center.x + v.x * radius
            val sy = center.y + v.y * radius
            val rx = radius * sizeRatio * (0.35f + 0.65f * v.z)
            val ry = radius * sizeRatio * 0.85f
            drawOval(
              color = if (lat < -70f) Color(0xFFF1FAEE) else Color(0xFF38B000),
              topLeft = Offset(sx - rx, sy - ry),
              size = Size(rx * 2f, ry * 2f),
              alpha = (v.z * 1.4f).coerceIn(0.2f, 0.95f),
            )
          }
        }
        val clouds = listOf(30f to -30f, -10f to 60f, 15f to 140f, -35f to -130f)
        clouds.forEach { (lat, lon) ->
          val v = rotateVec3(latLonToVec3(lat, lon), yawDeg * 1.25f, pitchDeg, tiltDeg)
          if (v.z > 0.08f) {
            val sx = center.x + v.x * radius
            val sy = center.y + v.y * radius
            drawOval(
              color = Color.White.copy(alpha = 0.55f * v.z),
              topLeft = Offset(sx - radius * 0.22f * v.z, sy - radius * 0.06f),
              size = Size(radius * 0.44f * v.z, radius * 0.12f),
            )
          }
        }
      }
      PlanetSurfaceStyle.MARS -> {
        val patches = listOf(15f to 20f, -20f to 110f, 5f to 230f, -10f to 310f)
        patches.forEach { (lat, lon) ->
          val v = rotateVec3(latLonToVec3(lat, lon), yawDeg, pitchDeg, tiltDeg)
          if (v.z > 0.05f) {
            val sx = center.x + v.x * radius
            val sy = center.y + v.y * radius
            drawOval(
              color = Color(0xFF6A1B1A).copy(alpha = 0.45f * v.z),
              topLeft = Offset(sx - radius * 0.24f * v.z, sy - radius * 0.14f),
              size = Size(radius * 0.48f * v.z, radius * 0.28f),
            )
          }
        }
        val pole = rotateVec3(latLonToVec3(82f, 0f), yawDeg, pitchDeg, tiltDeg)
        if (pole.z > -0.2f) {
          drawOval(
            color = Color(0xFFFFF8F0).copy(alpha = 0.85f),
            topLeft = Offset(center.x + pole.x * radius - radius * 0.24f, center.y + pole.y * radius - radius * 0.09f),
            size = Size(radius * 0.48f, radius * 0.18f),
          )
        }
      }
      PlanetSurfaceStyle.MERCURY, PlanetSurfaceStyle.PLUTO, PlanetSurfaceStyle.MOON -> {
        if (body.surfaceStyle == PlanetSurfaceStyle.MOON) {
          val maria = listOf(18f to -15f, 32f to 25f, -12f to -45f, 8f to 65f)
          maria.forEach { (lat, lon) ->
            val v = rotateVec3(latLonToVec3(lat, lon), yawDeg, pitchDeg, tiltDeg)
            if (v.z > 0.05f) {
              val sx = center.x + v.x * radius
              val sy = center.y + v.y * radius
              drawOval(
                color = Color(0xFF64748B).copy(alpha = 0.42f * v.z),
                topLeft = Offset(sx - radius * 0.25f * v.z, sy - radius * 0.18f),
                size = Size(radius * 0.50f * v.z, radius * 0.36f),
              )
            }
          }
        }
        val craters = listOf(20f to 25f, -18f to 75f, 35f to 145f, -32f to 215f, 5f to 285f, -5f to -30f)
        craters.forEachIndexed { idx, (lat, lon) ->
          val v = rotateVec3(latLonToVec3(lat, lon), yawDeg, pitchDeg, tiltDeg)
          if (v.z > 0.1f) {
            val sx = center.x + v.x * radius
            val sy = center.y + v.y * radius
            val cr = radius * (0.09f + (idx % 3) * 0.03f)
            drawOval(
              color = body.secondaryColor.copy(alpha = 0.65f * v.z),
              topLeft = Offset(sx - cr * v.z, sy - cr),
              size = Size(cr * 2f * v.z, cr * 2f),
            )
            drawOval(
              color = body.accentColor.copy(alpha = 0.35f * v.z),
              topLeft = Offset(sx - cr * v.z, sy - cr),
              size = Size(cr * 2f * v.z, cr * 2f),
              style = Stroke(width = 1.5f),
            )
          }
        }
        if (body.surfaceStyle == PlanetSurfaceStyle.PLUTO) {
          val heartPos = rotateVec3(latLonToVec3(-10f, 15f), yawDeg, pitchDeg, tiltDeg)
          if (heartPos.z > 0.1f) {
            val hx = center.x + heartPos.x * radius
            val hy = center.y + heartPos.y * radius
            val hr = radius * 0.26f * heartPos.z
            drawCircle(color = Color(0xFFFFF0F3).copy(alpha = 0.85f * heartPos.z), radius = hr * 0.65f, center = Offset(hx - hr * 0.35f, hy - hr * 0.15f))
            drawCircle(color = Color(0xFFFFF0F3).copy(alpha = 0.85f * heartPos.z), radius = hr * 0.65f, center = Offset(hx + hr * 0.35f, hy - hr * 0.15f))
            drawOval(color = Color(0xFFFFF0F3).copy(alpha = 0.85f * heartPos.z), topLeft = Offset(hx - hr * 0.65f, hy - hr * 0.1f), size = Size(hr * 1.3f, hr * 0.9f))
          }
        }
      }
      PlanetSurfaceStyle.JUPITER -> {
        val bandOffsets = listOf(-0.62f, -0.35f, -0.10f, 0.18f, 0.45f, 0.68f)
        bandOffsets.forEachIndexed { idx, bandY ->
          val bandColor = if (idx % 2 == 0) Color(0xFFB5653A) else Color(0xFFFCE7C8)
          rotate(degrees = tiltDeg, pivot = center) {
            drawRect(
              color = bandColor.copy(alpha = 0.55f),
              topLeft = Offset(center.x - radius * 1.1f, center.y + bandY * radius - radius * 0.08f),
              size = Size(radius * 2.2f, radius * 0.15f),
            )
          }
        }
        val spot = rotateVec3(latLonToVec3(-22f, 20f), yawDeg, pitchDeg, tiltDeg)
        if (spot.z > 0.06f) {
          val sx = center.x + spot.x * radius
          val sy = center.y + spot.y * radius
          drawOval(
            color = Color(0xFFD6402B).copy(alpha = 0.92f * spot.z),
            topLeft = Offset(sx - radius * 0.22f * spot.z, sy - radius * 0.12f),
            size = Size(radius * 0.44f * spot.z, radius * 0.24f),
          )
          drawOval(
            color = Color(0xFFFF9F68).copy(alpha = 0.7f * spot.z),
            topLeft = Offset(sx - radius * 0.14f * spot.z, sy - radius * 0.07f),
            size = Size(radius * 0.28f * spot.z, radius * 0.14f),
          )
        }
      }
      PlanetSurfaceStyle.SATURN, PlanetSurfaceStyle.VENUS, PlanetSurfaceStyle.URANUS, PlanetSurfaceStyle.NEPTUNE -> {
        val bands = listOf(-0.48f, -0.18f, 0.14f, 0.42f)
        bands.forEachIndexed { idx, by ->
          val c = if (idx % 2 == 0) body.secondaryColor else body.accentColor
          rotate(degrees = tiltDeg, pivot = center) {
            drawRect(
              color = c.copy(alpha = 0.28f),
              topLeft = Offset(center.x - radius * 1.1f, center.y + by * radius - radius * 0.07f),
              size = Size(radius * 2.2f, radius * 0.13f),
            )
          }
        }
        if (body.surfaceStyle == PlanetSurfaceStyle.NEPTUNE) {
          val storm = rotateVec3(latLonToVec3(-18f, 35f), yawDeg, pitchDeg, tiltDeg)
          if (storm.z > 0.08f) {
            drawOval(
              color = Color(0xFF0E1B5E).copy(alpha = 0.85f * storm.z),
              topLeft = Offset(center.x + storm.x * radius - radius * 0.18f * storm.z, center.y + storm.y * radius - radius * 0.09f),
              size = Size(radius * 0.36f * storm.z, radius * 0.18f),
            )
          }
        }
      }
    }

    if (body.surfaceStyle != PlanetSurfaceStyle.SUN) {
      drawCircle(
        brush =
          Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Transparent, Color(0xAA040714)),
            center = Offset(center.x - radius * 0.38f, center.y - radius * 0.32f),
            radius = radius * 1.48f,
          ),
        radius = radius,
        center = center,
      )
    }

    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
          center = Offset(center.x - radius * 0.45f, center.y - radius * 0.45f),
          radius = radius * 0.65f,
        ),
      radius = radius,
      center = center,
    )
  }
}

private fun DrawScope.draw3DRingHalf(
  center: Offset,
  planetRadius: Float,
  ringColor: Color,
  pitchDeg: Float,
  tiltDeg: Float,
  isUranus: Boolean,
  drawFront: Boolean,
) {
  if (planetRadius < 2f) return
  val ringWidth = planetRadius * (if (isUranus) 1.78f else 2.22f)
  val tiltFactor = sin(Math.toRadians(pitchDeg.toDouble().coerceIn(-75.0, 75.0))).toFloat()
  val ringHeight = (ringWidth * (0.20f + 0.45f * kotlin.math.abs(tiltFactor))).coerceAtLeast(planetRadius * 0.22f)

  val effectiveRotation = if (isUranus) tiltDeg + 78f else tiltDeg
  val startAngle = if (drawFront) 0f else 180f

  rotate(degrees = effectiveRotation, pivot = center) {
    drawArc(
      color = ringColor.copy(alpha = if (isUranus) 0.45f else 0.72f),
      startAngle = startAngle,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(center.x - ringWidth, center.y - ringHeight),
      size = Size(ringWidth * 2f, ringHeight * 2f),
      style = Stroke(width = (planetRadius * (if (isUranus) 0.12f else 0.28f)).coerceAtLeast(1.5f)),
    )
    val innerW = ringWidth * 0.78f
    val innerH = ringHeight * 0.78f
    drawArc(
      color = Color.White.copy(alpha = if (isUranus) 0.25f else 0.42f),
      startAngle = startAngle,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(center.x - innerW, center.y - innerH),
      size = Size(innerW * 2f, innerH * 2f),
      style = Stroke(width = (planetRadius * (if (isUranus) 0.06f else 0.16f)).coerceAtLeast(1f)),
    )
  }
}

private fun DrawScope.drawAxialRod(
  center: Offset,
  radius: Float,
  tiltDeg: Float,
  pitchDeg: Float,
  front: Boolean,
) {
  val len = radius * 1.42f
  val rad = Math.toRadians(tiltDeg.toDouble())
  val dx = (sin(rad) * len).toFloat()
  val dy = (-cos(rad) * len * cos(Math.toRadians(pitchDeg.toDouble()))).toFloat()

  if (!front) {
    drawLine(
      color = CosmicCyan.copy(alpha = 0.55f),
      start = center,
      end = Offset(center.x - dx, center.y - dy),
      strokeWidth = 4f,
      cap = StrokeCap.Round,
    )
  } else {
    drawLine(
      color = CosmicAmber,
      start = center,
      end = Offset(center.x + dx, center.y + dy),
      strokeWidth = 4.5f,
      cap = StrokeCap.Round,
    )
    drawCircle(color = CosmicAmber, radius = 6f, center = Offset(center.x + dx, center.y + dy))
  }
}

private fun DrawScope.drawOrbitingMoons(
  body: CelestialBody,
  center: Offset,
  planetRadius: Float,
  moonPhase: Float,
  pitchDeg: Float,
  drawFront: Boolean,
) {
  val moonVisualCount = body.notableMoons.size.coerceIn(1, 3)
  val pitchSin = sin(Math.toRadians(pitchDeg.toDouble())).toFloat()
  for (i in 0 until moonVisualCount) {
    val angle = moonPhase * (1.1f + i * 0.45f) + i * 2.1f
    val orbitR = planetRadius * (1.58f + i * 0.34f)
    val mx = cos(angle) * orbitR
    val mz = sin(angle) * orbitR
    val isInFront = mz >= 0f
    if (isInFront == drawFront) {
      val screenX = center.x + mx
      val screenY = center.y + mz * (0.28f + 0.3f * pitchSin)
      val moonR = (planetRadius * (0.14f - i * 0.02f)).coerceAtLeast(4.5f) * (1f + 0.18f * (mz / orbitR))
      drawCircle(color = Color(0xFFE2E8F0), radius = moonR, center = Offset(screenX, screenY))
      drawCircle(color = Color(0xFF94A3B8), radius = moonR * 0.35f, center = Offset(screenX - moonR * 0.25f, screenY - moonR * 0.2f))
    }
  }
}
