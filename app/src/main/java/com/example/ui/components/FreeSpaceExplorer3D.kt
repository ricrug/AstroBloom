package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
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
import com.example.model.ChibiExpression
import com.example.model.ChibiProfile
import com.example.ui.theme.AuroraGreen
import com.example.ui.theme.ComicBubbleCream
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
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private data class ProjectedBody(
  val body: CelestialBody,
  val worldPos: OrbitVec3,
  val screenPos: Offset,
  val screenRadius: Float,
  val depth: Float,
)

private fun projectPoint(
  worldPos: OrbitVec3,
  camFocus: OrbitVec3,
  camYawDeg: Float,
  camPitchDeg: Float,
  camDistance: Float,
  screenCenter: Offset,
  focalLength: Float,
): Triple<Offset, Float, Float>? {
  val rel = worldPos - camFocus
  val yaw = Math.toRadians(camYawDeg.toDouble())
  val pitch = Math.toRadians(camPitchDeg.toDouble())

  val cy = cos(yaw).toFloat()
  val sy = sin(yaw).toFloat()
  val x1 = rel.x * cy - rel.z * sy
  val z1 = rel.x * sy + rel.z * cy

  val cp = cos(pitch).toFloat()
  val sp = sin(pitch).toFloat()
  val y2 = rel.y * cp - z1 * sp
  val z2 = rel.y * sp + z1 * cp + camDistance

  if (z2 < 25f) return null
  val scale = focalLength / z2
  return Triple(
    Offset(screenCenter.x + x1 * scale, screenCenter.y + y2 * scale),
    scale,
    z2,
  )
}

