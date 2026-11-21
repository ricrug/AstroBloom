package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrbitVec3
import com.example.data.OrbitalMechanics
import com.example.data.SolarSystemData
import com.example.model.CelestialBody
import com.example.model.ChibiProfile
import com.example.model.PlanetSurfaceStyle
import com.example.ui.theme.AuroraGreen
import com.example.ui.theme.ComicInk
import com.example.ui.theme.CosmicAmber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.NebulaMagenta
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceNavy
import com.example.ui.theme.SpaceVoid
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.StarlightWhite
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private fun rotateAroundAxis(v: OrbitVec3, axis: OrbitVec3, angleRad: Float): OrbitVec3 {
  val k = axis.normalized()
  val cosA = cos(angleRad)
  val sinA = sin(angleRad)
  val term1 = v * cosA
  val term2 = k.cross(v) * sinA
  val term3 = k * (k.dot(v) * (1f - cosA))
  return term1 + term2 + term3
}

private data class CockpitProjectedBody(
  val body: CelestialBody,
  val worldPos: OrbitVec3,
  val screenPos: Offset,
  val screenRadius: Float,
  val depth: Float,
)

@Composable
fun FirstPersonFlightSimulator3D(
  chibiProfile: ChibiProfile,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenLandingSimulator: (String) -> Unit,
  onExitSimulator: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bodies = SolarSystemData.celestialBodies
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  // Ship starts near Earth-Mars corridor looking toward the Sun & inner ecliptic planets
  var shipPos by remember { mutableStateOf(OrbitVec3(-110f, 26f, 175f)) }
  var shipForward by remember { mutableStateOf(OrbitVec3(0.45f, -0.10f, -0.88f).normalized()) }
  var shipUp by remember { mutableStateOf(OrbitVec3(0f, 1f, 0f)) }

  // Flight controls state
  var throttle by remember { mutableFloatStateOf(0.22f) } // -0.25 (reverse) .. 1.0 (warp)
  var currentSpeed by remember { mutableFloatStateOf(20f) }
  var boostActive by remember { mutableStateOf(false) }

  // Steering stick (-1..1)
  var stickX by remember { mutableFloatStateOf(0f) }
  var stickY by remember { mutableFloatStateOf(0f) }

  // Autopilot & simulation state
  var autopilotTargetId by remember { mutableStateOf<String?>(null) }
  var isAutopilotSteering by remember { mutableStateOf(false) }
  var orbitsPaused by remember { mutableStateOf(false) }
  var simTimeSec by remember { mutableFloatStateOf(14f) }
  var nearestBody by remember { mutableStateOf<CelestialBody>(bodies.first()) }
  var nearestDist by remember { mutableFloatStateOf(100f) }
  var lastProjected by remember { mutableStateOf<List<CockpitProjectedBody>>(emptyList()) }

  // 140 local 3D stars wrapped in a 400-unit cube around ship
  val starCubeCoords = remember {
    List(140) { i ->
      val x = (((i * 97 + 13) % 400) - 200).toFloat()
      val y = (((i * 151 + 47) % 400) - 200).toFloat()
      val z = (((i * 223 + 89) % 400) - 200).toFloat()
      val brightness = 0.45f + (i % 5) * 0.12f
      OrbitVec3(x, y, z) to brightness
    }
  }

  // Asteroid belt particles
  val asteroids = remember {
    List(100) { i ->
      val r = 218f + ((i * 37) % 24).toFloat()
      val angleOffset = (i * 137.5f) % 360f
      val yOffset = (((i * 29) % 14) - 7).toFloat()
      val speed = 13f - (r - 218f) * 0.1f
      Triple(r, angleOffset, yOffset to speed)
    }
  }

  // 60fps 6-DOF Flight Physics & Keplerian Orbit Loop
  LaunchedEffect(Unit) {
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          if (!orbitsPaused) {
            simTimeSec += dt
          }

          val right = shipForward.cross(shipUp).normalized()

          val hasStick = abs(stickX) > 0.04f || abs(stickY) > 0.04f
          if (hasStick) {
            isAutopilotSteering = false
            val yawAngle = -stickX * 1.45f * dt
            val pitchAngle = -stickY * 1.15f * dt

            var f = rotateAroundAxis(shipForward, shipUp, yawAngle)
            var u = shipUp
            f = rotateAroundAxis(f, right, pitchAngle)
            u = rotateAroundAxis(u, right, pitchAngle)

            shipForward = f.normalized()
            val newRight = shipForward.cross(u).normalized()
            shipUp = newRight.cross(shipForward).normalized()
          } else if (isAutopilotSteering && autopilotTargetId != null) {
            val targetBody = SolarSystemData.getPlanetById(autopilotTargetId!!)
            val targetPos = OrbitalMechanics.bodyWorldPosition(targetBody, simTimeSec)
            val toTarget = targetPos - shipPos
            val dist = toTarget.length()
            val desiredDir = toTarget.normalized()

            val slew = (dt * 3.2f).coerceAtMost(1f)
            shipForward = (shipForward + (desiredDir - shipForward) * slew).normalized()
            val worldY = OrbitVec3(0f, 1f, 0f)
            val desiredRight =
              if (abs(shipForward.dot(worldY)) > 0.96f) OrbitVec3(1f, 0f, 0f)
              else shipForward.cross(worldY).normalized()
            val desiredUp = desiredRight.cross(shipForward).normalized()
            shipUp = (shipUp + (desiredUp - shipUp) * slew).normalized()

            val stopDist = targetBody.sceneVisualRadius * 3.4f + 16f
            if (dist > stopDist) {
              throttle = if (dist > 140f) 0.72f else 0.32f
            } else {
              throttle = 0f
              currentSpeed = 0f
              isAutopilotSteering = false
            }
          }

          val maxForwardSpeed = if (boostActive) 220f else 135f
          val targetSpeed = throttle * maxForwardSpeed
          currentSpeed += (targetSpeed - currentSpeed) * (dt * 4.5f).coerceAtMost(1f)
          shipPos += shipForward * (currentSpeed * dt)

          if (shipPos.length() > 760f) {
            shipPos = shipPos.normalized() * 760f
          }

          var bestBody = bodies.first()
          var bestDist = Float.MAX_VALUE
          bodies.forEach { b ->
            val d = (OrbitalMechanics.bodyWorldPosition(b, simTimeSec) - shipPos).length()
            if (d < bestDist) {
              bestDist = d
              bestBody = b
            }
          }
          nearestBody = bestBody
          nearestDist = bestDist
        }
        lastNanos = nanos
      }
    }
  }

  // Full-Screen Immersive Landscape Cockpit
  Box(
    modifier = modifier.fillMaxSize().background(SpaceVoid).testTag("first_person_flight_sim"),
  ) {
    // 1. Full-Bleed 3D Cockpit Windshield
    Canvas(
      modifier =
        Modifier.fillMaxSize()
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              isAutopilotSteering = false
              val right = shipForward.cross(shipUp).normalized()
              val yawDelta = -dragAmount.x * 0.0032f
              val pitchDelta = -dragAmount.y * 0.0028f
              var f = rotateAroundAxis(shipForward, shipUp, yawDelta)
              var u = shipUp
              f = rotateAroundAxis(f, right, pitchDelta)
              u = rotateAroundAxis(u, right, pitchDelta)
              shipForward = f.normalized()
              val newRight = shipForward.cross(u).normalized()
              shipUp = newRight.cross(shipForward).normalized()
            }
          }
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              val hit =
                lastProjected.asReversed().firstOrNull { proj ->
                  val hitR = proj.screenRadius.coerceAtLeast(26f)
                  hypot(tapOffset.x - proj.screenPos.x, tapOffset.y - proj.screenPos.y) <= hitR
                }
              if (hit != null) {
                autopilotTargetId = hit.body.id
              }
            }
          },
    ) {
      drawRect(Brush.radialGradient(listOf(Color(0xFF0A102B), SpaceVoid), radius = size.maxDimension * 0.85f))

      val center = Offset(size.width * 0.5f, size.height * 0.44f)
      val focal = size.minDimension * 1.15f
      val shipRight = shipForward.cross(shipUp).normalized()

      fun toCameraSpace(worldPt: OrbitVec3): OrbitVec3 {
        val rel = worldPt - shipPos
        return OrbitVec3(rel.dot(shipRight), rel.dot(shipUp), rel.dot(shipForward))
      }

      // Wrapping 3D Stars + Speed Warp Streaks
      val speedNorm = (abs(currentSpeed) / 140f).coerceIn(0f, 1.4f)
      starCubeCoords.forEach { (baseStar, alpha) ->
        val wx = ((baseStar.x - shipPos.x) % 400f + 600f) % 400f - 200f
        val wy = ((baseStar.y - shipPos.y) % 400f + 600f) % 400f - 200f
        val wz = ((baseStar.z - shipPos.z) % 400f + 600f) % 400f - 200f
        val rel = OrbitVec3(wx, wy, wz)
        val cz = rel.dot(shipForward)
        if (cz > 4f) {
          val cx = rel.dot(shipRight)
          val cy = rel.dot(shipUp)
          val sx = center.x + (cx / cz) * focal
          val sy = center.y - (cy / cz) * focal
          if (sx in -40f..(size.width + 40f) && sy in -40f..(size.height + 40f)) {
            val distFade = (1f - cz / 210f).coerceIn(0.15f, 1f)
            if (speedNorm > 0.25f) {
              val dx = sx - center.x
              val dy = sy - center.y
              val streak = speedNorm * 0.16f * distFade
              drawLine(
                color = (if (boostActive) CosmicCyan else Color.White).copy(alpha = alpha * distFade),
                start = Offset(sx, sy),
                end = Offset(sx + dx * streak, sy + dy * streak),
                strokeWidth = 1.6f + distFade * 1.4f,
                cap = StrokeCap.Round,
              )
            } else {
              drawCircle(
                color = Color.White.copy(alpha = alpha * distFade),
                radius = 1.2f + distFade * 1.4f,
                center = Offset(sx, sy),
              )
            }
          }
        }
      }

      // Realistic Inclined Elliptical Ecliptic Orbits
      bodies.filter { it.orbitDistanceScene > 0f }.forEach { b ->
        val pts = OrbitalMechanics.sampleOrbitLoop(b, simTimeSec, segments = 48)
        var prevPt: Offset? = null
        pts.forEach { worldPt ->
          val cam = toCameraSpace(worldPt)
          if (cam.z > 6f) {
            val pt = Offset(center.x + (cam.x / cam.z) * focal, center.y - (cam.y / cam.z) * focal)
            if (prevPt != null) {
              drawLine(
                color = b.primaryColor.copy(alpha = if (b.id == autopilotTargetId) 0.55f else 0.22f),
                start = prevPt!!,
                end = pt,
                strokeWidth = if (b.id == autopilotTargetId) 2.2f else 1.2f,
              )
            }
            prevPt = pt
          } else {
            prevPt = null
          }
        }
      }

      // Asteroid belt particles
      asteroids.forEach { (r, angle0, ySpeed) ->
        val ang = Math.toRadians((angle0 + simTimeSec * ySpeed.second).toDouble())
        val cam = toCameraSpace(OrbitVec3((cos(ang) * r).toFloat(), ySpeed.first, (sin(ang) * r).toFloat()))
        if (cam.z > 5f) {
          val sx = center.x + (cam.x / cam.z) * focal
          val sy = center.y - (cam.y / cam.z) * focal
          if (sx in 0f..size.width && sy in 0f..size.height) {
            drawCircle(
              color = Color(0xFFB7B7C9).copy(alpha = 0.55f),
              radius = (1.6f * focal / cam.z).coerceIn(1f, 3.5f),
              center = Offset(sx, sy),
            )
          }
        }
      }

      // Project all 11 Celestial Bodies & sort back-to-front
      val projected = mutableListOf<CockpitProjectedBody>()
      bodies.forEach { b ->
        val wp = OrbitalMechanics.bodyWorldPosition(b, simTimeSec)
        val cam = toCameraSpace(wp)
        if (cam.z > 2.5f) {
          val sx = center.x + (cam.x / cam.z) * focal
          val sy = center.y - (cam.y / cam.z) * focal
          val sr = ((b.sceneVisualRadius * 1.15f) / cam.z) * focal
          projected.add(CockpitProjectedBody(b, wp, Offset(sx, sy), sr, cam.z))
        }
      }
      val sorted = projected.sortedByDescending { it.depth }
      lastProjected = sorted

      sorted.forEach { p ->
        val drawR = p.screenRadius.coerceIn(3f, size.maxDimension * 0.45f)
        drawCockpitPlanet(
          body = p.body,
          screenPos = p.screenPos,
          radius = drawR,
          isLocked = p.body.id == autopilotTargetId,
          textMeasurer = textMeasurer,
          showLabel = p.depth < 620f || p.body.id == autopilotTargetId || p.body.id == "sun",
        )
      }

      // Off-screen Target Waypoint Chevron
      autopilotTargetId?.let { targetId ->
        val tBody = SolarSystemData.getPlanetById(targetId)
        val cam = toCameraSpace(OrbitalMechanics.bodyWorldPosition(tBody, simTimeSec))
        val onScreen =
          if (cam.z > 2.5f) {
            val sx = center.x + (cam.x / cam.z) * focal
            val sy = center.y - (cam.y / cam.z) * focal
            sx in 40f..(size.width - 40f) && sy in 40f..(size.height - 80f)
          } else {
            false
          }
        if (!onScreen) {
          val dirX = cam.x
          val dirY = -cam.y
          val ang = atan2(dirY, dirX)
          val edgeR = size.minDimension * 0.36f
          val arrowCenter = Offset(center.x + cos(ang) * edgeR, center.y + sin(ang) * edgeR)
          drawCircle(color = CosmicCyan.copy(alpha = 0.22f), radius = 18f, center = arrowCenter)
          val tip = Offset(arrowCenter.x + cos(ang) * 14f, arrowCenter.y + sin(ang) * 14f)
          val left = Offset(arrowCenter.x + cos(ang + 2.4f) * 11f, arrowCenter.y + sin(ang + 2.4f) * 11f)
          val rightPt = Offset(arrowCenter.x + cos(ang - 2.4f) * 11f, arrowCenter.y + sin(ang - 2.4f) * 11f)
          val arrowPath = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(left.x, left.y)
            lineTo(rightPt.x, rightPt.y)
            close()
          }
          drawPath(arrowPath, color = CosmicAmber)
          val lbl = textMeasurer.measure(tBody.name, TextStyle(fontFamily = FredokaFontFamily, fontSize = 10.sp, color = CosmicAmber))
          drawText(lbl, topLeft = Offset((arrowCenter.x - lbl.size.width / 2f).coerceIn(8f, size.width - lbl.size.width - 8f), (arrowCenter.y + 16f).coerceAtMost(size.height - 20f)))
        }
      }

      // Cockpit Artificial Horizon & Reticle
      val rollRad = atan2(shipRight.y.toDouble(), sqrt((shipRight.x * shipRight.x + shipRight.z * shipRight.z).toDouble())).toFloat()
      val horizonR = 36f
      val hx = cos(rollRad) * horizonR
      val hy = -sin(rollRad) * horizonR
      drawLine(
        color = CosmicCyan.copy(alpha = 0.45f),
        start = Offset(center.x - hx, center.y - hy),
        end = Offset(center.x - hx * 0.35f, center.y - hy * 0.35f),
        strokeWidth = 2f,
      )
      drawLine(
        color = CosmicCyan.copy(alpha = 0.45f),
        start = Offset(center.x + hx * 0.35f, center.y + hy * 0.35f),
        end = Offset(center.x + hx, center.y + hy),
        strokeWidth = 2f,
      )
      drawCircle(color = CosmicCyan.copy(alpha = 0.55f), radius = 10f, center = center, style = Stroke(width = 1.6f))
      drawCircle(color = CosmicAmber, radius = 2.5f, center = center)

      // Cockpit Windshield Canopy Struts
      val frameColor = Color(0xFF141C38)
      drawLine(color = frameColor, start = Offset(0f, size.height * 0.24f), end = Offset(size.width * 0.12f, 0f), strokeWidth = 14f)
      drawLine(color = frameColor, start = Offset(size.width, size.height * 0.24f), end = Offset(size.width * 0.88f, 0f), strokeWidth = 14f)
      drawLine(color = CosmicCyan.copy(alpha = 0.28f), start = Offset(0f, size.height * 0.24f), end = Offset(size.width * 0.12f, 0f), strokeWidth = 2f)
      drawLine(color = CosmicCyan.copy(alpha = 0.28f), start = Offset(size.width, size.height * 0.24f), end = Offset(size.width * 0.88f, 0f), strokeWidth = 2f)

      // Top-Right 2D Mini-Radar inside Cockpit
      val radarR = 36f
      val radarCenter = Offset(size.width - radarR - 14f, radarR + 50f)
      drawCircle(color = SpaceVoid.copy(alpha = 0.80f), radius = radarR, center = radarCenter)
      drawCircle(color = CosmicCyan.copy(alpha = 0.55f), radius = radarR, center = radarCenter, style = Stroke(width = 1.5f))
      drawCircle(color = CosmicCyan.copy(alpha = 0.22f), radius = radarR * 0.5f, center = radarCenter, style = Stroke(width = 1f))
      val radarScale = radarR / 460f
      val fwdAngle = atan2(shipForward.z, shipForward.x)
      drawLine(
        color = AuroraGreen.copy(alpha = 0.75f),
        start = radarCenter,
        end = Offset(radarCenter.x + cos(fwdAngle) * 18f, radarCenter.y + sin(fwdAngle) * 18f),
        strokeWidth = 2f,
      )
      bodies.forEach { b ->
        val wp = OrbitalMechanics.bodyWorldPosition(b, simTimeSec)
        val dx = (wp.x - shipPos.x) * radarScale
        val dz = (wp.z - shipPos.z) * radarScale
        val clampedDist = hypot(dx, dz)
        val factor = if (clampedDist > radarR - 4f) (radarR - 4f) / clampedDist else 1f
        drawCircle(
          color = if (b.id == autopilotTargetId) AuroraGreen else b.primaryColor,
          radius = if (b.id == "sun") 3.5f else 2.3f,
          center = Offset(radarCenter.x + dx * factor, radarCenter.y + dz * factor),
        )
      }
      drawCircle(color = StarlightWhite, radius = 2.8f, center = radarCenter)
    }

    // 2. Top-Left: EXIT SIMULATION BUTTON + Speed & Nearest Planet HUD
    Row(
      modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        onClick = onExitSimulator,
        color = Color(0xFFE63946),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, ComicInk),
        shadowElevation = 6.dp,
        modifier = Modifier.testTag("cockpit_exit_button"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
            contentDescription = "Esci dalla simulazione",
            tint = StarlightWhite,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Esci",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = StarlightWhite,
          )
        }
      }

      Surface(
        color = SpaceNavy.copy(alpha = 0.84f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.45f)),
      ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
          val speedKms = (abs(currentSpeed) * 240f).roundToInt()
          Text(
            text = if (currentSpeed < -1f) "RETRO $speedKms km/s" else "VELOCITÀ: $speedKms km/s",
            style = MaterialTheme.typography.labelLarge,
            color = if (boostActive) NebulaMagenta else CosmicAmber,
            modifier = Modifier.testTag("cockpit_speed_readout"),
          )
          val distKmLabel = "${(nearestDist * 1800f).roundToInt()} mila km"
          Text(
            text = "Vicino: ${nearestBody.name} ($distKmLabel)",
            style = MaterialTheme.typography.labelSmall,
            color = StarlightMuted,
          )
        }
      }
    }

    // 3. Top-Right: Orbit Pause & Level Horizon buttons
    Row(
      modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Surface(
        onClick = { orbitsPaused = !orbitsPaused },
        color = SpaceCard.copy(alpha = 0.88f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f)),
      ) {
        Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (orbitsPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
            contentDescription = "Pausa orbite",
            tint = StarlightWhite,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(if (orbitsPaused) "Orbite" else "Live", style = MaterialTheme.typography.labelSmall, color = StarlightWhite)
        }
      }
      Surface(
        onClick = {
          val worldUp = OrbitVec3(0f, 1f, 0f)
          val right =
            if (abs(shipForward.dot(worldUp)) > 0.96f) OrbitVec3(1f, 0f, 0f)
            else shipForward.cross(worldUp).normalized()
          shipUp = right.cross(shipForward).normalized()
        },
        color = SpaceCard.copy(alpha = 0.88f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicAmber.copy(alpha = 0.5f)),
        modifier = Modifier.testTag("cockpit_level_button"),
      ) {
        Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Rounded.AutoFixHigh, contentDescription = "Livella orizzonte", tint = CosmicAmber, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Livella", style = MaterialTheme.typography.labelSmall, color = CosmicAmber)
        }
      }
    }

    // 4. Bottom Landscape Cockpit Console Deck (Cloche Left | Route & Actions Center | Throttle & Roll Right)
    Row(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom,
    ) {
      // LEFT: Virtual Cloche (Direction: Pitch & Yaw)
      Surface(
        color = SpaceNavy.copy(alpha = 0.80f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.45f)),
      ) {
        Column(
          modifier = Modifier.padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          val joyMaxPx = with(density) { 28.dp.toPx() }
          Box(
            modifier =
              Modifier.size(80.dp)
                .background(SpaceVoid, CircleShape)
                .pointerInput(Unit) {
                  detectDragGestures(
                    onDragEnd = {
                      stickX = 0f
                      stickY = 0f
                    },
                    onDragCancel = {
                      stickX = 0f
                      stickY = 0f
                    },
                  ) { change, dragAmount ->
                    change.consume()
                    val rawX = stickX * joyMaxPx + dragAmount.x
                    val rawY = stickY * joyMaxPx + dragAmount.y
                    val dist = hypot(rawX, rawY)
                    val scale = if (dist > joyMaxPx) joyMaxPx / dist else 1f
                    stickX = (rawX * scale / joyMaxPx).coerceIn(-1f, 1f)
                    stickY = (rawY * scale / joyMaxPx).coerceIn(-1f, 1f)
                  }
                }
                .testTag("cockpit_flight_stick"),
            contentAlignment = Alignment.Center,
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              drawCircle(color = CosmicCyan.copy(alpha = 0.5f), radius = size.minDimension * 0.46f, style = Stroke(width = 2.5f))
              val knob = Offset(size.width / 2f + stickX * joyMaxPx, size.height / 2f + stickY * joyMaxPx)
              drawCircle(color = CosmicAmber, radius = 15.dp.toPx(), center = knob)
              drawCircle(color = ComicInk, radius = 15.dp.toPx(), center = knob, style = Stroke(width = 2f))
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text("Direzione", style = MaterialTheme.typography.labelSmall, color = StarlightMuted)
        }
      }

      // CENTER: Route Strip + Approach / Roll / Warp Bar
      Column(
        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        val actionBody =
          autopilotTargetId?.let { SolarSystemData.getPlanetById(it) }
            ?: if (nearestDist < nearestBody.sceneVisualRadius * 5.2f + 42f) nearestBody else null

        if (actionBody != null) {
          Surface(
            color = SpaceNavy.copy(alpha = 0.88f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, actionBody.primaryColor),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = if (isAutopilotSteering) "Rotta: ${actionBody.name}..." else actionBody.name,
                style = MaterialTheme.typography.labelLarge,
                color = StarlightWhite,
              )
              if (autopilotTargetId != null) {
                Button(
                  onClick = { isAutopilotSteering = true },
                  colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
                  shape = RoundedCornerShape(9.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp).testTag("cockpit_autopilot_button"),
                ) {
                  Icon(Icons.Rounded.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("Vai!", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
              }
              if (actionBody.id != "sun") {
                Button(
                  onClick = { onOpenLandingSimulator(actionBody.id) },
                  colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
                  shape = RoundedCornerShape(9.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp),
                ) {
                  Icon(Icons.Rounded.FlightLand, contentDescription = null, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("Atterra", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
              }
              Button(
                onClick = { onOpenPlanetDetail(actionBody.id) },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
                shape = RoundedCornerShape(9.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp),
              ) {
                Icon(Icons.Rounded.Explore, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("3D", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Quick Planet Target Row
        Surface(
          color = SpaceNavy.copy(alpha = 0.80f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.35f)),
        ) {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            items(bodies, key = { it.id }) { b ->
              FilterChip(
                selected = autopilotTargetId == b.id,
                onClick = {
                  autopilotTargetId = b.id
                  isAutopilotSteering = true
                },
                label = { Text(b.name, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = b.primaryColor, selectedLabelColor = ComicInk),
                modifier = Modifier.height(30.dp).testTag("cockpit_route_${b.id}"),
              )
            }
          }
        }
      }

      // RIGHT: Roll + Brake + Warp + Throttle Slider
      Surface(
        color = SpaceNavy.copy(alpha = 0.84f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.45f)),
      ) {
        Column(
          modifier = Modifier.width(210.dp).padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              onClick = {
                isAutopilotSteering = false
                val u = rotateAroundAxis(shipUp, shipForward, -0.28f)
                val right = shipForward.cross(u).normalized()
                shipUp = right.cross(shipForward).normalized()
              },
              color = SpaceCard,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("cockpit_roll_left"),
            ) {
              Icon(Icons.Rounded.RotateLeft, contentDescription = "Rollio sinistra", tint = CosmicCyan, modifier = Modifier.padding(6.dp).size(16.dp))
            }

            Surface(
              onClick = {
                throttle = 0f
                currentSpeed = 0f
                boostActive = false
                isAutopilotSteering = false
              },
              color = Color(0xFFE63946),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("cockpit_stop_button"),
            ) {
              Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.StopCircle, contentDescription = null, tint = StarlightWhite, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("STOP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StarlightWhite)
              }
            }

            Surface(
              onClick = {
                boostActive = !boostActive
                if (boostActive && throttle < 0.65f) throttle = 0.85f
              },
              color = if (boostActive) NebulaMagenta else SpaceCard,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("cockpit_warp_chip"),
            ) {
              Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = StarlightWhite, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("WARP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StarlightWhite)
              }
            }

            Surface(
              onClick = {
                isAutopilotSteering = false
                val u = rotateAroundAxis(shipUp, shipForward, 0.28f)
                val right = shipForward.cross(u).normalized()
                shipUp = right.cross(shipForward).normalized()
              },
              color = SpaceCard,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("cockpit_roll_right"),
            ) {
              Icon(Icons.Rounded.RotateRight, contentDescription = "Rollio destra", tint = CosmicCyan, modifier = Modifier.padding(6.dp).size(16.dp))
            }
          }

          Spacer(modifier = Modifier.height(2.dp))
          val throttlePct = (throttle * 100f).roundToInt()
          Text(
            text = if (throttlePct < 0) "Retro $throttlePct%" else "Motore $throttlePct%",
            style = MaterialTheme.typography.labelSmall,
            color = if (throttlePct > 0) AuroraGreen else CosmicAmber,
          )
          Slider(
            value = throttle,
            onValueChange = {
              isAutopilotSteering = false
              throttle = it
            },
            valueRange = -0.25f..1.0f,
            colors = SliderDefaults.colors(thumbColor = AuroraGreen, activeTrackColor = AuroraGreen),
            modifier = Modifier.height(26.dp).testTag("cockpit_throttle_slider"),
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(-0.2f to "Retro", 0.35f to "Croc.", 1.0f to "Max").forEach { (valT, lbl) ->
              Surface(
                onClick = {
                  isAutopilotSteering = false
                  throttle = valT
                },
                color = if (abs(throttle - valT) < 0.08f) CosmicCyan else SpaceCard,
                shape = RoundedCornerShape(8.dp),
              ) {
                Text(
                  text = lbl,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (abs(throttle - valT) < 0.08f) ComicInk else StarlightMuted,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun DrawScope.drawCockpitPlanet(
  body: CelestialBody,
  screenPos: Offset,
  radius: Float,
  isLocked: Boolean,
  textMeasurer: TextMeasurer,
  showLabel: Boolean,
) {
  if (body.id == "sun") {
    drawCircle(
      brush = Brush.radialGradient(listOf(Color(0xFFFFF3B0), Color(0xFFFF9F1C), Color.Transparent), center = screenPos, radius = radius * 2.5f),
      radius = radius * 2.5f,
      center = screenPos,
    )
    drawCircle(color = Color(0xFFFFD166), radius = radius, center = screenPos)
  } else {
    if (body.hasRings) {
      val ringW = radius * (if (body.surfaceStyle == PlanetSurfaceStyle.URANUS) 1.3f else 2.25f)
      val ringH = radius * (if (body.surfaceStyle == PlanetSurfaceStyle.URANUS) 2.2f else 0.72f)
      drawOval(
        color = body.ringColor.copy(alpha = 0.72f),
        topLeft = Offset(screenPos.x - ringW, screenPos.y - ringH * 0.5f),
        size = Size(ringW * 2f, ringH),
        style = Stroke(width = (radius * 0.2f).coerceIn(2f, 12f)),
      )
    }

    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(body.accentColor, body.primaryColor, Color(0xFF0A0D1A)),
          center = Offset(screenPos.x - radius * 0.3f, screenPos.y - radius * 0.3f),
          radius = (radius * 1.45f).coerceAtLeast(4f),
        ),
      radius = radius,
      center = screenPos,
    )
  }

  if (isLocked) {
    val targetR = (radius + 10f).coerceAtLeast(18f)
    drawCircle(
      color = AuroraGreen,
      radius = targetR,
      center = screenPos,
      style = Stroke(width = 2.2f),
    )
  }

  if (showLabel && screenPos.x in 10f..(size.width - 10f) && screenPos.y in 10f..(size.height - 22f)) {
    val lbl =
      textMeasurer.measure(
        body.name,
        TextStyle(
          fontFamily = FredokaFontFamily,
          fontSize = if (isLocked) 12.sp else 10.sp,
          fontWeight = FontWeight.Bold,
          color = if (isLocked) AuroraGreen else StarlightWhite,
        ),
      )
    drawText(
      lbl,
      topLeft = Offset((screenPos.x - lbl.size.width / 2f).coerceIn(4f, size.width - lbl.size.width - 4f), (screenPos.y + radius + 5f).coerceAtMost(size.height - 18f)),
    )
  }
}
