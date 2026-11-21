package com.ricrug.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricrug.data.SolarSystemData
import com.ricrug.model.CelestialBody
import com.ricrug.model.ChibiExpression
import com.ricrug.model.ChibiProfile
import com.ricrug.model.PlanetSurfaceStyle
import com.ricrug.ui.components.ChibiComicBubble
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceCard
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class LanderOutcome {
  FLYING,
  LANDED_SUCCESS,
  CRASHED_FAST,
  MISSED_PAD,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingSimulatorScreen(
  planetId: String,
  chibiProfile: ChibiProfile,
  onMarkExplored: (String) -> Unit,
  onBack: () -> Unit,
  onStartSurfaceExploration: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val body = SolarSystemData.getPlanetById(planetId)
  val isGasWorld =
    body.surfaceStyle in
      listOf(
        PlanetSurfaceStyle.JUPITER,
        PlanetSurfaceStyle.SATURN,
        PlanetSurfaceStyle.URANUS,
        PlanetSurfaceStyle.NEPTUNE,
        PlanetSurfaceStyle.SUN,
      )

  LaunchedEffect(planetId) { onMarkExplored(planetId) }

  // Simulation state (normalized 0..1 space)
  var resetSeed by remember { mutableIntStateOf(0) }
  var landerX by remember(planetId, resetSeed) { mutableFloatStateOf(0.28f) }
  var landerY by remember(planetId, resetSeed) { mutableFloatStateOf(0.14f) }
  var vx by remember(planetId, resetSeed) { mutableFloatStateOf(0.03f) }
  var vy by remember(planetId, resetSeed) { mutableFloatStateOf(0f) }
  var fuel by remember(planetId, resetSeed) { mutableFloatStateOf(1f) }
  var outcome by remember(planetId, resetSeed) { mutableStateOf(LanderOutcome.FLYING) }
  var touchSpeedMs by remember(planetId, resetSeed) { mutableIntStateOf(0) }
  var animClock by remember { mutableFloatStateOf(0f) }

  // Hold thruster states
  var thrustUp by remember { mutableStateOf(false) }
  var thrustLeft by remember { mutableStateOf(false) }
  var thrustRight by remember { mutableStateOf(false) }

  val padX = 0.56f
  val padHalfWidth = 0.12f
  val surfaceY = 0.84f

  // Gravity & wind tailored to the planet
  val gravityAccel = remember(body.id) { (0.045f + body.gravityRatio * 0.085f).coerceIn(0.045f, 0.24f) }
  val mainThrusterAccel = remember(gravityAccel) { gravityAccel * 2.15f + 0.09f }
  val windAccel =
    remember(body.id) {
      when (body.surfaceStyle) {
        PlanetSurfaceStyle.NEPTUNE -> 0.045f
        PlanetSurfaceStyle.JUPITER, PlanetSurfaceStyle.SATURN -> 0.028f
        PlanetSurfaceStyle.VENUS -> 0.020f
        else -> 0f
      }
    }

  LaunchedEffect(planetId, resetSeed) {
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.045f)
          animClock += dt

          if (outcome == LanderOutcome.FLYING) {
            var ax = windAccel * sin(animClock * 1.4f)
            var ay = gravityAccel

            if (fuel > 0f) {
              if (thrustUp) {
                ay -= mainThrusterAccel
                fuel = (fuel - dt * 0.13f).coerceAtLeast(0f)
              }
              if (thrustLeft) {
                ax -= 0.28f
                fuel = (fuel - dt * 0.045f).coerceAtLeast(0f)
              }
              if (thrustRight) {
                ax += 0.28f
                fuel = (fuel - dt * 0.045f).coerceAtLeast(0f)
              }
            }

            vx = (vx + ax * dt).coerceIn(-0.45f, 0.45f) * 0.992f
            vy = (vy + ay * dt).coerceIn(-0.45f, 0.65f)

            landerX = (landerX + vx * dt).coerceIn(0.06f, 0.94f)
            landerY += vy * dt

            if (landerY >= surfaceY) {
              landerY = surfaceY
              val speedMs = (vy * 100f).roundToInt().coerceAtLeast(0)
              touchSpeedMs = speedMs
              val onPad = abs(landerX - padX) <= padHalfWidth
              outcome =
                when {
                  !onPad -> LanderOutcome.MISSED_PAD
                  speedMs > 26 -> LanderOutcome.CRASHED_FAST
                  else -> LanderOutcome.LANDED_SUCCESS
                }
            }
          }
        }
        lastNanos = nanos
      }
    }
  }

  val currentSpeedMs = (vy * 100f).roundToInt().coerceAtLeast(0)
  val altitudeMeters = (((surfaceY - landerY) / (surfaceY - 0.12f)) * 420f).roundToInt().coerceAtLeast(0)

  Column(
    modifier = modifier.fillMaxSize().background(SpaceVoid),
  ) {
    TopAppBar(
      title = {
        Column {
          Text(
            text = if (isGasWorld) "Stazione su ${body.name}" else "Atterraggio su ${body.name}",
            style = MaterialTheme.typography.titleLarge,
            color = StarlightWhite,
          )
          Text(
            text = "Gravità: ${body.gravityRatio}x Terra • Velocità sicura ≤ 26 m/s",
            style = MaterialTheme.typography.labelSmall,
            color = CosmicAmber,
          )
        }
      },
      navigationIcon = {
        IconButton(onClick = onBack, modifier = Modifier.testTag("landing_back_button")) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Torna indietro",
            tint = StarlightWhite,
          )
        }
      },
      actions = {
        IconButton(
          onClick = { resetSeed++ },
          modifier = Modifier.testTag("landing_reset_button"),
        ) {
          Icon(Icons.Rounded.Refresh, contentDescription = "Ricomincia", tint = CosmicCyan)
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = SpaceNavy),
    )

    // Telemetry Bar
    Row(
      modifier = Modifier.fillMaxWidth().background(SpaceNavy.copy(alpha = 0.85f)).padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(color = SpaceCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
          Text("QUOTA", style = MaterialTheme.typography.labelSmall, color = StarlightMuted)
          Text("${altitudeMeters} m", style = MaterialTheme.typography.titleSmall, color = CosmicCyan, fontWeight = FontWeight.Bold)
        }
      }
      val speedColor =
        when {
          currentSpeedMs <= 22 -> AuroraGreen
          currentSpeedMs <= 26 -> CosmicAmber
          else -> NebulaMagenta
        }
      Surface(color = SpaceCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Rounded.Speed, contentDescription = null, tint = speedColor, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text("DISCESA", style = MaterialTheme.typography.labelSmall, color = StarlightMuted)
            Text("$currentSpeedMs m/s", style = MaterialTheme.typography.titleSmall, color = speedColor, fontWeight = FontWeight.Bold)
          }
        }
      }
      Surface(color = SpaceCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1.1f)) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LocalGasStation, contentDescription = null, tint = CosmicAmber, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("CARB. ${(fuel * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = StarlightMuted)
          }
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { fuel },
            color = if (fuel > 0.25f) CosmicAmber else NebulaMagenta,
            trackColor = SpaceVoid,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
          )
        }
      }
    }

    // Main Landing Simulation Arena
    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      Canvas(modifier = Modifier.fillMaxSize().testTag("landing_arena_canvas")) {
        drawPlanetLandingEnvironment(
          body = body,
          isGasWorld = isGasWorld,
          padX = padX,
          padHalfWidth = padHalfWidth,
          surfaceY = surfaceY,
          animClock = animClock,
        )

        val lx = landerX * size.width
        val ly = landerY * size.height
        val tiltDeg =
          when {
            thrustLeft && !thrustRight -> -14f
            thrustRight && !thrustLeft -> 14f
            else -> (vx * 28f).coerceIn(-12f, 12f)
          }

        drawLanderModule(
          center = Offset(lx, ly - 16f),
          tiltDeg = tiltDeg,
          mainThrustOn = thrustUp && fuel > 0f && outcome == LanderOutcome.FLYING,
          leftThrustOn = thrustLeft && fuel > 0f && outcome == LanderOutcome.FLYING,
          rightThrustOn = thrustRight && fuel > 0f && outcome == LanderOutcome.FLYING,
          suitColor = SolarSystemData.suitColors.getOrElse(chibiProfile.suitColorIndex) { CosmicAmber },
          animClock = animClock,
          crashed = outcome == LanderOutcome.CRASHED_FAST || outcome == LanderOutcome.MISSED_PAD,
        )
      }

      // Live SpaceBloom briefing / outcome overlay
      if (outcome == LanderOutcome.FLYING && altitudeMeters > 340) {
        Surface(
          color = SpaceNavy.copy(alpha = 0.85f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.6f)),
          modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
        ) {
          Text(
            text =
              if (isGasWorld) {
                "${chibiProfile.name}: Non c'è terreno solido su ${body.name}: atterra sulla piattaforma fluttuante!"
              } else {
                "${chibiProfile.name}: Centra la pista luminosa e frena prima di toccare terra!"
              },
            style = MaterialTheme.typography.bodyMedium,
            color = StarlightWhite,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          )
        }
      }

      if (outcome != LanderOutcome.FLYING) {
        Box(
          modifier = Modifier.align(Alignment.Center).padding(16.dp),
        ) {
          val (exp, badge, headline, msg) =
            when (outcome) {
              LanderOutcome.LANDED_SUCCESS ->
                Quad(
                  ChibiExpression.CHEERING,
                  "ATTERRAGGIO PERFETTO!",
                  "Ottimo lavoro pilota!",
                  "Sei atterrato su ${body.name} a soli $touchSpeedMs m/s con ${(fuel * 100).roundToInt()}% di carburante rimasto!",
                )
              LanderOutcome.CRASHED_FAST ->
                Quad(
                  ChibiExpression.THINKING,
                  "BOTTA FORTE!",
                  "Discesa troppo veloce ($touchSpeedMs m/s)",
                  "Tieni premuto il pulsante FRENO SU poco prima di toccare la pista per scendere sotto i 26 m/s!",
                )
              LanderOutcome.MISSED_PAD ->
                Quad(
                  ChibiExpression.AMAZED,
                  "FUORI PISTA!",
                  "Quasi centrata!",
                  "Usa i propulsori SINISTRA e DESTRA per allinearti sopra la piattaforma luminosa prima di scendere!",
                )
              LanderOutcome.FLYING -> Quad(ChibiExpression.HAPPY, "", "", "")
            }

          ChibiComicBubble(
            profile = chibiProfile,
            expression = exp,
            badgeText = badge,
            headline = headline,
            dialogue = msg,
            avatarSize = 78.dp,
            footerContent = {
              if (outcome == LanderOutcome.LANDED_SUCCESS) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Button(
                    onClick = { onStartSurfaceExploration(body.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("landing_explore_button"),
                  ) {
                    Text(
                      text = "🥾 Sbarca ed Esplora",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      softWrap = false,
                    )
                  }
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Button(
                      onClick = { resetSeed++ },
                      colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
                      shape = RoundedCornerShape(12.dp),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                      modifier = Modifier.weight(1f).testTag("landing_retry_button"),
                    ) {
                      Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Riprova", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(
                      onClick = onBack,
                      border = BorderStroke(1.5.dp, ComicInk),
                      shape = RoundedCornerShape(12.dp),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                      modifier = Modifier.weight(1f),
                    ) {
                      Text("Esci", color = ComicInk, style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                  }
                }
              } else {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Button(
                    onClick = { resetSeed++ },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).testTag("landing_retry_button"),
                  ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Riprova", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                  }
                  OutlinedButton(
                    onClick = onBack,
                    border = BorderStroke(1.5.dp, ComicInk),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f),
                  ) {
                    Text("Esci", color = ComicInk, style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                  }
                }
              }
            },
          )
        }
      }
    }

    // Bottom Thruster Controls
    Surface(
      color = SpaceNavy,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HoldControlButton(
          label = "SINISTRA",
          icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
          activeColor = CosmicCyan,
          onPressChange = { thrustLeft = it },
          modifier = Modifier.weight(1f).testTag("lander_left_button"),
        )
        HoldControlButton(
          label = "FRENO SU",
          icon = Icons.Rounded.ArrowUpward,
          activeColor = CosmicAmber,
          onPressChange = { thrustUp = it },
          modifier = Modifier.weight(1.35f).testTag("lander_thrust_button"),
        )
        HoldControlButton(
          label = "DESTRA",
          icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
          activeColor = CosmicCyan,
          onPressChange = { thrustRight = it },
          modifier = Modifier.weight(1f).testTag("lander_right_button"),
        )
      }
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun HoldControlButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  activeColor: Color,
  onPressChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var pressed by remember { mutableStateOf(false) }

  Surface(
    color = if (pressed) activeColor else SpaceCard,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(2.dp, if (pressed) ComicInk else activeColor.copy(alpha = 0.7f)),
    modifier =
      modifier.height(64.dp)
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = {
              pressed = true
              onPressChange(true)
              tryAwaitRelease()
              pressed = false
              onPressChange(false)
            },
          )
        },
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (pressed) ComicInk else activeColor,
        modifier = Modifier.size(26.dp),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (pressed) ComicInk else StarlightWhite,
        maxLines = 1,
        softWrap = false,
      )
    }
  }
}