@Composable
fun FreeSpaceExplorer3D(
  chibiProfile: ChibiProfile,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenLandingSimulator: (String) -> Unit,
  onOpenFirstPersonCockpit: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val bodies = SolarSystemData.celestialBodies
  val textMeasurer = rememberTextMeasurer()

  // 3D Camera & Navigation State (low-ish initial pitch makes tilted ecliptic orbits pop immediately!)
  var camYaw by remember { mutableFloatStateOf(-22f) }
  var camPitch by remember { mutableFloatStateOf(18f) }
  var camDistance by remember { mutableFloatStateOf(590f) }

  // Free flight spaceship position & velocity
  var focusPos by remember { mutableStateOf(OrbitVec3(0f, 0f, 0f)) }
  var shipVelocity by remember { mutableStateOf(OrbitVec3(0f, 0f, 0f)) }
  var rocketHeadingDeg by remember { mutableFloatStateOf(0f) }
  var lockedPlanetId by remember { mutableStateOf<String?>(null) }
  var nearPlanet by remember { mutableStateOf<CelestialBody?>(bodies.first()) }

  // Orbital simulation state
  var simTimeSec by remember { mutableFloatStateOf(12f) }
  var timeSpeedMultiplier by remember { mutableFloatStateOf(1f) }
  var showOrbits by remember { mutableStateOf(true) }

  // Virtual Joystick state (-1..1) & altitude thrusters (-1..1)
  var joyX by remember { mutableFloatStateOf(0f) }
  var joyY by remember { mutableFloatStateOf(0f) }
  var verticalThrust by remember { mutableFloatStateOf(0f) }

  // Screen projection cache for tap hit-testing
  var lastProjectedBodies by remember { mutableStateOf<List<ProjectedBody>>(emptyList()) }

  // Initial orbital angles spread nicely around the Sun
  val initialAngles = remember {
    listOf(0f, 25f, 110f, 200f, 40f, 305f, 55f, 165f, 255f, 340f, 80f)
  }

  // Asteroid belt particles (between Mars 195 and Jupiter 258)
  val asteroids = remember {
    List(110) { i ->
      val r = 218f + ((i * 37) % 24).toFloat()
      val angleOffset = (i * 137.5f) % 360f
      val yOffset = (((i * 29) % 14) - 7).toFloat()
      val speed = 13f - (r - 218f) * 0.1f
      Triple(r, angleOffset, yOffset to speed)
    }
  }

  // Deep 3D stars
  val deepStars = remember {
    List(140) { i ->
      val theta = (i * 137.5f) * PI / 180.0
      val phi = ((i * 53) % 180 - 90) * PI / 180.0
      val dist = 1200f + (i % 5) * 180f
      OrbitVec3(
        x = (dist * cos(phi) * cos(theta)).toFloat(),
        y = (dist * sin(phi)).toFloat(),
        z = (dist * cos(phi) * sin(theta)).toFloat(),
      )
    }
  }

  // Frame loop: Keplerian ecliptic orbits, smooth free-flight physics, and autopilot follow
  LaunchedEffect(Unit) {
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          simTimeSec += dt * timeSpeedMultiplier

          val hasPilotInput =
            hypot(joyX, joyY) > 0.06f || kotlin.math.abs(verticalThrust) > 0.05f

          if (hasPilotInput) {
            lockedPlanetId = null
            val yawRad = Math.toRadians(camYaw.toDouble())
            val screenRight = OrbitVec3(cos(yawRad).toFloat(), 0f, -sin(yawRad).toFloat())
            val screenForward = OrbitVec3(sin(yawRad).toFloat(), 0f, cos(yawRad).toFloat())

            val maxSpeed = (camDistance * 0.52f).coerceIn(130f, 340f)
            val desiredVel =
              screenRight * (joyX * maxSpeed) +
                screenForward * (-joyY * maxSpeed) +
                OrbitVec3(0f, verticalThrust * maxSpeed * 0.65f, 0f)

            val accel = (dt * 7.5f).coerceAtMost(1f)
            shipVelocity = shipVelocity + (desiredVel - shipVelocity) * accel
            focusPos = focusPos + shipVelocity * dt

            if (hypot(joyX, joyY) > 0.08f) {
              rocketHeadingDeg = Math.toDegrees(atan2(joyX.toDouble(), -joyY.toDouble())).toFloat()
            }
          } else if (lockedPlanetId != null) {
            val targetBody = SolarSystemData.getPlanetById(lockedPlanetId!!)
            val targetPos = OrbitalMechanics.bodyWorldPosition(targetBody, simTimeSec)
            val diff = targetPos - focusPos
            focusPos = focusPos + diff * (dt * 5.5f).coerceAtMost(1f)
            shipVelocity = OrbitVec3(0f, 0f, 0f)
          } else {
            if (shipVelocity.length() > 1f) {
              focusPos = focusPos + shipVelocity * dt
              shipVelocity = shipVelocity * 0.90f
            } else {
              shipVelocity = OrbitVec3(0f, 0f, 0f)
            }
          }

          var bestBody = bodies.first()
          var bestDist = Float.MAX_VALUE
          for (b in bodies) {
            val d = (OrbitalMechanics.bodyWorldPosition(b, simTimeSec) - focusPos).length()
            if (d < bestDist) {
              bestDist = d
              bestBody = b
            }
          }
          nearPlanet = bestBody
        }
        lastNanos = nanos
      }
    }
  }

  Box(modifier = modifier.fillMaxSize().background(SpaceVoid)) {
    // 3D Universe Canvas
    Canvas(
      modifier =
        Modifier.fillMaxSize()
          .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
              camYaw = (camYaw + pan.x * 0.35f) % 360f
              camPitch = (camPitch - pan.y * 0.30f).coerceIn(-82f, 82f)
              if (zoom != 1f) {
                camDistance = (camDistance / zoom).coerceIn(75f, 1150f)
              }
            }
          }
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              val hit =
                lastProjectedBodies
                  .sortedBy { it.depth }
                  .firstOrNull { proj ->
                    val hitRadius = (proj.screenRadius + 26f).coerceAtLeast(38f)
                    hypot(tapOffset.x - proj.screenPos.x, tapOffset.y - proj.screenPos.y) <= hitRadius
                  }
              if (hit != null) {
                lockedPlanetId = hit.body.id
                camDistance = (hit.body.sceneVisualRadius * 7.2f).coerceIn(95f, 280f)
              }
            }
          },
    ) {
      val screenCenter = Offset(size.width * 0.5f, size.height * 0.5f)
      val focalLength = size.minDimension * 0.95f

      drawRect(
        brush =
          Brush.radialGradient(
            colors = listOf(Color(0xFF172454), SpaceVoid),
            center = screenCenter,
            radius = size.maxDimension * 0.7f,
          )
      )

      // 3D Starfield
      deepStars.forEachIndexed { idx, starPos ->
        val p = projectPoint(starPos, focusPos * 0.25f, camYaw, camPitch, camDistance, screenCenter, focalLength)
        if (p != null) {
          val alpha = (0.35f + (idx % 4) * 0.16f).coerceIn(0.2f, 0.9f)
          drawCircle(
            color = if (idx % 7 == 0) CosmicCyan.copy(alpha = alpha) else Color.White.copy(alpha = alpha),
            radius = if (idx % 9 == 0) 2.2f else 1.3f,
            center = p.first,
          )
        }
      }

      // Ecliptic Reference Plane (y = 0) & Inclined Elliptical 3D Orbits
      if (showOrbits) {
        drawEclipticReferencePlane(
          camFocus = focusPos,
          camYaw = camYaw,
          camPitch = camPitch,
          camDistance = camDistance,
          screenCenter = screenCenter,
          focalLength = focalLength,
        )
        bodies.drop(1).forEach { body ->
          draw3DKeplerOrbit(
            body = body,
            simTimeSec = simTimeSec,
            camFocus = focusPos,
            camYaw = camYaw,
            camPitch = camPitch,
            camDistance = camDistance,
            screenCenter = screenCenter,
            focalLength = focalLength,
            highlighted = lockedPlanetId == body.id || nearPlanet?.id == body.id,
          )
        }
      }

      // 3D Asteroid Belt
      asteroids.forEach { (r, angleOffset, yAndSpeed) ->
        val angleRad = Math.toRadians((angleOffset + simTimeSec * yAndSpeed.second).toDouble())
        val pos = OrbitVec3((cos(angleRad) * r).toFloat(), yAndSpeed.first, (sin(angleRad) * r).toFloat())
        val proj = projectPoint(pos, focusPos, camYaw, camPitch, camDistance, screenCenter, focalLength)
        if (proj != null) {
          drawCircle(
            color = Color(0xFF94A3B8).copy(alpha = 0.65f),
            radius = (2.8f * proj.second).coerceIn(1.1f, 4.2f),
            center = proj.first,
          )
        }
      }

      // Project & Z-sort celestial bodies back-to-front
      val projected =
        bodies.mapNotNull { body ->
          val wPos = OrbitalMechanics.bodyWorldPosition(body, simTimeSec)
          val p = projectPoint(wPos, focusPos, camYaw, camPitch, camDistance, screenCenter, focalLength)
          if (p != null) {
            ProjectedBody(
              body = body,
              worldPos = wPos,
              screenPos = p.first,
              screenRadius = (body.sceneVisualRadius * p.second).coerceIn(5f, size.minDimension * 0.32f),
              depth = p.third,
            )
          } else {
            null
          }
        }.sortedByDescending { it.depth }

      lastProjectedBodies = projected

      projected.forEach { item ->
        drawScenePlanet(item, simTimeSec)
        drawPlanetLabel(item, textMeasurer, isSelected = lockedPlanetId == item.body.id)
      }

      // Draw Chibi Spaceship oriented along flight direction
      val shipFlying = shipVelocity.length() > 10f
      val rocketCenter =
        if (lockedPlanetId == null) {
          screenCenter
        } else {
          // Orbit gently beside the locked planet
          val orbitAngle = simTimeSec * 1.4f
          Offset(
            screenCenter.x + cos(orbitAngle) * 72f,
            screenCenter.y + sin(orbitAngle) * 32f,
          )
        }
      drawExplorerRocket(
        center = rocketCenter,
        angleDeg = if (lockedPlanetId == null) rocketHeadingDeg else (simTimeSec * 80f) % 360f,
        thrusting = shipFlying || lockedPlanetId != null,
        suitColor = SolarSystemData.suitColors.getOrElse(chibiProfile.suitColorIndex) { CosmicAmber },
        phase = simTimeSec * 10f,
      )
    }

    // TOP HUD: Planet Radar Bar + Controls + Zoom
    Column(
      modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 8.dp),
    ) {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
      ) {
        items(bodies) { body ->
          val selected = lockedPlanetId == body.id
          FilterChip(
            selected = selected,
            onClick = {
              lockedPlanetId = body.id
              camDistance = (body.sceneVisualRadius * 7.2f).coerceIn(95f, 280f)
            },
            label = {
              Text(
                text = body.name.removePrefix("Il ").removePrefix("La "),
                style = MaterialTheme.typography.labelLarge,
              )
            },
            leadingIcon = {
              Box(modifier = Modifier.size(12.dp).background(body.primaryColor, CircleShape))
            },
            colors =
              FilterChipDefaults.filterChipColors(
                containerColor = SpaceNavy.copy(alpha = 0.85f),
                labelColor = StarlightWhite,
                selectedContainerColor = CosmicAmber,
                selectedLabelColor = ComicInk,
              ),
            modifier = Modifier.testTag("explore_radar_${body.id}"),
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          onClick = {
            timeSpeedMultiplier =
              when (timeSpeedMultiplier) {
                0f -> 1f
                1f -> 4f
                4f -> 12f
                else -> 0f
              }
          },
          color = SpaceCard.copy(alpha = 0.88f),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f)),
          modifier = Modifier.testTag("explore_speed_button"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector =
                when (timeSpeedMultiplier) {
                  0f -> Icons.Rounded.Pause
                  1f -> Icons.Rounded.PlayArrow
                  else -> Icons.Rounded.FastForward
                },
              contentDescription = "Velocità orbite",
              tint = CosmicCyan,
              modifier = Modifier.size(17.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (timeSpeedMultiplier == 0f) "Pausa" else "${timeSpeedMultiplier.toInt()}x",
              style = MaterialTheme.typography.labelMedium,
              color = StarlightWhite,
            )
          }
        }

        Surface(
          onClick = { showOrbits = !showOrbits },
          color = SpaceCard.copy(alpha = 0.88f),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, if (showOrbits) CosmicAmber else StarlightMuted.copy(alpha = 0.3f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = if (showOrbits) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
              contentDescription = "Mostra orbite",
              tint = if (showOrbits) CosmicAmber else StarlightMuted,
              modifier = Modifier.size(17.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Orbite", style = MaterialTheme.typography.labelMedium, color = StarlightWhite)
          }
        }

        Surface(
          onClick = {
            lockedPlanetId = null
            focusPos = OrbitVec3(0f, 0f, 0f)
            shipVelocity = OrbitVec3(0f, 0f, 0f)
            camYaw = -22f
            camPitch = 18f
            camDistance = 590f
          },
          color = SpaceCard.copy(alpha = 0.88f),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, NebulaMagenta.copy(alpha = 0.5f)),
          modifier = Modifier.testTag("explore_reset_button"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.CenterFocusStrong,
              contentDescription = "Vista Sistema",
              tint = NebulaMagenta,
              modifier = Modifier.size(17.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sistema", style = MaterialTheme.typography.labelMedium, color = StarlightWhite)
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Dedicated Zoom -/+ buttons for effortless one-hand zoom
        Surface(
          onClick = { camDistance = (camDistance * 1.28f).coerceIn(75f, 1150f) },
          color = SpaceCard.copy(alpha = 0.88f),
          shape = CircleShape,
          border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f)),
          modifier = Modifier.size(34.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Remove, contentDescription = "Allontana", tint = StarlightWhite, modifier = Modifier.size(18.dp))
          }
        }
        Surface(
          onClick = { camDistance = (camDistance * 0.78f).coerceIn(75f, 1150f) },
          color = SpaceCard.copy(alpha = 0.88f),
          shape = CircleShape,
          border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f)),
          modifier = Modifier.size(34.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Add, contentDescription = "Avvicina", tint = StarlightWhite, modifier = Modifier.size(18.dp))
          }
        }
      }

      if (onOpenFirstPersonCockpit != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Button(
          onClick = onOpenFirstPersonCockpit,
          colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.padding(horizontal = 12.dp).height(38.dp).testTag("launch_cockpit_simulator_button"),
        ) {
          Icon(Icons.Rounded.Flight, contentDescription = null, modifier = Modifier.size(17.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Decolla in Cabina 1ª Persona (Landscape)", style = MaterialTheme.typography.labelLarge)
        }
      }
    }

    // BOTTOM HUD: Nearest Planet Chibi Card + Virtual Joystick & Quota Buttons
    Row(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 12.dp),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      val activeBody = lockedPlanetId?.let { SolarSystemData.getPlanetById(it) } ?: nearPlanet
      if (activeBody != null) {
        Surface(
          color = ComicBubbleCream.copy(alpha = 0.96f),
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(2.5.dp, ComicInk),
          shadowElevation = 8.dp,
          modifier = Modifier.weight(1f),
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            ChibiAvatar(
              profile = chibiProfile,
              expression = if (lockedPlanetId != null) ChibiExpression.AMAZED else ChibiExpression.ROCKET,
              size = 52.dp,
              showBackdrop = true,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text =
                  if (lockedPlanetId != null) {
                    "In orbita: ${activeBody.name}"
                  } else {
                    "Vicino a: ${activeBody.name}"
                  },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ComicInk,
                maxLines = 1,
              )
              val orbitEl = OrbitalMechanics.elementsFor(activeBody.id)
              val incStr = String.format(Locale.ITALIAN, "%.1f°", orbitEl.inclinationDeg)
              val hookOrOrbit =
                if (activeBody.id == "sun") activeBody.quickHook
                else "Inclinazione eclittica: $incStr • ${activeBody.quickHook}"
              Text(
                text = hookOrOrbit,
                style = MaterialTheme.typography.bodySmall,
                color = ComicInk.copy(alpha = 0.85f),
                maxLines = 2,
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                  onClick = { onOpenPlanetDetail(activeBody.id) },
                  colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(32.dp).testTag("explore_inspect_button"),
                ) {
                  Icon(Icons.Rounded.Explore, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("3D", style = MaterialTheme.typography.labelMedium)
                }
                if (activeBody.id != "sun") {
                  Button(
                    onClick = { onOpenLandingSimulator(activeBody.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp).testTag("explore_land_button"),
                  ) {
                    Icon(Icons.Rounded.FlightLand, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Atterra!", style = MaterialTheme.typography.labelMedium)
                  }
                }
              }
            }
          }
        }
      }

      // Altitude Up/Down + Responsive Virtual Flight Joystick
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("space_joystick"),
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          HoldThrusterPill(
            icon = Icons.Rounded.ArrowUpward,
            contentDescription = "Sali di quota",
            onPressChanged = { pressed -> verticalThrust = if (pressed) -1f else 0f },
          )
          HoldThrusterPill(
            icon = Icons.Rounded.ArrowDownward,
            contentDescription = "Scendi di quota",
            onPressChanged = { pressed -> verticalThrust = if (pressed) 1f else 0f },
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        VirtualFlightJoystick(
          onJoyChange = { x, y ->
            joyX = x
            joyY = y
          },
        )
      }
    }
  }
}

