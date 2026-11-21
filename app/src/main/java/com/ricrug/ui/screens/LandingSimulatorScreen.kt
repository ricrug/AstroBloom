package com.ricrug.ui.screens

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.CenterFocusStrong
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
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
  val tiltDeg =
    when {
      thrustLeft && !thrustRight -> -14f
      thrustRight && !thrustLeft -> 14f
      else -> (vx * 28f).coerceIn(-12f, 12f)
    }

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

    // Bottom Aerospace Flight Control Console
    Surface(
      color = SpaceNavy,
      modifier = Modifier.fillMaxWidth(),
    ) {
      LanderFlightConsole(
        thrustLeft = thrustLeft,
        thrustRight = thrustRight,
        thrustUp = thrustUp,
        currentSpeedMs = currentSpeedMs,
        tiltDeg = tiltDeg,
        onThrustLeftChange = { thrustLeft = it },
        onThrustRightChange = { thrustRight = it },
        onThrustUpChange = { thrustUp = it },
      )
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun LanderFlightConsole(
  thrustLeft: Boolean,
  thrustRight: Boolean,
  thrustUp: Boolean,
  currentSpeedMs: Int,
  tiltDeg: Float,
  onThrustLeftChange: (Boolean) -> Unit,
  onThrustRightChange: (Boolean) -> Unit,
  onThrustUpChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var controlMode by remember { mutableIntStateOf(0) } // 0: CLOCHE 3D, 1: D-PAD
  var stickThrustUp by remember { mutableStateOf(false) }
  var leverThrustUp by remember { mutableStateOf(false) }

  LaunchedEffect(stickThrustUp, leverThrustUp) {
    onThrustUpChange(stickThrustUp || leverThrustUp)
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          listOf(
            SpaceNavy,
            Color(0xFF070B18),
          )
        )
      )
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    // Top Mini Bar: Selettore Modalità Cloche/D-Pad + Titolo Console
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          onClick = { controlMode = 0 },
          color = if (controlMode == 0) CosmicCyan.copy(alpha = 0.32f) else SpaceCard,
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, if (controlMode == 0) CosmicCyan else Color.Transparent),
          modifier = Modifier.height(20.dp),
        ) {
          Text(
            text = "CLOCHE",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
            color = if (controlMode == 0) StarlightWhite else StarlightMuted,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false,
          )
        }
        Surface(
          onClick = { controlMode = 1 },
          color = if (controlMode == 1) AuroraGreen.copy(alpha = 0.32f) else SpaceCard,
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, if (controlMode == 1) AuroraGreen else Color.Transparent),
          modifier = Modifier.height(20.dp),
        ) {
          Text(
            text = "D-PAD",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
            color = if (controlMode == 1) StarlightWhite else StarlightMuted,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      Text(
        text = "CONSOLE DISCESA • 104DP",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
        color = CosmicCyan.copy(alpha = 0.75f),
        maxLines = 1,
        softWrap = false,
      )

      Text(
        text = "LEVA FRENO A MOLLA ▶",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Bold),
        color = CosmicAmber.copy(alpha = 0.9f),
        maxLines = 1,
        softWrap = false,
      )
    }

    // Main Control Bar: [Pad SX 104dp] + [Dashboard Assetto 104dp] + [Leva Freno DX 104dp]
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Pad SX di Pilotaggio Discesa (104dp)
      LanderFlightStickPad(
        controlMode = controlMode,
        thrustLeft = thrustLeft,
        thrustRight = thrustRight,
        onThrustLeftChange = onThrustLeftChange,
        onThrustRightChange = onThrustRightChange,
        onThrustUpChange = { stickThrustUp = it },
      )

      // Dashboard Centrale Assetto e Tachimetro (104dp)
      LanderAttitudeDashboard(
        currentSpeedMs = currentSpeedMs,
        tiltDeg = tiltDeg,
        thrustLeft = thrustLeft,
        thrustRight = thrustRight,
        thrustUp = thrustUp,
        modifier = Modifier.weight(1f),
      )

      // Leva Verticale del Freno con ritorno automatico a molla (104dp x 68dp)
      LanderMainThrustLever(
        thrustUp = thrustUp,
        onThrustUpChange = { leverThrustUp = it },
      )
    }
  }
}