private fun DrawScope.drawPlanetLandingEnvironment(
  body: CelestialBody,
  isGasWorld: Boolean,
  padX: Float,
  padHalfWidth: Float,
  surfaceY: Float,
  animClock: Float,
) {
  val skyTop = Color(0xFF060917)
  val skyBottom =
    when (body.surfaceStyle) {
      PlanetSurfaceStyle.EARTH -> Color(0xFF1D4ED8)
      PlanetSurfaceStyle.MARS -> Color(0xFF7C2D12)
      PlanetSurfaceStyle.VENUS -> Color(0xFF92400E)
      PlanetSurfaceStyle.JUPITER -> Color(0xFF78350F)
      PlanetSurfaceStyle.SATURN -> Color(0xFF713F12)
      PlanetSurfaceStyle.URANUS -> Color(0xFF0E7490)
      PlanetSurfaceStyle.NEPTUNE -> Color(0xFF1E3A8A)
      PlanetSurfaceStyle.SUN -> Color(0xFF9A3412)
      else -> Color(0xFF111827)
    }

  drawRect(
    brush = Brush.verticalGradient(colors = listOf(skyTop, skyBottom)),
  )

  // Stars in upper sky
  for (i in 0 until 45) {
    val sx = ((i * 73 + 11) % 100) / 100f * size.width
    val sy = ((i * 41 + 7) % 58) / 100f * size.height
    drawCircle(
      color = Color.White.copy(alpha = 0.4f + (i % 4) * 0.15f),
      radius = if (i % 5 == 0) 2.2f else 1.3f,
      center = Offset(sx, sy),
    )
  }

  val groundTopY = surfaceY * size.height

  if (isGasWorld) {
    // Swirling cloud deck below floating landing platform
    for (band in 0..3) {
      val bandY = groundTopY + band * 26f
      val drift = sin(animClock * 0.8f + band) * 18f
      drawRoundRect(
        color = body.primaryColor.copy(alpha = 0.55f - band * 0.1f),
        topLeft = Offset(-30f + drift, bandY),
        size = Size(size.width + 60f, 34f),
        cornerRadius = CornerRadius(18f),
      )
    }
  } else {
    // Rocky terrain & hills
    val terrainPath =
      Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, groundTopY + 12f)
        lineTo(size.width * 0.18f, groundTopY - 26f)
        lineTo(size.width * (padX - padHalfWidth - 0.04f), groundTopY)
        lineTo(size.width * (padX + padHalfWidth + 0.04f), groundTopY)
        lineTo(size.width * 0.84f, groundTopY - 32f)
        lineTo(size.width, groundTopY + 10f)
        lineTo(size.width, size.height)
        close()
      }
    drawPath(
      path = terrainPath,
      brush = Brush.verticalGradient(colors = listOf(body.primaryColor, body.secondaryColor), startY = groundTopY - 30f, endY = size.height),
    )
    // Surface craters on Moon / Mercury / Mars / Pluto
    listOf(0.14f, 0.34f, 0.78f, 0.90f).forEachIndexed { idx, cxRatio ->
      drawOval(
        color = body.secondaryColor.copy(alpha = 0.7f),
        topLeft = Offset(size.width * cxRatio - 22f, groundTopY + 18f + (idx % 2) * 14f),
        size = Size(44f, 14f),
      )
    }
  }

  // Glowing Landing Pad
  val padCenterPx = padX * size.width
  val padHalfPx = padHalfWidth * size.width
  val pulse = 0.65f + 0.35f * sin(animClock * 4f)

  // Beacon glow beam
  drawRect(
    brush =
      Brush.verticalGradient(
        colors = listOf(Color.Transparent, AuroraGreen.copy(alpha = 0.18f * pulse)),
        startY = groundTopY - 110f,
        endY = groundTopY,
      ),
    topLeft = Offset(padCenterPx - padHalfPx, groundTopY - 110f),
    size = Size(padHalfPx * 2f, 110f),
  )

  drawRoundRect(
    color = Color(0xFF1E293B),
    topLeft = Offset(padCenterPx - padHalfPx, groundTopY - 6f),
    size = Size(padHalfPx * 2f, 14f),
    cornerRadius = CornerRadius(6f),
  )
  drawRoundRect(
    color = AuroraGreen,
    topLeft = Offset(padCenterPx - padHalfPx, groundTopY - 6f),
    size = Size(padHalfPx * 2f, 14f),
    cornerRadius = CornerRadius(6f),
    style = Stroke(width = 3f),
  )
  // Pad beacons
  drawCircle(color = CosmicAmber, radius = 5f, center = Offset(padCenterPx - padHalfPx + 6f, groundTopY - 8f))
  drawCircle(color = CosmicAmber, radius = 5f, center = Offset(padCenterPx + padHalfPx - 6f, groundTopY - 8f))
}