@Composable
private fun HoldThrusterPill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDescription: String,
  onPressChanged: (Boolean) -> Unit,
) {
  var pressed by remember { mutableStateOf(false) }
  Surface(
    color = if (pressed) CosmicAmber else SpaceNavy.copy(alpha = 0.9f),
    shape = CircleShape,
    border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.7f)),
    modifier =
      Modifier.size(36.dp)
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = {
              pressed = true
              onPressChanged(true)
              tryAwaitRelease()
              pressed = false
              onPressChanged(false)
            },
          )
        },
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (pressed) ComicInk else CosmicCyan,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
private fun VirtualFlightJoystick(
  onJoyChange: (Float, Float) -> Unit,
) {
  var dragOffset by remember { mutableStateOf(Offset.Zero) }
  var boxSizePx by remember { mutableFloatStateOf(300f) }
  val maxRadiusPx = (boxSizePx * 0.34f).coerceAtLeast(30f)

  Box(
    modifier =
      Modifier.size(108.dp)
        .onSizeChanged { size ->
          boxSizePx = minOf(size.width, size.height).toFloat()
        }
        .pointerInput(maxRadiusPx) {
          detectDragGestures(
            onDragEnd = {
              dragOffset = Offset.Zero
              onJoyChange(0f, 0f)
            },
            onDragCancel = {
              dragOffset = Offset.Zero
              onJoyChange(0f, 0f)
            },
            onDrag = { change, dragAmount ->
              change.consume()
              val next = dragOffset + dragAmount
              val dist = hypot(next.x, next.y)
              val clamped =
                if (dist > maxRadiusPx) {
                  Offset(next.x / dist * maxRadiusPx, next.y / dist * maxRadiusPx)
                } else {
                  next
                }
              dragOffset = clamped
              onJoyChange(
                (clamped.x / maxRadiusPx).coerceIn(-1f, 1f),
                (clamped.y / maxRadiusPx).coerceIn(-1f, 1f),
              )
            },
          )
        },
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val c = Offset(size.width / 2f, size.height / 2f)
      val outerR = size.minDimension * 0.46f
      val knobR = size.minDimension * 0.19f
      drawCircle(color = SpaceCard.copy(alpha = 0.88f), radius = outerR, center = c)
      drawCircle(color = CosmicCyan.copy(alpha = 0.72f), radius = outerR, center = c, style = Stroke(width = 3f))
      drawLine(color = Color.White.copy(alpha = 0.18f), start = Offset(c.x - outerR * 0.65f, c.y), end = Offset(c.x + outerR * 0.65f, c.y), strokeWidth = 2f)
      drawLine(color = Color.White.copy(alpha = 0.18f), start = Offset(c.x, c.y - outerR * 0.65f), end = Offset(c.x, c.y + outerR * 0.65f), strokeWidth = 2f)

      val knob = c + dragOffset
      drawCircle(color = CosmicAmber, radius = knobR, center = knob)
      drawCircle(color = ComicInk, radius = knobR, center = knob, style = Stroke(width = 2.5f))
    }
  }
}