@Composable
private fun LanderFlightStickPad(
  controlMode: Int,
  thrustLeft: Boolean,
  thrustRight: Boolean,
  onThrustLeftChange: (Boolean) -> Unit,
  onThrustRightChange: (Boolean) -> Unit,
  onThrustUpChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  val joyBoxSizeDp = 104.dp
  val joyBoxPx = with(density) { joyBoxSizeDp.toPx() }
  val centerPx = joyBoxPx / 2f
  val maxRadiusPx = joyBoxPx * 0.42f
  val deadzonePx = with(density) { 5.dp.toPx() }

  var stickX by remember { mutableFloatStateOf(0f) }
  var stickY by remember { mutableFloatStateOf(0f) }
  val isStickActive = abs(stickX) > 0.02f || abs(stickY) > 0.02f

  fun updateStick(pos: Offset) {
    val dx = pos.x - centerPx
    val dy = pos.y - centerPx
    val dist = hypot(dx, dy)
    if (dist > deadzonePx) {
      val norm = ((dist - deadzonePx) / (maxRadiusPx - deadzonePx)).coerceIn(0f, 1f)
      val curved = (norm * norm * 0.75f + norm * 0.25f).coerceIn(0f, 1f)
      val ang = atan2(dy, dx)
      stickX = (cos(ang) * curved).coerceIn(-1f, 1f)
      stickY = (sin(ang) * curved).coerceIn(-1f, 1f)
    } else {
      stickX = 0f
      stickY = 0f
    }
    onThrustLeftChange(stickX < -0.18f)
    onThrustRightChange(stickX > 0.18f)
    onThrustUpChange(stickY < -0.22f)
  }

  fun resetStick() {
    stickX = 0f
    stickY = 0f
    onThrustLeftChange(false)
    onThrustRightChange(false)
    onThrustUpChange(false)
  }

  if (controlMode == 0) {
    // MODALITÀ 0: CLOCHE 3D ANALOGICA CON RITORNO A MOLLA (104dp)
    Box(
      modifier = modifier
        .size(joyBoxSizeDp)
        .background(Color(0xD90A1128), CircleShape)
        .border(
          2.dp,
          Brush.radialGradient(
            listOf(
              if (isStickActive) CosmicAmber else CosmicCyan,
              Color(0xFF14203D),
            )
          ),
          CircleShape,
        )
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = { offset ->
              updateStick(offset)
              tryAwaitRelease()
              resetStick()
            }
          )
        }
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset -> updateStick(offset) },
            onDragEnd = { resetStick() },
            onDragCancel = { resetStick() },
          ) { change, _ ->
            change.consume()
            updateStick(change.position)
          }
        }
        .testTag("cockpit_flight_stick"),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2f, size.height / 2f)

        // Cerchi concentrici al neon e mirino
        drawCircle(
          color = CosmicCyan.copy(alpha = if (isStickActive) 0.65f else 0.25f),
          radius = size.minDimension * 0.46f,
          style = Stroke(width = if (isStickActive) 2.2f else 1.6f),
        )
        drawCircle(
          color = CosmicCyan.copy(alpha = 0.16f),
          radius = size.minDimension * 0.24f,
          style = Stroke(width = 1f),
        )
        drawLine(
          color = CosmicCyan.copy(alpha = 0.35f),
          start = Offset(c.x - 10f, c.y),
          end = Offset(c.x + 10f, c.y),
          strokeWidth = 1.2f,
        )
        drawLine(
          color = CosmicCyan.copy(alpha = 0.35f),
          start = Offset(c.x, c.y - 10f),
          end = Offset(c.x, c.y + 10f),
          strokeWidth = 1.2f,
        )

        // Frecce guida con retroilluminazione attiva
        val upColor = if (stickY < -0.22f) CosmicAmber else CosmicCyan.copy(alpha = 0.45f)
        val leftColor = if (thrustLeft || stickX < -0.18f) CosmicAmber else CosmicCyan.copy(alpha = 0.45f)
        val rightColor = if (thrustRight || stickX > 0.18f) CosmicAmber else CosmicCyan.copy(alpha = 0.45f)

        drawLine(color = upColor, start = Offset(c.x, 5f), end = Offset(c.x, 14f), strokeWidth = if (stickY < -0.22f) 3f else 1.8f)
        drawLine(color = leftColor, start = Offset(5f, c.y), end = Offset(14f, c.y), strokeWidth = if (thrustLeft) 3f else 1.8f)
        drawLine(color = rightColor, start = Offset(size.width - 5f, c.y), end = Offset(size.width - 14f, c.y), strokeWidth = if (thrustRight) 3f else 1.8f)

        // Raggio laser dinamico di forza
        val knob = Offset(c.x + stickX * maxRadiusPx, c.y + stickY * maxRadiusPx)
        if (isStickActive) {
          drawLine(
            color = CosmicCyan.copy(alpha = 0.70f),
            start = c,
            end = knob,
            strokeWidth = 2.6f,
          )
        }

        // Manopola cloche tridimensionale
        drawCircle(
          brush = Brush.radialGradient(
            listOf(
              if (isStickActive) CosmicAmber else Color(0xFFFFB703),
              Color(0xFFE76F51),
            )
          ),
          radius = 15.dp.toPx(),
          center = knob,
        )
        drawCircle(
          color = ComicInk,
          radius = 15.dp.toPx(),
          center = knob,
          style = Stroke(width = 2.2f),
        )
        drawCircle(
          color = StarlightWhite,
          radius = 3.dp.toPx(),
          center = Offset(knob.x - 3.dp.toPx(), knob.y - 3.dp.toPx()),
        )
      }

      // Nodi semantici accessibili per i test automatici
      Box(
        modifier = Modifier
          .size(1.dp)
          .align(Alignment.CenterStart)
          .testTag("lander_left_button")
          .semantics {
            onClick {
              onThrustLeftChange(true)
              true
            }
          },
      )
      Box(
        modifier = Modifier
          .size(1.dp)
          .align(Alignment.CenterEnd)
          .testTag("lander_right_button")
          .semantics {
            onClick {
              onThrustRightChange(true)
              true
            }
          },
      )
    }
  } else {
    // MODALITÀ 1: D-PAD TATTICO (104dp)
    Box(
      modifier = modifier
        .size(joyBoxSizeDp)
        .background(Color(0xD90A1128), RoundedCornerShape(16.dp))
        .border(2.dp, Brush.radialGradient(listOf(AuroraGreen, Color(0xFF14203D))), RoundedCornerShape(16.dp))
        .testTag("cockpit_flight_dpad"),
      contentAlignment = Alignment.Center,
    ) {
      // Pulsante FRENO SU
      Surface(
        color = if (stickY < -0.2f) CosmicAmber else SpaceCard,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.2.dp, if (stickY < -0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.6f)),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 4.dp)
          .size(width = 48.dp, height = 30.dp)
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = {
                onThrustUpChange(true)
                tryAwaitRelease()
                onThrustUpChange(false)
              }
            )
          },
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            "▲ FRENO",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
            color = StarlightWhite,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      // Pulsante SX (Vira a Sinistra)
      Surface(
        color = if (thrustLeft) CosmicAmber else SpaceCard,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.2.dp, if (thrustLeft) CosmicAmber else CosmicCyan.copy(alpha = 0.6f)),
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 4.dp)
          .size(width = 38.dp, height = 40.dp)
          .testTag("lander_left_button")
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = {
                onThrustLeftChange(true)
                tryAwaitRelease()
                onThrustLeftChange(false)
              }
            )
          },
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            "◀ SX",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = StarlightWhite,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      // Pulsante DX (Vira a Destra)
      Surface(
        color = if (thrustRight) CosmicAmber else SpaceCard,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.2.dp, if (thrustRight) CosmicAmber else CosmicCyan.copy(alpha = 0.6f)),
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 4.dp)
          .size(width = 38.dp, height = 40.dp)
          .testTag("lander_right_button")
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = {
                onThrustRightChange(true)
                tryAwaitRelease()
                onThrustRightChange(false)
              }
            )
          },
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            "DX ▶",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = StarlightWhite,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      // Icona centrale D-Pad
      Box(
        modifier = Modifier
          .size(20.dp)
          .background(Color(0xFF0F1A35), CircleShape)
          .border(1.dp, AuroraGreen.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          Icons.Rounded.CenterFocusStrong,
          contentDescription = null,
          tint = AuroraGreen,
          modifier = Modifier.size(12.dp),
        )
      }
    }
  }
}