private fun DrawScope.drawLanderModule(
  center: Offset,
  tiltDeg: Float,
  mainThrustOn: Boolean,
  leftThrustOn: Boolean,
  rightThrustOn: Boolean,
  suitColor: Color,
  animClock: Float,
  crashed: Boolean,
) {
  rotate(degrees = tiltDeg, pivot = center) {
    if (mainThrustOn) {
      val flameH = 26f + 12f * sin(animClock * 36f)
      drawOval(
        color = CosmicAmber,
        topLeft = Offset(center.x - 9f, center.y + 14f),
        size = Size(18f, flameH),
      )
      drawOval(
        color = CosmicCyan,
        topLeft = Offset(center.x - 5f, center.y + 15f),
        size = Size(10f, flameH * 0.6f),
      )
    }
    if (leftThrustOn) {
      drawOval(color = CosmicCyan, topLeft = Offset(center.x + 14f, center.y - 6f), size = Size(14f, 8f))
    }
    if (rightThrustOn) {
      drawOval(color = CosmicCyan, topLeft = Offset(center.x - 28f, center.y - 6f), size = Size(14f, 8f))
    }

    // Lander legs
    drawLine(color = StarlightWhite, start = Offset(center.x - 10f, center.y + 8f), end = Offset(center.x - 19f, center.y + 18f), strokeWidth = 4f)
    drawLine(color = StarlightWhite, start = Offset(center.x + 10f, center.y + 8f), end = Offset(center.x + 19f, center.y + 18f), strokeWidth = 4f)

    // Capsule body
    val capsule =
      Path().apply {
        moveTo(center.x, center.y - 22f)
        lineTo(center.x + 15f, center.y + 10f)
        lineTo(center.x - 15f, center.y + 10f)
        close()
      }
    drawPath(capsule, color = if (crashed) NebulaMagenta else Color.White)
    drawPath(capsule, color = ComicInk, style = Stroke(width = 2.8f))
    drawCircle(color = suitColor, radius = 6.5f, center = Offset(center.x, center.y - 4f))
  }
}