private fun DrawScope.drawEclipticReferencePlane(
  camFocus: OrbitVec3,
  camYaw: Float,
  camPitch: Float,
  camDistance: Float,
  screenCenter: Offset,
  focalLength: Float,
) {
  // Dashed crosshair axes on y = 0 ecliptic plane + outer ecliptic rim
  val extent = 540f
  val axes =
    listOf(
      OrbitVec3(-extent, 0f, 0f) to OrbitVec3(extent, 0f, 0f),
      OrbitVec3(0f, 0f, -extent) to OrbitVec3(0f, 0f, extent),
    )
  axes.forEach { (a, b) ->
    val p1 = projectPoint(a, camFocus, camYaw, camPitch, camDistance, screenCenter, focalLength)
    val p2 = projectPoint(b, camFocus, camYaw, camPitch, camDistance, screenCenter, focalLength)
    if (p1 != null && p2 != null) {
      drawLine(
        color = CosmicCyan.copy(alpha = 0.14f),
        start = p1.first,
        end = p2.first,
        strokeWidth = 1.2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
      )
    }
  }
}

private fun DrawScope.draw3DKeplerOrbit(
  body: CelestialBody,
  simTimeSec: Float,
  camFocus: OrbitVec3,
  camYaw: Float,
  camPitch: Float,
  camDistance: Float,
  screenCenter: Offset,
  focalLength: Float,
  highlighted: Boolean,
) {
  val points = OrbitalMechanics.sampleOrbitLoop(body, simTimeSec, segments = 64)
  if (points.isEmpty()) return
  val path = Path()
  var started = false
  points.forEach { pos ->
    val proj = projectPoint(pos, camFocus, camYaw, camPitch, camDistance, screenCenter, focalLength)
    if (proj != null) {
      if (!started) {
        path.moveTo(proj.first.x, proj.first.y)
        started = true
      } else {
        path.lineTo(proj.first.x, proj.first.y)
      }
    } else {
      started = false
    }
  }
  drawPath(
    path = path,
    color = if (highlighted) body.primaryColor.copy(alpha = 0.88f) else body.primaryColor.copy(alpha = 0.34f),
    style = Stroke(width = if (highlighted) 3.4f else 1.5f),
  )
}