@Composable
private fun LanderAttitudeDashboard(
  currentSpeedMs: Int,
  tiltDeg: Float,
  thrustLeft: Boolean,
  thrustRight: Boolean,
  thrustUp: Boolean,
  modifier: Modifier = Modifier,
) {
  val speedColor =
    when {
      currentSpeedMs <= 22 -> AuroraGreen
      currentSpeedMs <= 26 -> CosmicAmber
      else -> NebulaMagenta
    }

  Surface(
    color = Color(0xD90A1128),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.5.dp, Brush.verticalGradient(listOf(CosmicCyan.copy(alpha = 0.7f), Color(0xFF14203D)))),
    modifier = modifier.height(104.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 6.dp, vertical = 5.dp),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Header: Titolo con valore numerico inclinazione
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "ORIZZONTE",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Bold),
          color = CosmicCyan,
          maxLines = 1,
          softWrap = false,
        )
        val tiltRounded = tiltDeg.roundToInt()
        val tiltLabel = when {
          tiltRounded < -2 -> "${abs(tiltRounded)}° SX"
          tiltRounded > 2 -> "${tiltRounded}° DX"
          else -> "0° RETTO"
        }
        Text(
          text = tiltLabel,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Bold),
          color = if (abs(tiltRounded) > 10) NebulaMagenta else AuroraGreen,
          maxLines = 1,
          softWrap = false,
        )
      }

      // Middle: Giroscopio Orizzonte Artificiale in tempo reale
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF090E1F))
      ) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        // Orizzonte rotante secondo tiltDeg
        rotate(degrees = -tiltDeg, pivot = c) {
          // Cielo / Suolo
          drawRect(
            color = CosmicCyan.copy(alpha = 0.12f),
            topLeft = Offset(0f, 0f),
            size = Size(w, c.y),
          )
          drawRect(
            color = CosmicAmber.copy(alpha = 0.12f),
            topLeft = Offset(0f, c.y),
            size = Size(w, h - c.y),
          )

          // Linea d'orizzonte
          drawLine(
            color = CosmicCyan,
            start = Offset(4f, c.y),
            end = Offset(w - 4f, c.y),
            strokeWidth = 1.8f,
          )

          // Tacche pitch ladder
          drawLine(
            color = CosmicCyan.copy(alpha = 0.45f),
            start = Offset(c.x - 12f, c.y - 11f),
            end = Offset(c.x + 12f, c.y - 11f),
            strokeWidth = 1f,
          )
          drawLine(
            color = CosmicCyan.copy(alpha = 0.45f),
            start = Offset(c.x - 12f, c.y + 11f),
            end = Offset(c.x + 12f, c.y + 11f),
            strokeWidth = 1f,
          )
        }

        // Sagoma fissa del lander al centro
        drawLine(
          color = CosmicAmber,
          start = Offset(c.x - 12f, c.y),
          end = Offset(c.x - 4f, c.y),
          strokeWidth = 2.2f,
        )
        drawLine(
          color = CosmicAmber,
          start = Offset(c.x + 4f, c.y),
          end = Offset(c.x + 12f, c.y),
          strokeWidth = 2.2f,
        )
        drawCircle(
          color = CosmicAmber,
          radius = 2.2f,
          center = c,
        )
      }

      // Bottom Row: Telemetria RCS & Tachimetro Discesa
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // RCS SX LED
        Surface(
          color = if (thrustLeft) CosmicAmber.copy(alpha = 0.35f) else SpaceCard,
          shape = RoundedCornerShape(4.dp),
          border = BorderStroke(1.dp, if (thrustLeft) CosmicAmber else Color(0x3300F5D4)),
          modifier = Modifier.height(18.dp),
        ) {
          Text(
            text = "◀ RCS",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Bold),
            color = if (thrustLeft) CosmicAmber else StarlightMuted,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false,
          )
        }

        // Speed badge
        Surface(
          color = speedColor.copy(alpha = 0.22f),
          shape = RoundedCornerShape(4.dp),
          border = BorderStroke(1.dp, speedColor),
          modifier = Modifier.height(18.dp),
        ) {
          Text(
            text = "$currentSpeedMs m/s",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
            color = speedColor,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false,
          )
        }

        // RCS DX LED
        Surface(
          color = if (thrustRight) CosmicAmber.copy(alpha = 0.35f) else SpaceCard,
          shape = RoundedCornerShape(4.dp),
          border = BorderStroke(1.dp, if (thrustRight) CosmicAmber else Color(0x3300F5D4)),
          modifier = Modifier.height(18.dp),
        ) {
          Text(
            text = "RCS ▶",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp, fontWeight = FontWeight.Bold),
            color = if (thrustRight) CosmicAmber else StarlightMuted,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }
  }
}

