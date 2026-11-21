package com.ricrug.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.RotateLeft
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricrug.data.OrbitVec3
import com.ricrug.data.OrbitalMechanics
import com.ricrug.data.SolarSystemData
import com.ricrug.model.CelestialBody
import com.ricrug.model.ChibiExpression
import com.ricrug.model.ChibiProfile
import com.ricrug.model.PlanetSurfaceStyle
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.FredokaFontFamily
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceCard
import com.ricrug.ui.theme.SpaceCardElevated
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
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

private fun getCockpitCoPilotDialogue(
  nearestBody: CelestialBody,
  nearestDist: Float,
  sunDist: Float,
  boostActive: Boolean,
  throttle: Float,
  isAutopilot: Boolean,
  chibiName: String,
): Pair<String, ChibiExpression> {
  // Extreme proximity to Sun (< 75f)
  if (sunDist < 58f) {
    return Pair(
      "🔥 AIUTO! SCUDI AL 200%! I popcorn sul cruscotto sono già scoppiati! Vira via prima di diventare un toast spaziale!",
      ChibiExpression.AMAZED,
    )
  }
  if (sunDist < 100f) {
    return Pair(
      "☀️ ALLARME CALORE SOLARE: 5.500°C! Il condizionatore dell'astronave è andato in fumo! Comandante $chibiName, servono occhiali da sole galattici!",
      ChibiExpression.AMAZED,
    )
  }

  // Planet-specific funny & educational dialogues when near (< 60f)
  if (nearestDist < 60f) {
    return when (nearestBody.id) {
      "sun" -> Pair(
        "🔥 ALLARME FUSIONE! Il Sole sprigiona milioni di gradi nel nucleo! Rischiamo di arrostire le antenne radio!",
        ChibiExpression.AMAZED,
      )
      "mercury" -> Pair(
        "🌡️ Mercurio all'orizzonte! Di giorno ci cuoci le uova sul muso (430°C), di notte congelatore a -180°C! Niente mezze misure!",
        ChibiExpression.THINKING,
      )
      "venus" -> Pair(
        "☁️ Allarme effetto serra estremo! Su Venere piove acido solforico e ci sono 465°C: più caldo di una friggitrice ad aria! Finestrini sigillati!",
        ChibiExpression.AMAZED,
      )
      "earth" -> Pair(
        "🌍 Casa dolce casa! I sensori rilevano pizza margherita, cioccolata e Wi-Fi gratuito! Sosta rifornimento snack, Comandante $chibiName?",
        ChibiExpression.CHEERING,
      )
      "moon" -> Pair(
        "🌕 Gravità lunare ridotta! Qui si salta a 3 metri di quota! E sfatiamo il mito: i crateri non sono fatti di formaggio coi buchi!",
        ChibiExpression.CHEERING,
      )
      "mars" -> Pair(
        "🔴 Benvenuti sul Pianeta Rosso! Spolverate i sensori: tempesta di polvere di ruggine! Attenti al Monte Olimpo: è alto 3 volte l'Everest!",
        ChibiExpression.AMAZED,
      )
      "jupiter" -> Pair(
        "🌪️ GIOVE GIGANTE! Quella Grande Macchia Rossa è un uragano mostruoso che dura da 400 anni! E vietato atterrare: è tutta una palla di gas!",
        ChibiExpression.AMAZED,
      )
      "saturn" -> Pair(
        "🪐 Spettacolo cosmico! Gli anelli di Saturno sono fatti di miliardi di cubetti di ghiaccio... Se solo avessimo una limonata gigante!",
        ChibiExpression.AMAZED,
      )
      "uranus" -> Pair(
        "🧊 Brrr, che freddo (-220°C)! Urano rotola coricato di lato come una palla da bowling e sa di zolfo! Chiudete i filtri dell'aria!",
        ChibiExpression.THINKING,
      )
      "neptune" -> Pair(
        "💨 Venti supersonici a 2.100 km/h! Qui i cristalli di metano formano piogge di diamanti nel cielo... Tiriamo fuori un retino?",
        ChibiExpression.AMAZED,
      )
      "pluto" -> Pair(
        "❤️ Ciao piccolo Plutone! Guarda che bel cuore di ghiaccio ha sulla superficie! Sarà pure un pianeta nano, ma noi gli vogliamo un mondo di bene!",
        ChibiExpression.CHEERING,
      )
      else -> Pair(
        "🛰️ Avvicinamento orbitale a ${nearestBody.name}! Sensori scientifici attivi a piena potenza!",
        ChibiExpression.CHEERING,
      )
    }
  }

  // General flight states
  if (boostActive) {
    return Pair(
      "🚀 PROPULSIONE WARP AL 100%! Le stelle sembrano spaghetti luminosi! Tenetevi fortissimo, Comandante $chibiName!",
      ChibiExpression.ROCKET,
    )
  }
  if (isAutopilot) {
    return Pair(
      "🧭 Autopilota di puntamento attivo: prua allineata verso le coordinate spaziali!",
      ChibiExpression.HAPPY,
    )
  }
  if (throttle < -0.05f) {
    return Pair(
      "🛑 Motori di manovra in retromarcia! Fischiano i sensori di parcheggio spaziale: BEEP... BEEP... BEEP!",
      ChibiExpression.THINKING,
    )
  }
  if (throttle == 0f) {
    return Pair(
      "🛸 Motori al minimo: veleggiando dolcemente nel silenzio del cosmo. Tutti i sistemi nominali, Comandante $chibiName!",
      ChibiExpression.HAPPY,
    )
  }
  return Pair(
    "✨ Rotta libera nello spazio profondo! Toccate un pianeta sul radar per impostare la navigazione!",
    ChibiExpression.HAPPY,
  )
}