private fun DrawScope.drawScenePlanet(
  item: ProjectedBody,
  simTimeSec: Float,
) {
  val body = item.body
  val c = item.screenPos
  val r = item.screenRadius

  body.atmosphereColor?.let { glow ->
    val glowMult = if (body.id == "sun") 1.75f + 0.08f * sin(simTimeSec * 3f) else 1.28f
    drawCircle(
      brush = Brush.radialGradient(colors = listOf(glow.copy(alpha = 0.45f), Color.Transparent), center = c, radius = r * glowMult),
      radius = r * glowMult,
      center = c,
    )
  }

  if (body.hasRings) {
    drawArc(
      color = body.ringColor.copy(alpha = 0.72f),
      startAngle = 180f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(c.x - r * 2.1f, c.y - r * 0.62f),
      size = Size(r * 4.2f, r * 1.24f),
      style = Stroke(width = (r * 0.25f).coerceAtLeast(2f)),
    )
  }

  drawCircle(
    brush =
      Brush.radialGradient(
        colors = listOf(body.accentColor, body.primaryColor, body.secondaryColor),
        center = Offset(c.x - r * 0.3f, c.y - r * 0.3f),
        radius = r * 1.4f,
      ),
    radius = r,
    center = c,
  )

  if (body.hasRings) {
    drawArc(
      color = body.ringColor.copy(alpha = 0.85f),
      startAngle = 0f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(c.x - r * 2.1f, c.y - r * 0.62f),
      size = Size(r * 4.2f, r * 1.24f),
      style = Stroke(width = (r * 0.25f).coerceAtLeast(2f)),
    )
  }
}