@Composable
private fun LanderMainThrustLever(
  thrustUp: Boolean,
  onThrustUpChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  val leverWidthDp = 68.dp
  val leverHeightDp = 104.dp
  val leverHeightPx = with(density) { leverHeightDp.toPx() }

  var leverNorm by remember { mutableFloatStateOf(0f) }

  fun updateFromY(yPx: Float) {
    val normY = (yPx / leverHeightPx).coerceIn(0f, 1f)
    leverNorm = (1f - normY).coerceIn(0f, 1f)
    onThrustUpChange(leverNorm > 0.08f)
  }

  fun resetLever() {
    leverNorm = 0f
    onThrustUpChange(false)
  }

  Box(
    modifier = modifier
      .size(width = leverWidthDp, height = leverHeightDp)
      .background(Color(0xD90A1128), RoundedCornerShape(12.dp))
      .border(
        1.5.dp,
        Brush.verticalGradient(
          listOf(
            NebulaMagenta,
            CosmicAmber,
            CosmicCyan,
          )
        ),
        RoundedCornerShape(12.dp),
      )
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = { offset ->
            updateFromY(offset.y)
            tryAwaitRelease()
            resetLever()
          }
        )
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset -> updateFromY(offset.y) },
          onDragEnd = { resetLever() },
          onDragCancel = { resetLever() },
        ) { change, _ ->
          change.consume()
          updateFromY(change.position.y)
        }
      }
      .semantics {
        onClick {
          onThrustUpChange(true)
          true
        }
      }
      .testTag("lander_thrust_button"),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 6.dp)) {
      val w = size.width
      val h = size.height

      // Binario scanalato di scorrimento verticale
      val trackX = w * 0.30f
      val trackW = 8.dp.toPx()
      drawRoundRect(
        color = Color(0xFF070B16),
        topLeft = Offset(trackX - trackW / 2f, 0f),
        size = Size(trackW, h),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
      )
      drawRoundRect(
        color = CosmicCyan.copy(alpha = 0.35f),
        topLeft = Offset(trackX - trackW / 2f, 0f),
        size = Size(trackW, h),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 1f),
      )

      // Plume fiammeggiante nel binario quando il propulsore è acceso
      val effectiveNorm = if (thrustUp && leverNorm < 0.2f) 0.85f else leverNorm
      if (effectiveNorm > 0.05f) {
        val plumeHeight = h * effectiveNorm
        drawRoundRect(
          brush = Brush.verticalGradient(
            listOf(
              CosmicAmber,
              Color(0xFFFFB703),
              NebulaMagenta.copy(alpha = 0.4f),
            ),
            startY = h - plumeHeight,
            endY = h,
          ),
          topLeft = Offset(trackX - trackW / 2f + 1f, h - plumeHeight),
          size = Size(trackW - 2f, plumeHeight),
          cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        )
      }

      // Tacche di graduazione lungo il lato destro
      val stepCount = 4
      for (i in 0..stepCount) {
        val tickY = (h / stepCount) * i
        val isZero = i == stepCount
        val tickW = if (isZero) 10.dp.toPx() else 6.dp.toPx()
        drawLine(
          color = if (isZero) CosmicCyan else CosmicAmber.copy(alpha = 0.6f),
          start = Offset(trackX + trackW / 2f + 3f, tickY),
          end = Offset(trackX + trackW / 2f + 3f + tickW, tickY),
          strokeWidth = if (isZero) 1.5f else 1f,
        )
      }

      // Manopola 3D della Leva ergonomica
      val knobY = (h * (1f - effectiveNorm)).coerceIn(10.dp.toPx(), h - 10.dp.toPx())
      val knobW = w * 0.72f
      val knobH = 18.dp.toPx()
      val knobX = trackX - knobW * 0.35f

      // Ombra e bordo della manopola
      drawRoundRect(
        color = ComicInk,
        topLeft = Offset(knobX - 1f, knobY - knobH / 2f - 1f),
        size = Size(knobW + 2f, knobH + 2f),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
      )

      // Corpo metallico / ambra della manopola
      drawRoundRect(
        brush = Brush.verticalGradient(
          listOf(
            if (effectiveNorm > 0.05f) CosmicAmber else Color(0xFF6B7280),
            if (effectiveNorm > 0.05f) Color(0xFFD97706) else Color(0xFF374151),
          )
        ),
        topLeft = Offset(knobX, knobY - knobH / 2f),
        size = Size(knobW, knobH),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
      )

      // Scanalature antiscivolo
      val ridgeColor = if (effectiveNorm > 0.05f) StarlightWhite.copy(alpha = 0.5f) else Color(0xFF9CA3AF)
      drawLine(
        color = ridgeColor,
        start = Offset(knobX + 6.dp.toPx(), knobY - 3.dp.toPx()),
        end = Offset(knobX + knobW - 6.dp.toPx(), knobY - 3.dp.toPx()),
        strokeWidth = 1.2f,
      )
      drawLine(
        color = ridgeColor,
        start = Offset(knobX + 6.dp.toPx(), knobY + 3.dp.toPx()),
        end = Offset(knobX + knobW - 6.dp.toPx(), knobY + 3.dp.toPx()),
        strokeWidth = 1.2f,
      )
    }

    // Etichetta del freno
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 4.dp, bottom = 4.dp),
      horizontalAlignment = Alignment.End,
    ) {
      Text(
        text = "FRENO",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
        color = if (thrustUp) CosmicAmber else StarlightMuted,
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