@Composable
fun FirstPersonFlightSimulator3D(
  chibiProfile: ChibiProfile,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenLandingSimulator: (String) -> Unit,
  onExitSimulator: () -> Unit,
  onOpenAsteroidDodge: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val bodies = SolarSystemData.celestialBodies
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  // Ship starts in the inner solar system looking toward the Sun & planetary plane
  var shipPos by remember { mutableStateOf(OrbitVec3(-110f, 26f, 175f)) }

  // Spherical Euler orientation:
  // - shipHeading: Azimuth angle in XZ plane (radians)
  // - shipPitch: Elevation angle above/below ecliptic plane (radians), clamped to prevent gimbal flip
  // - shipBank: Dynamic roll angle (radians), automatically banking into turns and auto-leveling
  // - manualRollOffset: Pilot-set wing roll bias via cockpit trim buttons
  var shipHeading by remember { mutableFloatStateOf(-1.10f) }
  var shipPitch by remember { mutableFloatStateOf(-0.10f) }
  var shipBank by remember { mutableFloatStateOf(0f) }
  var manualRollOffset by remember { mutableFloatStateOf(0f) }

  // Smooth angular velocities for natural, responsive inertia
  var smoothYawVel by remember { mutableFloatStateOf(0f) }
  var smoothPitchVel by remember { mutableFloatStateOf(0f) }

  // Orthonormal frame derived from Euler angles
  var shipForward by remember { mutableStateOf(OrbitVec3(0.45f, -0.10f, -0.88f).normalized()) }
  var shipUp by remember { mutableStateOf(OrbitVec3(0f, 1f, 0f)) }

  // Flight controls state - Start stationary at 0 throttle so ship doesn't move autonomously
  var throttle by remember { mutableFloatStateOf(0f) } // -0.35 (reverse) .. 1.0 (warp)
  var currentSpeed by remember { mutableFloatStateOf(0f) }
  var boostActive by remember { mutableStateOf(false) }

  // Left Thumb: Cloche di Volo 3D (stickX = Virata/Yaw, stickY = Beccheggio/Pitch)
  var stickX by remember { mutableFloatStateOf(0f) }
  var stickY by remember { mutableFloatStateOf(0f) }

  val hasStick = abs(stickX) > 0.03f || abs(stickY) > 0.03f
  val sunDist = shipPos.length()

  // Autopilot & simulation state
  var autopilotTargetId by remember { mutableStateOf<String?>(null) }
  var isAutopilotSteering by remember { mutableStateOf(false) }
  // Scala temporale orbite: 0 = 1x Reale Astronomico (default), 1 = 100x Time-Lapse, 2 = Orbite Ferme
  var timeWarpMode by remember { mutableIntStateOf(0) }
  var invertPitch by remember { mutableStateOf(false) }
  var simTimeSec by remember { mutableFloatStateOf(14f) }
  var nearestBody by remember { mutableStateOf<CelestialBody>(bodies.first()) }
  var nearestDist by remember { mutableFloatStateOf(100f) }
  var lastProjected by remember { mutableStateOf<List<CockpitProjectedBody>>(emptyList()) }

  // Animated cockpit navigation beacon & warp transitions
  val infiniteTransition = rememberInfiniteTransition(label = "spaceshipAnim")
  val beaconBlink by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
    label = "beacon",
  )
  val warpPulse by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Restart),
    label = "warpPulse",
  )

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
          val timeScaleMultiplier = when (timeWarpMode) {
            0 -> 1.0f // 1x Velocita astronomica reale
            1 -> 100.0f // 100x Time-Lapse
            else -> 0.0f // Orbite ferme
          }
          if (timeWarpMode != 2) {
            simTimeSec += dt
          }

          if (hasStick) {
            isAutopilotSteering = false
            // Yaw (virata): stickX > 0 (destra) -> vira a destra
            val targetYawVel = -stickX * 1.65f
            // Pitch (beccheggio): Arcade (spingi su = muso in alto) vs Aero (spingi su = picchiata)
            val pitchFactor = if (invertPitch) 1.35f else -1.35f
            val targetPitchVel = stickY * pitchFactor
            smoothYawVel += (targetYawVel - smoothYawVel) * (dt * 10.0f).coerceAtMost(1f)
            smoothPitchVel += (targetPitchVel - smoothPitchVel) * (dt * 10.0f).coerceAtMost(1f)

            shipHeading += smoothYawVel * dt
            shipPitch = (shipPitch + smoothPitchVel * dt).coerceIn(-1.42f, 1.42f)

            // Aerodynamic coordinated banking into turns: bank rolls proportionally to yaw turn
            val targetBank = (-stickX * 0.38f) + manualRollOffset
            shipBank += (targetBank - shipBank) * (dt * 6.5f).coerceAtMost(1f)
          } else {
            // Natural angular deceleration & auto-leveling to manualRollOffset (or 0)
            smoothYawVel += (0f - smoothYawVel) * (dt * 8.0f).coerceAtMost(1f)
            smoothPitchVel += (0f - smoothPitchVel) * (dt * 8.0f).coerceAtMost(1f)
            if (abs(smoothYawVel) > 0.001f) shipHeading += smoothYawVel * dt
            if (abs(smoothPitchVel) > 0.001f) shipPitch = (shipPitch + smoothPitchVel * dt).coerceIn(-1.42f, 1.42f)

            // Auto-level back to horizon
            shipBank += (manualRollOffset - shipBank) * (dt * 5.5f).coerceAtMost(1f)
          }

          if (isAutopilotSteering && autopilotTargetId != null) {
            val targetBody = SolarSystemData.getPlanetById(autopilotTargetId!!)
            val targetPos = OrbitalMechanics.bodyWorldPosition(targetBody, simTimeSec, useRealisticSpeed = true, timeScaleMultiplier = timeScaleMultiplier)
            val toTarget = targetPos - shipPos
            val desiredDir = toTarget.normalized()

            val desiredPitch = asin(desiredDir.y.coerceIn(-0.98f, 0.98f))
            val desiredHeading = atan2(desiredDir.z, desiredDir.x)

            var deltaH = desiredHeading - shipHeading
            while (deltaH > PI) deltaH -= (2 * PI).toFloat()
            while (deltaH < -PI) deltaH += (2 * PI).toFloat()
            val deltaP = desiredPitch - shipPitch

            val steerRate = (dt * 2.8f).coerceAtMost(1f)
            shipHeading += deltaH * steerRate
            shipPitch += deltaP * steerRate

            // Dynamic banking into the turn during autopilot re-alignment
            val targetBank = (-deltaH.coerceIn(-0.8f, 0.8f) * 0.42f)
            shipBank += (targetBank - shipBank) * (dt * 5.0f).coerceAtMost(1f)

            if (abs(deltaH) < 0.025f && abs(deltaP) < 0.025f) {
              isAutopilotSteering = false
            }
          }

          // Compute exact orthonormal ship orientation from Euler angles (Gimbal-free)
          val cosP = cos(shipPitch)
          val sinP = sin(shipPitch)
          val cosH = cos(shipHeading)
          val sinH = sin(shipHeading)
          val cosB = cos(shipBank)
          val sinB = sin(shipBank)

          val fwd = OrbitVec3(cosP * cosH, sinP, cosP * sinH).normalized()
          val baseRight = OrbitVec3(sinH, 0f, -cosH).normalized()
          val baseUp = fwd.cross(baseRight).normalized()

          val right = (baseRight * cosB + baseUp * sinB).normalized()
          val up = (baseUp * cosB - baseRight * sinB).normalized()

          shipForward = fwd
          shipUp = up

          val maxForwardSpeed = if (boostActive) 220f else 135f
          val targetSpeed = throttle * maxForwardSpeed
          // Active damping when throttle is 0 so the ship comes to a complete rest
          if (abs(throttle) < 0.02f) {
            currentSpeed += (0f - currentSpeed) * (dt * 6.5f).coerceAtMost(1f)
            if (abs(currentSpeed) < 0.08f) currentSpeed = 0f
          } else {
            currentSpeed += (targetSpeed - currentSpeed) * (dt * 4.5f).coerceAtMost(1f)
          }

          if (abs(currentSpeed) > 0.005f) {
            shipPos += shipForward * (currentSpeed * dt)
          }

          if (shipPos.length() > 760f) {
            shipPos = shipPos.normalized() * 760f
          }

          var bestBody = bodies.first()
          var bestDist = Float.MAX_VALUE
          bodies.forEach { b ->
            val d = (OrbitalMechanics.bodyWorldPosition(b, simTimeSec, useRealisticSpeed = true, timeScaleMultiplier = timeScaleMultiplier) - shipPos).length()
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

  // Full-Screen Immersive Landscape Spaceship Cockpit
  Box(
    modifier = modifier.fillMaxSize().background(SpaceVoid).testTag("first_person_flight_sim"),
  ) {
    // 1. Full-Bleed 3D Cockpit Windshield & Exterior Spaceship Visuals
    Canvas(
      modifier =
        Modifier.fillMaxSize()
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              // Only process touch gestures in windshield zone (upper 68%) to prevent interference with dashboard controls
              if (change.position.y < size.height * 0.68f) {
                change.consume()
                isAutopilotSteering = false
                shipHeading -= dragAmount.x * 0.0032f
                shipPitch = (shipPitch - dragAmount.y * 0.0030f).coerceIn(-1.42f, 1.42f)
              }
            }
          }
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              if (tapOffset.y < size.height * 0.68f) {
                val hit =
                  lastProjected.asReversed().firstOrNull { proj ->
                    val hitR = proj.screenRadius.coerceAtLeast(26f)
                    hypot(tapOffset.x - proj.screenPos.x, tapOffset.y - proj.screenPos.y) <= hitR
                  }
                if (hit != null) {
                  autopilotTargetId = hit.body.id
                  isAutopilotSteering = false
                }
              }
            }
          },
    ) {
      // Deep space background with radial nebular falloff
      drawRect(Brush.radialGradient(listOf(Color(0xFF090E26), SpaceVoid), radius = size.maxDimension * 0.85f))

      val center = Offset(size.width * 0.5f, size.height * 0.44f)
      val focal = size.minDimension * 1.15f
      // Terna ortonormale destra corretta: Up x Forward = Right
      val shipRight = shipUp.cross(shipForward).normalized()

      fun toCameraSpace(worldPt: OrbitVec3): OrbitVec3 {
        val rel = worldPt - shipPos
        return OrbitVec3(rel.dot(shipRight), rel.dot(shipUp), rel.dot(shipForward))
      }

      val currentMultiplier = when (timeWarpMode) {
        0 -> 1.0f
        1 -> 100.0f
        else -> 0.0f
      }

      // Posizione proiettata del Sole per lo shading fotorealistico dei pianeti
      val sunCam = toCameraSpace(OrbitVec3(0f, 0f, 0f))
      val sunScreen = if (sunCam.z > 0.5f) {
        Offset(center.x + (sunCam.x / sunCam.z) * focal, center.y - (sunCam.y / sunCam.z) * focal)
      } else {
        null
      }

      // 3D Starfield + Speed Warp Streaks
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
            if (speedNorm > 0.25f || boostActive) {
              val dx = sx - center.x
              val dy = sy - center.y
              val streak = (if (boostActive) 0.32f else speedNorm * 0.16f) * distFade
              drawLine(
                color = (if (boostActive) NebulaMagenta else CosmicCyan).copy(alpha = alpha * distFade),
                start = Offset(sx, sy),
                end = Offset(sx + dx * streak, sy + dy * streak),
                strokeWidth = if (boostActive) 2.5f else (1.6f + distFade * 1.4f),
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

      // Warp Tunnel shockwaves when Warp Engine is actively boosting
      if (boostActive) {
        val warpR = size.minDimension * (0.35f + warpPulse * 0.45f)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, CosmicCyan.copy(alpha = 0.25f * (1f - warpPulse)), NebulaMagenta.copy(alpha = 0.35f * (1f - warpPulse))),
            center = center,
            radius = warpR,
          ),
          radius = warpR,
          center = center,
          style = Stroke(width = 4f),
        )
      }

      // Keplerian Planetary Orbits
      bodies.filter { it.orbitDistanceScene > 0f }.forEach { b ->
        val pts = OrbitalMechanics.sampleOrbitLoop(b, simTimeSec, segments = 48, useRealisticSpeed = true, timeScaleMultiplier = currentMultiplier)
        var prevPt: Offset? = null
        pts.forEach { worldPt ->
          val cam = toCameraSpace(worldPt)
          if (cam.z > 6f) {
            val pt = Offset(center.x + (cam.x / cam.z) * focal, center.y - (cam.y / cam.z) * focal)
            val p = prevPt
            if (p != null) {
              drawLine(
                color = b.primaryColor.copy(alpha = if (b.id == autopilotTargetId) 0.65f else 0.22f),
                start = p,
                end = pt,
                strokeWidth = if (b.id == autopilotTargetId) 2.4f else 1.2f,
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
              color = Color(0xFFC5C5D8).copy(alpha = 0.55f),
              radius = (1.6f * focal / cam.z).coerceIn(1f, 3.5f),
              center = Offset(sx, sy),
            )
          }
        }
      }

      // Project Celestial Bodies & Sort back-to-front
      val projected = mutableListOf<CockpitProjectedBody>()
      bodies.forEach { b ->
        val wp = OrbitalMechanics.bodyWorldPosition(b, simTimeSec, useRealisticSpeed = true, timeScaleMultiplier = currentMultiplier)
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
          sunScreenPos = sunScreen,
        )
      }

      // Off-screen Waypoint Indicator
      autopilotTargetId?.let { targetId ->
        val tBody = SolarSystemData.getPlanetById(targetId)
        val cam = toCameraSpace(OrbitalMechanics.bodyWorldPosition(tBody, simTimeSec, useRealisticSpeed = true, timeScaleMultiplier = currentMultiplier))
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

      // ==========================================================
      // 🚀 ASTRONAVE: MUSO, ALI E STRUTTURA ESTERNA
      // ==========================================================
      val shipNoseW = size.width * 0.20f
      val shipNoseBottomY = size.height * 0.88f
      val shipNoseTipY = size.height * 0.58f

      // 1. Muso aerodinamico centrale dell'astronave che punta verso lo spazio
      val nosePath = Path().apply {
        moveTo(center.x, shipNoseTipY)
        lineTo(center.x + shipNoseW * 0.5f, shipNoseBottomY)
        lineTo(center.x - shipNoseW * 0.5f, shipNoseBottomY)
        close()
      }
      drawPath(
        path = nosePath,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF2E3B5E), Color(0xFF161E33), Color(0xFF0C101D)),
          startY = shipNoseTipY,
          endY = shipNoseBottomY,
        ),
      )
      // Linea di cresta e pannellature metalliche del muso
      drawLine(
        brush = Brush.verticalGradient(listOf(CosmicCyan, Color(0xFF1E2844))),
        start = Offset(center.x, shipNoseTipY),
        end = Offset(center.x, shipNoseBottomY),
        strokeWidth = 2.5f,
      )
      // Sensore al plasma pulsante sulla punta del muso
      drawCircle(
        color = CosmicCyan.copy(alpha = 0.85f),
        radius = 4f,
        center = Offset(center.x, shipNoseTipY + 3f),
      )

      // Getti RCS laterali sul muso che si accendono virando con la cloche
      if (stickX < -0.1f) {
        // Getti verso destra per virare a sinistra
        drawLine(
          color = CosmicCyan,
          start = Offset(center.x + shipNoseW * 0.25f, shipNoseTipY + 20f),
          end = Offset(center.x + shipNoseW * 0.25f + 16f * abs(stickX), shipNoseTipY + 20f),
          strokeWidth = 3f,
        )
      } else if (stickX > 0.1f) {
        // Getti verso sinistra per virare a destra
        drawLine(
          color = CosmicCyan,
          start = Offset(center.x - shipNoseW * 0.25f, shipNoseTipY + 20f),
          end = Offset(center.x - shipNoseW * 0.25f - 16f * abs(stickX), shipNoseTipY + 20f),
          strokeWidth = 3f,
        )
      }

      // 2. Estremità alari visibili ai lati inferiori
      // Ala Sinistra
      val leftWingPath = Path().apply {
        moveTo(0f, size.height * 0.65f)
        lineTo(size.width * 0.14f, size.height * 0.76f)
        lineTo(0f, size.height * 0.85f)
        close()
      }
      drawPath(
        path = leftWingPath,
        brush = Brush.horizontalGradient(listOf(Color(0xFF141C30), Color(0xFF283654))),
      )
      // Luce di navigazione di babordo (ROSSA lampeggiante)
      drawCircle(
        color = Color(0xFFFF2A2A).copy(alpha = beaconBlink),
        radius = 5f,
        center = Offset(size.width * 0.13f, size.height * 0.76f),
      )

      // Ala Destra
      val rightWingPath = Path().apply {
        moveTo(size.width, size.height * 0.65f)
        lineTo(size.width * 0.86f, size.height * 0.76f)
        lineTo(size.width, size.height * 0.85f)
        close()
      }
      drawPath(
        path = rightWingPath,
        brush = Brush.horizontalGradient(listOf(Color(0xFF283654), Color(0xFF141C30))),
      )
      // Luce di navigazione di tribordo (VERDE lampeggiante)
      drawCircle(
        color = Color(0xFF00FF7F).copy(alpha = beaconBlink),
        radius = 5f,
        center = Offset(size.width * 0.87f, size.height * 0.76f),
      )

      // Getti RCS alari durante il rollio coordinato o manuale
      if (shipBank > 0.06f) {
        val rPow = (shipBank / 0.38f).coerceIn(0.2f, 1f)
        // Rollio a sinistra: getto verso il basso ala sx, getto verso l'alto ala dx
        drawLine(color = CosmicCyan, start = Offset(size.width * 0.13f, size.height * 0.76f), end = Offset(size.width * 0.13f, size.height * 0.76f + 20f * rPow), strokeWidth = 3f)
        drawLine(color = CosmicCyan, start = Offset(size.width * 0.87f, size.height * 0.76f), end = Offset(size.width * 0.87f, size.height * 0.76f - 20f * rPow), strokeWidth = 3f)
      } else if (shipBank < -0.06f) {
        val rPow = (-shipBank / 0.38f).coerceIn(0.2f, 1f)
        // Rollio a destra: getto verso l'alto ala sx, getto verso il basso ala dx
        drawLine(color = CosmicCyan, start = Offset(size.width * 0.13f, size.height * 0.76f), end = Offset(size.width * 0.13f, size.height * 0.76f - 20f * rPow), strokeWidth = 3f)
        drawLine(color = CosmicCyan, start = Offset(size.width * 0.87f, size.height * 0.76f), end = Offset(size.width * 0.87f, size.height * 0.76f + 20f * rPow), strokeWidth = 3f)
      }

      // ==========================================================
      // 🛡️ CUPOLA E TELAIO CORAZZATO DELLA CABINA DI PILOTAGGIO
      // ==========================================================
      val frameColor = Color(0xFF10172A)
      val frameHighlight = CosmicCyan.copy(alpha = 0.40f)

      // Reticolo nanotecnologico olografico HUD sul vetro temperato
      val gridStepX = 64f
      val gridStepY = 36f
      val maxGridY = size.height * 0.65f
      for (gx in 0..(size.width / gridStepX).toInt()) {
        for (gy in 1..(maxGridY / gridStepY).toInt()) {
          val px = gx * gridStepX + (if (gy % 2 == 1) gridStepX * 0.5f else 0f)
          val py = gy * gridStepY
          drawCircle(color = CosmicCyan.copy(alpha = 0.06f), radius = 1.1f, center = Offset(px, py))
        }
      }

      // Effetto Calore Solare Estremo: Glare e bagliore termico sul parabrezza vicino al Sole
      val sunDist = shipPos.length()
      if (sunDist < 130f) {
        val heatRatio = ((130f - sunDist) / 85f).coerceIn(0f, 1f)
        // Bagliore ambra/arancione di radiazione solare sul vetro
        drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFF7700).copy(alpha = 0.32f * heatRatio),
              Color(0xFFFF2200).copy(alpha = 0.18f * heatRatio),
              Color.Transparent,
            ),
            center = center,
            radius = size.maxDimension * 0.85f,
          ),
        )

        // Sirena Termica a impulsi rossi intermittenti per allarme fusione imminente (< 85 unità)
        if (sunDist < 85f) {
          val strobePulse = (sin(simTimeSec * 9.5f) * 0.5f + 0.5f)
          drawRect(
            brush = Brush.radialGradient(
              colors = listOf(
                Color.Transparent,
                Color(0xFFFF1100).copy(alpha = 0.42f * strobePulse * heatRatio),
              ),
              center = center,
              radius = size.maxDimension * 0.72f,
            ),
          )
        }
      }

      // Arco superiore della cupola (Canopy Roof Strut)
      drawRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF090E1C), Color(0xFF151F38))),
        topLeft = Offset(0f, 0f),
        size = Size(size.width, 24f),
      )
      drawLine(color = frameHighlight, start = Offset(0f, 24f), end = Offset(size.width, 24f), strokeWidth = 1.8f)

      // Bulloni/rivetti metallici in lega di titanio sul montante superiore
      for (i in 1..9) {
        val rx = size.width * (i / 10f)
        drawCircle(color = Color(0xFF283654), radius = 3.2f, center = Offset(rx, 12f))
        drawCircle(color = CosmicCyan.copy(alpha = 0.65f), radius = 1.2f, center = Offset(rx - 0.9f, 11.2f))
      }

      // Montante Diagonale Sinistro
      val strutL = Path().apply {
        moveTo(0f, size.height * 0.30f)
        lineTo(size.width * 0.18f, 0f)
        lineTo(size.width * 0.22f, 0f)
        lineTo(0f, size.height * 0.38f)
        close()
      }
      drawPath(strutL, color = frameColor)
      drawLine(color = frameHighlight, start = Offset(0f, size.height * 0.30f), end = Offset(size.width * 0.18f, 0f), strokeWidth = 2f)

      // Rivetti montante sinistro
      for (i in 1..5) {
        val t = i / 6f
        val rx = size.width * 0.20f * (1f - t)
        val ry = size.height * 0.34f * t
        drawCircle(color = Color(0xFF283654), radius = 2.8f, center = Offset(rx + 6f, ry))
        drawCircle(color = CosmicCyan.copy(alpha = 0.65f), radius = 1f, center = Offset(rx + 5.2f, ry - 0.8f))
      }

      // Montante Diagonale Destro
      val strutR = Path().apply {
        moveTo(size.width, size.height * 0.30f)
        lineTo(size.width * 0.82f, 0f)
        lineTo(size.width * 0.78f, 0f)
        lineTo(size.width, size.height * 0.38f)
        close()
      }
      drawPath(strutR, color = frameColor)
      drawLine(color = frameHighlight, start = Offset(size.width, size.height * 0.30f), end = Offset(size.width * 0.82f, 0f), strokeWidth = 2f)

      // Rivetti montante destro
      for (i in 1..5) {
        val t = i / 6f
        val rx = size.width - size.width * 0.20f * (1f - t)
        val ry = size.height * 0.34f * t
        drawCircle(color = Color(0xFF283654), radius = 2.8f, center = Offset(rx - 6f, ry))
        drawCircle(color = CosmicCyan.copy(alpha = 0.65f), radius = 1f, center = Offset(rx - 6.8f, ry - 0.8f))
      }

      // Riflesso curvo di luce sul vetro temperato della cupola
      val glarePath = Path().apply {
        moveTo(size.width * 0.28f, 25f)
        lineTo(size.width * 0.38f, 25f)
        lineTo(size.width * 0.15f, size.height * 0.60f)
        lineTo(size.width * 0.08f, size.height * 0.60f)
        close()
      }
      drawPath(
        path = glarePath,
        brush = Brush.linearGradient(
          colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
          start = Offset(size.width * 0.38f, 25f),
          end = Offset(size.width * 0.10f, size.height * 0.60f),
        ),
      )

      // ==========================================================
      // 🛰️ HUD TATTICO E STRUMENTI DI VOLO SUL PARABREZZA
      // ==========================================================
      // 1. Bussola Stellare / Azimuth Heading Ribbon in alto
      val headingDeg = ((atan2(shipForward.z.toDouble(), shipForward.x.toDouble()) * 180.0 / PI + 360.0) % 360.0).roundToInt()
      val compassCenter = Offset(center.x, 34f)
      val compassHalfW = 110f
      drawLine(color = CosmicCyan.copy(alpha = 0.4f), start = Offset(compassCenter.x - compassHalfW, compassCenter.y), end = Offset(compassCenter.x + compassHalfW, compassCenter.y), strokeWidth = 1.2f)
      // Ticks della bussola
      for (offset in -4..4) {
        val tickDeg = (headingDeg + offset * 10 + 360) % 360
        val tickX = compassCenter.x + offset * 24f
        val tickH = if (tickDeg % 30 == 0) 8f else 4f
        drawLine(color = CosmicCyan.copy(alpha = 0.5f), start = Offset(tickX, compassCenter.y - tickH), end = Offset(tickX, compassCenter.y), strokeWidth = 1.2f)
        if (tickDeg % 30 == 0) {
          val headingLabel = when (tickDeg) {
            0 -> "E"
            90 -> "S"
            180 -> "W"
            270 -> "N"
            else -> "$tickDeg°"
          }
          val txt = textMeasurer.measure(headingLabel, TextStyle(fontFamily = FredokaFontFamily, fontSize = 8.sp, color = CosmicCyan))
          drawText(txt, topLeft = Offset(tickX - txt.size.width / 2f, compassCenter.y + 2f))
        }
      }
      // Indicatore centrale prua
      drawCircle(color = CosmicAmber, radius = 2.5f, center = Offset(compassCenter.x, compassCenter.y - 2f))

      // 2. Orizzonte Artificiale Militare Spaziale & Mirino Centrale
      val rollRad = shipBank
      val horizonR = 40f
      val hx = cos(rollRad) * horizonR
      val hy = -sin(rollRad) * horizonR
      drawLine(color = CosmicCyan.copy(alpha = 0.65f), start = Offset(center.x - hx, center.y - hy), end = Offset(center.x - hx * 0.35f, center.y - hy * 0.35f), strokeWidth = 2.2f)
      drawLine(color = CosmicCyan.copy(alpha = 0.65f), start = Offset(center.x + hx * 0.35f, center.y + hy * 0.35f), end = Offset(center.x + hx, center.y + hy), strokeWidth = 2.2f)
      drawCircle(color = CosmicCyan.copy(alpha = 0.60f), radius = 12f, center = center, style = Stroke(width = 1.8f))
      drawCircle(color = CosmicAmber, radius = 2.8f, center = center)

      // Scala di Beccheggio Militare (Military Pitch Ladder: +20°, +10°, -10°, -20°)
      val pitchDeg = (shipPitch * 180f / PI).toFloat()
      val normAngle = rollRad + (PI / 2.0).toFloat()
      val nx = cos(normAngle)
      val ny = -sin(normAngle)
      for (step in listOf(20, 10, -10, -20)) {
        val stepOffsetPx = (step - pitchDeg) * 1.55f
        if (abs(stepOffsetPx) < 70f) {
          val stepCx = center.x + nx * stepOffsetPx
          val stepCy = center.y + ny * stepOffsetPx
          val halfW = 28f * (if (abs(step) == 20) 0.8f else 1.0f)
          val p1 = Offset(stepCx - cos(rollRad) * halfW, stepCy - (-sin(rollRad)) * halfW)
          val p2 = Offset(stepCx + cos(rollRad) * halfW, stepCy + (-sin(rollRad)) * halfW)
          drawLine(
            color = CosmicCyan.copy(alpha = if (step < 0) 0.32f else 0.48f),
            start = p1,
            end = p2,
            strokeWidth = 1.4f,
          )
          val stepTxt = textMeasurer.measure("${abs(step)}", TextStyle(fontFamily = FredokaFontFamily, fontSize = 7.sp, color = CosmicCyan.copy(alpha = 0.55f)))
          drawText(stepTxt, topLeft = Offset(p1.x - stepTxt.size.width - 2f, p1.y - stepTxt.size.height / 2f))
        }
      }

      // 3. Banner Tattico di Allarme Prossimità / Sovraccarico Termico Solare sul Parabrezza
      if (sunDist < 95f || nearestDist < 52f) {
        val isSunAlert = sunDist < 95f
        val alertBannerY = 62f
        val alertColor = if (isSunAlert) Color(0xFFFF2A2A) else CosmicAmber
        val alertBlink = (sin(simTimeSec * 7f) * 0.5f + 0.5f)

        val alertText = if (isSunAlert) {
          "🚨 ALLARME ROSSO: SOVRACCARICO TERMICO SOLARE (+5.500°C) 🚨"
        } else {
          "⚠️ PROSSIMITÀ: ${nearestBody.name.uppercase()} (${(nearestDist * 1800f).roundToInt()} MILA KM)"
        }
        val alertMeas = textMeasurer.measure(
          alertText,
          TextStyle(fontFamily = FredokaFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = alertColor.copy(alpha = 0.4f + 0.6f * alertBlink)),
        )
        val alertW = alertMeas.size.width + 22f
        val alertH = 17f
        drawRoundRect(
          color = Color(0xDD0A0E1D),
          topLeft = Offset(center.x - alertW / 2f, alertBannerY - 2f),
          size = Size(alertW, alertH),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        )
        drawRoundRect(
          color = alertColor.copy(alpha = 0.35f + 0.65f * alertBlink),
          topLeft = Offset(center.x - alertW / 2f, alertBannerY - 2f),
          size = Size(alertW, alertH),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
          style = Stroke(width = 1.3f),
        )
        drawText(alertMeas, topLeft = Offset(center.x - alertMeas.size.width / 2f, alertBannerY))
      }

      // 3. Mini-Radar Tattico 2D a cupola
      val radarR = 38f
      val radarCenter = Offset(size.width - radarR - 16f, radarR + 42f)
      drawCircle(color = SpaceVoid.copy(alpha = 0.88f), radius = radarR, center = radarCenter)
      drawCircle(color = CosmicCyan.copy(alpha = 0.70f), radius = radarR, center = radarCenter, style = Stroke(width = 1.8f))
      drawCircle(color = CosmicCyan.copy(alpha = 0.28f), radius = radarR * 0.5f, center = radarCenter, style = Stroke(width = 1f))
      val radarScale = radarR / 460f
      val fwdAngle = atan2(shipForward.z, shipForward.x)
      drawLine(
        color = AuroraGreen.copy(alpha = 0.85f),
        start = radarCenter,
        end = Offset(radarCenter.x + cos(fwdAngle) * 20f, radarCenter.y + sin(fwdAngle) * 20f),
        strokeWidth = 2.2f,
      )
      bodies.forEach { b ->
        val wp = OrbitalMechanics.bodyWorldPosition(b, simTimeSec, useRealisticSpeed = true, timeScaleMultiplier = currentMultiplier)
        val rel = wp - shipPos
        val relX = rel.dot(shipRight) * radarScale
        val relZ = rel.dot(shipForward) * radarScale
        val clampedDist = hypot(relX, relZ)
        val factor = if (clampedDist > radarR - 4f) (radarR - 4f) / clampedDist else 1f
        drawCircle(
          color = if (b.id == autopilotTargetId) AuroraGreen else b.primaryColor,
          radius = if (b.id == "sun") 3.8f else 2.4f,
          center = Offset(radarCenter.x + relX * factor, radarCenter.y - relZ * factor),
        )
      }
      drawCircle(color = StarlightWhite, radius = 2.8f, center = radarCenter)
    }

    // 2. Top-Left HUD: Portello Uscita / Airlock + Telemetria di Bordo
    Row(
      modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        onClick = onExitSimulator,
        color = Color(0xFFD90429),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, ComicInk),
        shadowElevation = 6.dp,
        modifier = Modifier.testTag("cockpit_exit_button"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
        color = SpaceNavy.copy(alpha = 0.90f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, CosmicCyan.copy(alpha = 0.55f)),
      ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
          val speedKms = (abs(currentSpeed) * 240f).roundToInt()
          Text(
            text = if (currentSpeed < -1f) "RETROMARCIA $speedKms km/s" else "PROPULSIONE: $speedKms km/s",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (boostActive) NebulaMagenta else CosmicAmber,
            modifier = Modifier.testTag("cockpit_speed_readout"),
          )
          val distKmLabel = "${(nearestDist * 1800f).roundToInt()} mila km"
          Text(
            text = "Prossimità: ${nearestBody.name} ($distKmLabel)",
            style = MaterialTheme.typography.labelSmall,
            color = StarlightMuted,
          )

          val extTempCelsius: Int = when {
            sunDist < 45f -> 5500
            sunDist < 80f -> (5500 - (sunDist - 45f) * 85f).roundToInt()
            sunDist < 130f -> (2500 - (sunDist - 80f) * 35f).roundToInt()
            nearestBody.id == "sun" -> 5500
            nearestBody.id == "venus" && nearestDist < 50f -> 465
            nearestBody.id == "mercury" && nearestDist < 50f -> 430
            nearestBody.id == "earth" && nearestDist < 50f -> 15
            nearestBody.id == "moon" && nearestDist < 50f -> 120
            nearestBody.id == "mars" && nearestDist < 50f -> -60
            nearestBody.id == "jupiter" && nearestDist < 50f -> -110
            nearestBody.id == "saturn" && nearestDist < 50f -> -140
            nearestBody.id == "uranus" && nearestDist < 50f -> -215
            nearestBody.id == "neptune" && nearestDist < 50f -> -218
            nearestBody.id == "pluto" && nearestDist < 50f -> -230
            else -> (-270f + (220f / (1f + sunDist * 0.04f))).roundToInt().coerceAtLeast(-270)
          }

          val tempLabel = when {
            extTempCelsius >= 1000 -> "+$extTempCelsius °C 🔥 [CRITICO]"
            extTempCelsius >= 400 -> "+$extTempCelsius °C 🌡️ [CALDO ESTREMO]"
            extTempCelsius >= 0 -> "+$extTempCelsius °C 🌍 [TEMPERATO]"
            else -> "$extTempCelsius °C 🧊 [GELO COSMICO]"
          }
          val tempColor = when {
            extTempCelsius >= 1000 -> Color(0xFFFF2A2A)
            extTempCelsius >= 400 -> CosmicAmber
            extTempCelsius >= 0 -> AuroraGreen
            else -> CosmicCyan
          }
          Text(
            text = "Scafo: $tempLabel",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = tempColor,
          )
        }
      }
    }

    // 3. Top-Right: Comandi Simulazione Live / Pausa, Scansa Asteroidi e Livellamento
    Row(
      modifier = Modifier.align(Alignment.TopEnd).padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (onOpenAsteroidDodge != null) {
        Surface(
          onClick = onOpenAsteroidDodge,
          color = NebulaMagenta,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.2.dp, CosmicCyan),
          shadowElevation = 6.dp,
          modifier = Modifier.testTag("cockpit_open_asteroid_dodge"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.RocketLaunch,
              contentDescription = "Scansa Asteroidi",
              tint = StarlightWhite,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Scansa Asteroidi",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = StarlightWhite,
            )
          }
        }
      }

      Surface(
        onClick = { timeWarpMode = (timeWarpMode + 1) % 3 },
        color = SpaceCard.copy(alpha = 0.90f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, when (timeWarpMode) { 0 -> AuroraGreen; 1 -> CosmicAmber; else -> StarlightMuted }.copy(alpha = 0.6f)),
        modifier = Modifier.testTag("cockpit_orbits_mode"),
      ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (timeWarpMode == 2) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
            contentDescription = "Velocita orbite",
            tint = when (timeWarpMode) { 0 -> AuroraGreen; 1 -> CosmicAmber; else -> StarlightMuted },
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = when (timeWarpMode) { 0 -> "Orbite: 1x Reale"; 1 -> "Orbite: 100x"; else -> "Orbite: Ferme" },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = StarlightWhite,
          )
        }
      }

      Surface(
        onClick = { invertPitch = !invertPitch },
        color = SpaceCard.copy(alpha = 0.90f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f)),
        modifier = Modifier.testTag("cockpit_pitch_mode"),
      ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.Explore,
            contentDescription = "Modalita Beccheggio",
            tint = CosmicCyan,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (invertPitch) "Pitch: Aero" else "Pitch: Arcade",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = StarlightWhite,
          )
        }
      }
      Surface(
        onClick = {
          manualRollOffset = 0f
          shipBank = 0f
        },
        color = SpaceCard.copy(alpha = 0.90f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicAmber.copy(alpha = 0.5f)),
        modifier = Modifier.testTag("cockpit_level_button"),
      ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Rounded.AutoFixHigh, contentDescription = "Livella orizzonte", tint = CosmicAmber, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Orizzonte", style = MaterialTheme.typography.labelSmall, color = CosmicAmber)
        }
      }
    }

    // ==========================================================
    // 🎛️ PLANCIA DI COMANDO DELL'ASTRONAVE (DASHBOARD CONSOLE)
    // ==========================================================
    Surface(
      modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
      color = Color(0xF20B1124),
      border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(CosmicCyan.copy(alpha = 0.7f), NebulaMagenta.copy(alpha = 0.7f), CosmicAmber.copy(alpha = 0.7f)))),
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      shadowElevation = 16.dp,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // 1. POSTAZIONE POLLICE SINISTRO: Cloche di Volo 3D (Virata & Beccheggio)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(3.dp),
          modifier = Modifier.width(122.dp),
        ) {
          // Barra Integrità Scudo e Scafo compatta
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(11.dp))
            Text("SCUDI 100%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = CosmicCyan, fontWeight = FontWeight.Bold)
            Text("•", color = StarlightMuted, fontSize = 9.sp)
            Text("SCAFO 100%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AuroraGreen, fontWeight = FontWeight.Bold)
          }

          val joyLeftMaxPx = with(density) { 34.dp.toPx() }
          Box(
            modifier =
              Modifier.size(92.dp)
                .background(Color(0xD90A1128), CircleShape)
                .border(2.dp, Brush.radialGradient(listOf(CosmicCyan, Color(0xFF14203D))), CircleShape)
                .pointerInput(Unit) {
                  val centerPx = 46.dp.toPx()
                  val deadzone = 4.dp.toPx()
                  fun updateJoy(pos: Offset) {
                    isAutopilotSteering = false
                    val dx = pos.x - centerPx
                    val dy = pos.y - centerPx
                    val dist = hypot(dx, dy)
                    if (dist > deadzone) {
                      val norm = ((dist - deadzone) / (joyLeftMaxPx - deadzone)).coerceIn(0f, 1f)
                      val curved = norm * (0.35f + 0.65f * norm)
                      val ang = atan2(dy, dx)
                      stickX = (cos(ang) * curved).coerceIn(-1f, 1f)
                      stickY = (sin(ang) * curved).coerceIn(-1f, 1f)
                    } else {
                      stickX = 0f
                      stickY = 0f
                    }
                  }
                  detectDragGestures(
                    onDragStart = { offset -> updateJoy(offset) },
                    onDragEnd = {
                      stickX = 0f
                      stickY = 0f
                    },
                    onDragCancel = {
                      stickX = 0f
                      stickY = 0f
                    },
                  ) { change, _ ->
                    change.consume()
                    updateJoy(change.position)
                  }
                }
                .testTag("cockpit_flight_stick"),
            contentAlignment = Alignment.Center,
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val c = Offset(size.width / 2f, size.height / 2f)
              // Guide cerchi e tacche al neon
              drawCircle(color = CosmicCyan.copy(alpha = if (hasStick) 0.5f else 0.22f), radius = size.minDimension * 0.46f, style = Stroke(width = 1.8f))
              drawCircle(color = CosmicCyan.copy(alpha = 0.15f), radius = size.minDimension * 0.24f, style = Stroke(width = 1f))
              // Mirino centrale
              drawLine(color = CosmicCyan.copy(alpha = 0.35f), start = Offset(c.x - 10f, c.y), end = Offset(c.x + 10f, c.y), strokeWidth = 1.2f)
              drawLine(color = CosmicCyan.copy(alpha = 0.35f), start = Offset(c.x, c.y - 10f), end = Offset(c.x, c.y + 10f), strokeWidth = 1.2f)
              // Frecce guida sottili
              drawLine(color = CosmicCyan.copy(alpha = 0.45f), start = Offset(c.x, 5f), end = Offset(c.x, 12f), strokeWidth = 1.8f) // Cabra
              drawLine(color = CosmicCyan.copy(alpha = 0.45f), start = Offset(c.x, size.height - 5f), end = Offset(c.x, size.height - 12f), strokeWidth = 1.8f) // Picchia
              drawLine(color = CosmicCyan.copy(alpha = 0.45f), start = Offset(5f, c.y), end = Offset(12f, c.y), strokeWidth = 1.8f) // Sx
              drawLine(color = CosmicCyan.copy(alpha = 0.45f), start = Offset(size.width - 5f, c.y), end = Offset(size.width - 12f, c.y), strokeWidth = 1.8f) // Dx

              // Se attivo, raggio laser che collega il centro alla manopola
              val knob = Offset(c.x + stickX * joyLeftMaxPx, c.y + stickY * joyLeftMaxPx)
              if (hasStick) {
                drawLine(color = CosmicCyan.copy(alpha = 0.55f), start = c, end = knob, strokeWidth = 2.4f)
              }
              // Manopola cloche tridimensionale
              drawCircle(brush = Brush.radialGradient(listOf(CosmicAmber, Color(0xFFE76F51))), radius = 13.dp.toPx(), center = knob)
              drawCircle(color = ComicInk, radius = 13.dp.toPx(), center = knob, style = Stroke(width = 2f))
              drawCircle(color = StarlightWhite, radius = 2.5.dp.toPx(), center = Offset(knob.x - 3.dp.toPx(), knob.y - 3.dp.toPx()))
            }
          }
          Text(
            text = "CLOCHE (POLLICE SX)",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = CosmicCyan,
          )
        }

        // 2. POSTAZIONE CENTRALE: Co-Pilota Chibi Olografico + Rotte Pianeti
        Column(
          modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          val actionBody =
            autopilotTargetId?.let { SolarSystemData.getPlanetById(it) }
              ?: if (nearestDist < nearestBody.sceneVisualRadius * 5.2f + 42f) nearestBody else null

          // Terminale Olografico con Astronauta Chibi Co-Pilota
          Surface(
            color = SpaceCardElevated.copy(alpha = 0.92f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, if (boostActive) NebulaMagenta else CosmicCyan.copy(alpha = 0.6f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              val (coPilotSpeech, coPilotExpr) = getCockpitCoPilotDialogue(
                nearestBody = nearestBody,
                nearestDist = nearestDist,
                sunDist = sunDist,
                boostActive = boostActive,
                throttle = throttle,
                isAutopilot = isAutopilotSteering,
                chibiName = chibiProfile.name,
              )

              // Avatar Chibi
              ChibiAvatar(
                profile = chibiProfile,
                expression = coPilotExpr,
                size = 38.dp,
                showBackdrop = false,
              )

              // Messaggio del Co-pilota
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Text(
                    text = "CO-PILOTA ${chibiProfile.name.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CosmicAmber,
                  )
                  if (sunDist < 100f) {
                    Surface(
                      color = Color(0xFFFF2A2A),
                      shape = RoundedCornerShape(4.dp),
                    ) {
                      Text(
                        text = "ALLERTA CALORE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = StarlightWhite,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                      )
                    }
                  } else if (nearestDist < 55f) {
                    Surface(
                      color = AuroraGreen,
                      shape = RoundedCornerShape(4.dp),
                    ) {
                      Text(
                        text = "PROSSIMITÀ",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = ComicInk,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                      )
                    }
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = coPilotSpeech,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                  color = if (sunDist < 90f) CosmicAmber else StarlightWhite,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              // Pulsanti azione rapida per il pianeta
              if (actionBody != null) {
                if (autopilotTargetId != null) {
                  Button(
                    onClick = { isAutopilotSteering = !isAutopilotSteering },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (isAutopilotSteering) NebulaMagenta else AuroraGreen,
                      contentColor = if (isAutopilotSteering) StarlightWhite else ComicInk,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("cockpit_autopilot_button"),
                  ) {
                    Icon(Icons.Rounded.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = if (isAutopilotSteering) "Sgancia" else "Punta",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
                if (actionBody.id != "sun") {
                  Button(
                    onClick = { onOpenLandingSimulator(actionBody.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
                    shape = RoundedCornerShape(8.dp),
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
                  shape = RoundedCornerShape(8.dp),
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

          // Selettore Rapido Pianeti Rotta
          Surface(
            color = SpaceNavy.copy(alpha = 0.85f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.35f)),
          ) {
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 3.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              items(bodies, key = { it.id }) { b ->
                FilterChip(
                  selected = autopilotTargetId == b.id,
                  onClick = {
                    autopilotTargetId = b.id
                    isAutopilotSteering = false
                  },
                  label = { Text(b.name, style = MaterialTheme.typography.labelSmall) },
                  colors = FilterChipDefaults.filterChipColors(selectedContainerColor = b.primaryColor, selectedLabelColor = ComicInk),
                  modifier = Modifier.height(26.dp).testTag("cockpit_route_${b.id}"),
                )
              }
            }
          }
        }

        // 3. POSTAZIONE POLLICE DESTRO: Manetta Spinta & Pulsanti Tattici (STOP, WARP, Rollio)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(3.dp),
          modifier = Modifier.width(148.dp).testTag("cockpit_right_stick"),
        ) {
          // Barra comandi assetto: Livellamento e Rollio manuale fine
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              onClick = {
                manualRollOffset = 0f
                shipBank = 0f
              },
              color = SpaceCard,
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, CosmicAmber.copy(alpha = 0.5f)),
              modifier = Modifier.height(22.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(Icons.Rounded.AutoFixHigh, contentDescription = "Livella Orizzonte", tint = CosmicAmber, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("LIVELLA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = CosmicAmber)
              }
            }

            Surface(
              onClick = {
                manualRollOffset = (manualRollOffset + 0.16f).coerceIn(-0.8f, 0.8f)
              },
              color = SpaceCard,
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.4f)),
              modifier = Modifier.size(22.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.RotateLeft, contentDescription = "Rollio Sinistra", tint = CosmicCyan, modifier = Modifier.size(13.dp))
              }
            }

            Surface(
              onClick = {
                manualRollOffset = (manualRollOffset - 0.16f).coerceIn(-0.8f, 0.8f)
              },
              color = SpaceCard,
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.4f)),
              modifier = Modifier.size(22.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.RotateRight, contentDescription = "Rollio Destra", tint = CosmicCyan, modifier = Modifier.size(13.dp))
              }
            }
          }

          // Blocco Manetta Verticale + Pulsanti Rapidi STOP & WARP
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Manetta Verticale con mantenimento velocita di crociera al rilascio
            val leverHeightDp = 86.dp
            val leverWidthDp = 50.dp
            val zeroRatio = 0.72f // La linea 0% e al 72% dall'alto (spinta avanti sopra, retro sotto)

            fun updateThrottleFromY(yPx: Float, totalHPx: Float) {
              val normY = (yPx / totalHPx).coerceIn(0f, 1f)
              throttle = if (normY <= zeroRatio) {
                val fwd = (zeroRatio - normY) / zeroRatio
                fwd.coerceIn(0f, 1f)
              } else {
                val rev = (normY - zeroRatio) / (1f - zeroRatio)
                (-rev * 0.35f).coerceIn(-0.35f, 0f)
              }
              if (boostActive && throttle < 0.95f) {
                boostActive = false
              }
            }

            Box(
              modifier = Modifier
                .size(width = leverWidthDp, height = leverHeightDp)
                .background(Color(0xD90A1128), RoundedCornerShape(12.dp))
                .border(1.5.dp, Brush.verticalGradient(listOf(NebulaMagenta, CosmicCyan, CosmicAmber)), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                  detectTapGestures { offset ->
                    updateThrottleFromY(offset.y, size.height.toFloat())
                  }
                }
                .pointerInput(Unit) {
                  detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newY = with(density) {
                      val currentY = if (throttle >= 0f) {
                        (zeroRatio - throttle * zeroRatio) * leverHeightDp.toPx()
                      } else {
                        (zeroRatio + (-throttle / 0.35f) * (1f - zeroRatio)) * leverHeightDp.toPx()
                      }
                      currentY + dragAmount.y
                    }
                    updateThrottleFromY(newY, size.height.toFloat())
                  }
                }
                .testTag("cockpit_cruise_button"),
              contentAlignment = Alignment.Center,
            ) {
              Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp)) {
                val w = size.width
                val h = size.height
                val zeroY = h * zeroRatio

                // Binario scanalato di scorrimento
                val trackX = w * 0.32f
                val trackW = 7.dp.toPx()
                drawRoundRect(
                  color = Color(0xFF070B16),
                  topLeft = Offset(trackX - trackW / 2f, 0f),
                  size = Size(trackW, h),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                )

                // Posizione Y del cursore manetta
                val knobY = if (throttle >= 0f) {
                  zeroY - throttle * zeroY
                } else {
                  zeroY + (-throttle / 0.35f) * (h - zeroY)
                }

                // Barra luminosa di potenza attiva
                if (throttle > 0.01f) {
                  drawRoundRect(
                    brush = Brush.verticalGradient(
                      colors = listOf(
                        if (boostActive || throttle > 0.85f) NebulaMagenta else CosmicAmber,
                        CosmicCyan,
                        AuroraGreen,
                      ),
                      startY = knobY,
                      endY = zeroY,
                    ),
                    topLeft = Offset(trackX - trackW / 2f + 1f, knobY),
                    size = Size(trackW - 2f, zeroY - knobY),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                  )
                } else if (throttle < -0.01f) {
                  drawRoundRect(
                    color = Color(0xFFFF4D4D),
                    topLeft = Offset(trackX - trackW / 2f + 1f, zeroY),
                    size = Size(trackW - 2f, knobY - zeroY),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                  )
                }

                // Tacche di graduazione manetta
                val tickX1 = trackX + trackW / 2f + 4f
                val tickX2 = w - 4f
                // MAX / WARP tick
                drawLine(color = NebulaMagenta, start = Offset(tickX1, 2f), end = Offset(tickX2, 2f), strokeWidth = 1.5f)
                // 50% tick
                val halfY = zeroY * 0.5f
                drawLine(color = CosmicCyan.copy(alpha = 0.6f), start = Offset(tickX1, halfY), end = Offset(tickX2 - 4f, halfY), strokeWidth = 1.2f)
                // 0% Neutro
                drawLine(color = StarlightWhite.copy(alpha = 0.85f), start = Offset(2f, zeroY), end = Offset(tickX2, zeroY), strokeWidth = 1.8f)
                // Retro tick
                val retroTickY = zeroY + (h - zeroY) * 0.7f
                drawLine(color = Color(0xFFFF4D4D).copy(alpha = 0.6f), start = Offset(tickX1, retroTickY), end = Offset(tickX2 - 4f, retroTickY), strokeWidth = 1.2f)

                // Etichette manetta
                val warpTxt = textMeasurer.measure("MAX", TextStyle(fontFamily = FredokaFontFamily, fontSize = 7.sp, color = NebulaMagenta))
                drawText(warpTxt, topLeft = Offset(w - warpTxt.size.width - 2f, 3f))

                val zeroTxt = textMeasurer.measure("0", TextStyle(fontFamily = FredokaFontFamily, fontSize = 7.sp, color = StarlightMuted))
                drawText(zeroTxt, topLeft = Offset(w - zeroTxt.size.width - 2f, zeroY - zeroTxt.size.height / 2f))

                val revTxt = textMeasurer.measure("REV", TextStyle(fontFamily = FredokaFontFamily, fontSize = 6.sp, color = Color(0xFFFF7777)))
                drawText(revTxt, topLeft = Offset(w - revTxt.size.width - 2f, h - revTxt.size.height - 2f))

                // Pomello manetta con presa ergonomica e bordo al neon
                val knobW = 28.dp.toPx()
                val knobH = 13.dp.toPx()
                val knobLeft = trackX - knobW * 0.45f
                val knobTop = knobY - knobH / 2f
                drawRoundRect(
                  brush = Brush.verticalGradient(
                    colors = listOf(
                      Color(0xFF384668),
                      Color(0xFF1E2840),
                      Color(0xFF12192A),
                    ),
                    startY = knobTop,
                    endY = knobTop + knobH,
                  ),
                  topLeft = Offset(knobLeft, knobTop),
                  size = Size(knobW, knobH),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                )
                drawRoundRect(
                  color = if (boostActive) NebulaMagenta else if (throttle > 0.05f) AuroraGreen else if (throttle < -0.05f) Color(0xFFFF3333) else CosmicCyan,
                  topLeft = Offset(knobLeft, knobTop),
                  size = Size(knobW, knobH),
                  cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                  style = Stroke(width = 1.6f),
                )
                // Luce centrale del pomello
                drawLine(
                  color = StarlightWhite,
                  start = Offset(knobLeft + 4.dp.toPx(), knobY),
                  end = Offset(knobLeft + knobW - 4.dp.toPx(), knobY),
                  strokeWidth = 1.8f,
                )
              }
            }

            // Pulsanti Tattici Azione Rapida
            Column(
              verticalArrangement = Arrangement.spacedBy(6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              // Pulsante STOP Emergenza (Grande e accessibile col pollice)
              Surface(
                onClick = {
                  throttle = 0f
                  currentSpeed = 0f
                  boostActive = false
                  isAutopilotSteering = false
                },
                color = Color(0xFFD90429),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.2.dp, Color(0xFFFF6666)),
                shadowElevation = 4.dp,
                modifier = Modifier.size(width = 78.dp, height = 38.dp).testTag("cockpit_stop_button"),
              ) {
                Row(
                  modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(Icons.Rounded.StopCircle, contentDescription = "Freno di Emergenza", tint = StarlightWhite, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("STOP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StarlightWhite)
                }
              }

              // Pulsante WARP / Curvatura
              Surface(
                onClick = {
                  boostActive = !boostActive
                  if (boostActive) {
                    throttle = 1.0f
                  }
                },
                color = if (boostActive) NebulaMagenta else SpaceCardElevated,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.2.dp, if (boostActive) CosmicCyan else NebulaMagenta.copy(alpha = 0.6f)),
                shadowElevation = 4.dp,
                modifier = Modifier.size(width = 78.dp, height = 38.dp).testTag("cockpit_warp_chip"),
              ) {
                Row(
                  modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(Icons.Rounded.Bolt, contentDescription = "Motore a Curvatura", tint = if (boostActive) StarlightWhite else NebulaMagenta, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(if (boostActive) "WARP ON" else "WARP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StarlightWhite)
                }
              }
            }
          }

          val throttlePct = (throttle * 100f).roundToInt()
          val modeLabel = when {
            boostActive -> "WARP 100%"
            throttlePct > 0 -> "SPINTA $throttlePct%"
            throttlePct < 0 -> "RETRO $throttlePct%"
            else -> "FERMO 0%"
          }
          Text(
            text = modeLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = if (boostActive) NebulaMagenta else if (throttlePct > 0) AuroraGreen else if (throttlePct < 0) Color(0xFFFF4D4D) else CosmicAmber,
          )
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
  sunScreenPos: Offset? = null,
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

    // Direzione reale della luce solare calcolata rispetto al Sole
    val lightDir = if (sunScreenPos != null) {
      val diff = sunScreenPos - screenPos
      val l = hypot(diff.x, diff.y)
      if (l > 1f) Offset(diff.x / l, diff.y / l) else Offset(-0.6f, -0.6f)
    } else {
      Offset(-0.55f, -0.55f)
    }

    val lightCenter = Offset(screenPos.x + lightDir.x * radius * 0.38f, screenPos.y + lightDir.y * radius * 0.38f)

    // Base del pianeta con luce orientata verso il Sole
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(body.accentColor, body.primaryColor, Color(0xFF070B16)),
          center = lightCenter,
          radius = (radius * 1.38f).coerceAtLeast(4f),
        ),
      radius = radius,
      center = screenPos,
    )

    // Emisfero d'ombra e terminatore fotorealistico
    val shadowCenter = Offset(screenPos.x - lightDir.x * radius * 0.50f, screenPos.y - lightDir.y * radius * 0.50f)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color.Transparent, Color(0xCC03050B)),
        center = shadowCenter,
        radius = radius * 1.15f,
      ),
      radius = radius,
      center = screenPos,
    )

    // Scattering atmosferico sul bordo rivolto verso il Sole (Terra, Venere, Giove, ecc.)
    if (radius > 12f && (body.id in listOf("earth", "venus", "jupiter", "saturn", "uranus", "neptune"))) {
      val atmoColor = when (body.id) {
        "earth" -> CosmicCyan.copy(alpha = 0.60f)
        "venus" -> CosmicAmber.copy(alpha = 0.50f)
        "jupiter", "saturn" -> Color(0xFFFFD166).copy(alpha = 0.40f)
        else -> CosmicCyan.copy(alpha = 0.50f)
      }
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(atmoColor, Color.Transparent),
          center = lightCenter,
          radius = radius * 1.05f,
        ),
        radius = radius * 1.04f,
        center = screenPos,
        style = Stroke(width = (radius * 0.08f).coerceIn(1.5f, 4f)),
      )
    }
  }

  // Mirino Tattico di Puntamento Olografico Fantascientifico
  if (isLocked) {
    val targetR = (radius + 12f).coerceAtLeast(20f)
    // 4 parentesi angolari di puntamento
    val bracketLen = targetR * 0.45f
    // Top-Left
    drawLine(color = AuroraGreen, start = Offset(screenPos.x - targetR, screenPos.y - targetR), end = Offset(screenPos.x - targetR + bracketLen, screenPos.y - targetR), strokeWidth = 2.4f)
    drawLine(color = AuroraGreen, start = Offset(screenPos.x - targetR, screenPos.y - targetR), end = Offset(screenPos.x - targetR, screenPos.y - targetR + bracketLen), strokeWidth = 2.4f)
    // Top-Right
    drawLine(color = AuroraGreen, start = Offset(screenPos.x + targetR, screenPos.y - targetR), end = Offset(screenPos.x + targetR - bracketLen, screenPos.y - targetR), strokeWidth = 2.4f)
    drawLine(color = AuroraGreen, start = Offset(screenPos.x + targetR, screenPos.y - targetR), end = Offset(screenPos.x + targetR, screenPos.y - targetR + bracketLen), strokeWidth = 2.4f)
    // Bottom-Left
    drawLine(color = AuroraGreen, start = Offset(screenPos.x - targetR, screenPos.y + targetR), end = Offset(screenPos.x - targetR + bracketLen, screenPos.y + targetR), strokeWidth = 2.4f)
    drawLine(color = AuroraGreen, start = Offset(screenPos.x - targetR, screenPos.y + targetR), end = Offset(screenPos.x - targetR, screenPos.y + targetR - bracketLen), strokeWidth = 2.4f)
    // Bottom-Right
    drawLine(color = AuroraGreen, start = Offset(screenPos.x + targetR, screenPos.y + targetR), end = Offset(screenPos.x + targetR - bracketLen, screenPos.y + targetR), strokeWidth = 2.4f)
    drawLine(color = AuroraGreen, start = Offset(screenPos.x + targetR, screenPos.y + targetR), end = Offset(screenPos.x + targetR, screenPos.y + targetR - bracketLen), strokeWidth = 2.4f)

    drawCircle(
      color = AuroraGreen.copy(alpha = 0.35f),
      radius = targetR,
      center = screenPos,
      style = Stroke(width = 1.2f),
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
      topLeft = Offset((screenPos.x - lbl.size.width / 2f).coerceIn(4f, size.width - lbl.size.width - 4f), (screenPos.y + radius + 6f).coerceAtMost(size.height - 18f)),
    )
  }
}