private fun DrawScope.drawPlanetLabel(
  item: ProjectedBody,
  textMeasurer: TextMeasurer,
  isSelected: Boolean,
) {
  val label = item.body.name.removePrefix("Il ").removePrefix("La ")
  val style =
    TextStyle(
      fontFamily = FredokaFontFamily,
      fontSize = if (isSelected) 13.sp else 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (isSelected) CosmicAmber else StarlightWhite,
    )
  val measured = textMeasurer.measure(label, style)
  val tx = item.screenPos.x - measured.size.width / 2f
  val ty = item.screenPos.y + item.screenRadius + 6f
  if (tx > 0f && tx + measured.size.width < size.width && ty > 0f && ty < size.height - 20f) {
    drawText(measured, topLeft = Offset(tx, ty))
  }
}

private fun DrawScope.drawExplorerRocket(
  center: Offset,
  angleDeg: Float,
  thrusting: Boolean,
  suitColor: Color,
  phase: Float,
) {
  rotate(degrees = angleDeg, pivot = center) {
    if (thrusting) {
      val flameLen = 16f + 7f * sin(phase * 2.5f)
      drawOval(
        color = CosmicAmber,
        topLeft = Offset(center.x - 5.5f, center.y + 9f),
        size = Size(11f, flameLen),
      )
      drawOval(
        color = CosmicCyan,
        topLeft = Offset(center.x - 3f, center.y + 10f),
        size = Size(6f, flameLen * 0.6f),
      )
    }
    val bodyPath =
      Path().apply {
        moveTo(center.x, center.y - 18f)
        lineTo(center.x + 10f, center.y + 9f)
        lineTo(center.x - 10f, center.y + 9f)
        close()
      }
    drawPath(bodyPath, color = Color.White)
    drawPath(bodyPath, color = ComicInk, style = Stroke(width = 2.5f))
    drawCircle(color = suitColor, radius = 4.5f, center = Offset(center.x, center.y - 2f))
  }
}
