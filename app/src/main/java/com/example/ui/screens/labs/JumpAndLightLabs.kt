package com.example.ui.screens.labs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Send
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SolarSystemData
import com.example.model.ChibiExpression
import com.example.model.ChibiProfile
import com.example.ui.components.ChibiComicBubble
import com.example.ui.theme.AuroraGreen
import com.example.ui.theme.ComicInk
import com.example.ui.theme.CosmicAmber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.NebulaMagenta
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardElevated
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.ui.theme.SpaceNavy
import com.example.ui.theme.SpaceVoid
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.StarlightWhite
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SpaceJumpLab(
  chibiProfile: ChibiProfile,
  modifier: Modifier = Modifier,
) {
  var earthJumpCm by remember { mutableFloatStateOf(40f) }
  var isJumping by remember { mutableStateOf(false) }
  var jumpTrigger by remember { mutableIntStateOf(0) }
  var elapsedSec by remember { mutableFloatStateOf(0f) }

  val targetIds = listOf("earth", "moon", "mars", "jupiter", "pluto")
  val targets = remember { targetIds.map { SolarSystemData.getPlanetById(it) } }
  val textMeasurer = rememberTextMeasurer()

  // Realistic gravity ratios relative to Earth (9.81 m/s^2)
  // Max hang time for Pluto (0.063g): t_total = 2 * sqrt(2 * h_earth / g_earth) / gravityRatio
  // For UI playfulness we use real height ratio (h = h_earth / gravityRatio) and smoothly scaled flight time (~3.6s max)
  val earthJumpM = earthJumpCm / 100f
  val maxJumpM = earthJumpM / 0.063f
  val animDurationSec = 3.4f

  LaunchedEffect(jumpTrigger) {
    if (jumpTrigger == 0) return@LaunchedEffect
    isJumping = true
    elapsedSec = 0f
    var lastNanos = 0L
    while (elapsedSec < animDurationSec) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          elapsedSec += dt
        }
        lastNanos = nanos
      }
    }
    isJumping = false
  }

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.5.dp, AuroraGreen.copy(alpha = 0.65f)),
    modifier = modifier.fillMaxWidth().testTag("lab_panel_jump"),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Height, contentDescription = null, tint = AuroraGreen)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Salto Spaziale: Quanto in alto voli?",
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
      }

      Text(
        text = "Scegli quanto salti sulla Terra e guarda SpaceBloom spiccare il volo sugli altri mondi con la loro vera gravità!",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )

      Text(
        text = "Il tuo salto sulla Terra: ${earthJumpCm.roundToInt()} cm",
        style = MaterialTheme.typography.labelLarge,
        color = CosmicAmber,
      )
      Slider(
        value = earthJumpCm,
        onValueChange = { earthJumpCm = it },
        valueRange = 20f..80f,
        colors = SliderDefaults.colors(thumbColor = CosmicAmber, activeTrackColor = AuroraGreen),
        modifier = Modifier.testTag("lab_jump_slider"),
      )

      // Animated Jump Arena Canvas
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SpaceVoid),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF070B1E), Color(0xFF131D3D))),
          )

          val groundY = size.height - 40f
          val skyTop = 26f
          val usableH = (groundY - skyTop).coerceAtLeast(60f)
          val scaleMaxM = maxOf(maxJumpM * 1.08f, 3.2f)

          // Basketball hoop reference line at 3.05 m
          val hoopY = groundY - (3.05f / scaleMaxM) * usableH
          if (hoopY in skyTop..groundY) {
            drawLine(
              color = CosmicAmber.copy(alpha = 0.55f),
              start = Offset(8f, hoopY),
              end = Offset(size.width - 8f, hoopY),
              strokeWidth = 2f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
            )
            val hoopText =
              textMeasurer.measure(
                "Canestro NBA (3,05 m)",
                TextStyle(fontFamily = SpaceMonoFontFamily, fontSize = 9.sp, color = CosmicAmber),
              )
            drawText(hoopText, topLeft = Offset(10f, (hoopY - hoopText.size.height - 2f).coerceAtLeast(4f)))
          }

          val colW = size.width / targets.size
          targets.forEachIndexed { idx, planet ->
            val cx = colW * idx + colW * 0.5f
            val peakM = earthJumpM / planet.gravityRatio
            val peakPx = (peakM / scaleMaxM) * usableH

            // Flight duration proportional to 1 / sqrt(g) or 1 / g capped nicely inside animDurationSec
            val planetFlightSec = (0.65f / planet.gravityRatio.coerceAtLeast(0.18f)).coerceIn(0.45f, animDurationSec)
            val currentHeightPx =
              if (isJumping && elapsedSec <= planetFlightSec) {
                val u = elapsedSec / planetFlightSec
                // Parabolic arc: 4 * u * (1 - u) peaks at 1.0 at u = 0.5
                peakPx * (4f * u * (1f - u))
              } else {
                0f
              }

            // Ground platform pad
            drawRoundRect(
              color = planet.primaryColor.copy(alpha = 0.85f),
              topLeft = Offset(cx - colW * 0.34f, groundY),
              size = Size(colW * 0.68f, 8f),
            )

            // Peak ghost marker line
            val peakY = groundY - peakPx
            drawLine(
              color = planet.accentColor.copy(alpha = 0.35f),
              start = Offset(cx, groundY),
              end = Offset(cx, peakY),
              strokeWidth = 2.5f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )

            // Jumper capsule (mini SpaceBloom astronaut helmet + jetpack boots!)
            val jumperY = groundY - currentHeightPx - 12f
            drawCircle(
              color = planet.accentColor,
              radius = 11f,
              center = Offset(cx, jumperY),
            )
            drawCircle(
              color = Color.White,
              radius = 6f,
              center = Offset(cx, jumperY - 1f),
            )
            // Visor
            drawRoundRect(
              color = ComicInk,
              topLeft = Offset(cx - 4f, jumperY - 3f),
              size = Size(8f, 4f),
            )

            // Peak height label
            val peakLabel =
              if (peakM >= 1f) String.format(Locale.ITALIAN, "%.1fm", peakM)
              else "${(peakM * 100f).roundToInt()}cm"
            val hMeas =
              textMeasurer.measure(
                peakLabel,
                TextStyle(
                  fontFamily = SpaceMonoFontFamily,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = planet.accentColor,
                ),
              )
            drawText(
              hMeas,
              topLeft = Offset(cx - hMeas.size.width / 2f, (peakY - hMeas.size.height - 2f).coerceAtLeast(4f)),
            )

            // Planet name under ground
            val nMeas =
              textMeasurer.measure(
                planet.name.replace("La ", ""),
                TextStyle(fontFamily = FredokaFontFamily, fontSize = 11.sp, color = StarlightWhite),
              )
            drawText(
              nMeas,
              topLeft = Offset(cx - nMeas.size.width / 2f, groundY + 12f),
            )
          }
        }
      }

      Button(
        onClick = { jumpTrigger++ },
        colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("lab_jump_button"),
      ) {
        Icon(if (isJumping) Icons.Rounded.Replay else Icons.Rounded.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isJumping) "Salta di nuovo!" else "SALTA ORA SU 5 MONDI!",
          style = MaterialTheme.typography.titleMedium,
        )
      }

      val moonJump = String.format(Locale.ITALIAN, "%.1f m", earthJumpM / 0.165f)
      val plutoJump = String.format(Locale.ITALIAN, "%.1f m", earthJumpM / 0.063f)
      val jupiterCm = ((earthJumpM / 2.53f) * 100f).roundToInt()
      ChibiComicBubble(
        profile = chibiProfile,
        expression = ChibiExpression.CHEERING,
        dialogue =
          "Con un salto di ${earthJumpCm.roundToInt()} cm arriveresti a $moonJump sulla Luna e addirittura a $plutoJump su Plutone! Su Giove invece salteresti solo $jupiterCm cm!",
        avatarSize = 62.dp,
      )
    }
  }
}

private data class LightTarget(
  val id: String,
  val name: String,
  val distanceKm: Long,
  val distanceLabel: String,
  val lightSeconds: Float,
  val funComparison: String,
  val color: Color,
)

private val lightTargets =
  listOf(
    LightTarget("moon", "La Luna", 384_400L, "384.400 km", 1.28f, "Meno di un battito di ciglia lungo — appena 1,3 secondi!", Color(0xFFD6DCE8)),
    LightTarget("sun", "Il Sole", 149_600_000L, "150 milioni km", 499f, "8 minuti e 20 secondi: il tempo di fare colazione! La luce che vedi è già partita 8 minuti fa!", Color(0xFFFFB703)),
    LightTarget("mars", "Marte", 225_000_000L, "225 milioni km (media)", 750f, "Circa 12 minuti e mezzo solo per arrivare: ecco perché i rover su Marte guidano da soli!", Color(0xFFE76F51)),
    LightTarget("jupiter", "Giove", 778_000_000L, "778 milioni km", 2595f, "Circa 43 minuti: il tempo di una puntata intera del tuo cartone preferito!", Color(0xFFE9C46A)),
    LightTarget("pluto", "Plutone", 5_900_000_000L, "5,9 miliardi km", 19_680f, "Quasi 5 ore e mezza! Per avere una risposta dovresti aspettare quasi 11 ore!", Color(0xFFD4A373)),
  )

@Composable
fun LightSpeedLab(
  chibiProfile: ChibiProfile,
  modifier: Modifier = Modifier,
) {
  var selectedIdx by remember { mutableIntStateOf(0) }
  var beamProgress by remember { mutableFloatStateOf(0f) }
  var isSending by remember { mutableStateOf(false) }
  var sendTrigger by remember { mutableIntStateOf(0) }
  var arrivedCount by remember { mutableIntStateOf(0) }

  val target = lightTargets[selectedIdx]
  // Visual duration in seconds (proportional feel without making kids wait 5 real hours!)
  val visualDurationSec =
    when (target.id) {
      "moon" -> 1.28f
      "sun" -> 3.0f
      "mars" -> 4.0f
      "jupiter" -> 5.2f
      else -> 6.5f
    }

  LaunchedEffect(sendTrigger) {
    if (sendTrigger == 0) return@LaunchedEffect
    isSending = true
    beamProgress = 0f
    var lastNanos = 0L
    while (beamProgress < 1f) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          beamProgress = (beamProgress + dt / visualDurationSec).coerceAtMost(1f)
        }
        lastNanos = nanos
      }
    }
    isSending = false
    arrivedCount++
  }

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.65f)),
    modifier = modifier.fillMaxWidth().testTag("lab_panel_light"),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = CosmicCyan)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Messaggi alla Velocità della Luce",
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
      }

      Text(
        text = "La luce viaggia a 300.000 km al secondo! Scegli dove inviare un 'Ciao!' radio dalla Terra.",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )

      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        lightTargets.forEachIndexed { idx, item ->
          FilterChip(
            selected = selectedIdx == idx,
            onClick = {
              selectedIdx = idx
              beamProgress = 0f
              isSending = false
            },
            label = { Text(item.name) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = CosmicCyan,
                selectedLabelColor = ComicInk,
              ),
            modifier = Modifier.testTag("lab_light_target_${item.id}"),
          )
        }
      }

      // Laser Space Beam Canvas
      val textMeasurer = rememberTextMeasurer()
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SpaceVoid),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawRect(Brush.horizontalGradient(listOf(Color(0xFF0A1633), Color(0xFF050816))))

          val leftX = 52f
          val rightX = size.width - 52f
          val cy = size.height * 0.48f

          // Trajectory dotted guide
          drawLine(
            color = StarlightMuted.copy(alpha = 0.35f),
            start = Offset(leftX, cy),
            end = Offset(rightX, cy),
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
          )

          // Active glowing beam
          if (beamProgress > 0f) {
            val tipX = leftX + (rightX - leftX) * beamProgress
            drawLine(
              brush = Brush.horizontalGradient(listOf(CosmicCyan.copy(alpha = 0.2f), CosmicCyan, Color.White)),
              start = Offset(leftX, cy),
              end = Offset(tipX, cy),
              strokeWidth = 8f,
              cap = StrokeCap.Round,
            )
            // Pulse head
            drawCircle(
              color = Color.White,
              radius = 9f,
              center = Offset(tipX, cy),
            )
            drawCircle(
              color = CosmicCyan.copy(alpha = 0.45f),
              radius = 18f,
              center = Offset(tipX, cy),
            )
          }

          // Earth on left
          drawCircle(color = Color(0xFF2B6CB0), radius = 22f, center = Offset(leftX, cy))
          drawCircle(color = Color(0xFF48BB78), radius = 8f, center = Offset(leftX - 5f, cy - 4f))
          drawCircle(color = Color(0xFF48BB78), radius = 6f, center = Offset(leftX + 7f, cy + 5f))

          // Target on right
          drawCircle(color = target.color, radius = 24f, center = Offset(rightX, cy))
          if (beamProgress >= 1f) {
            drawCircle(
              color = AuroraGreen,
              radius = 32f,
              center = Offset(rightX, cy),
              style = Stroke(width = 3f),
            )
          }

          val earthLbl = textMeasurer.measure("Terra", TextStyle(fontFamily = FredokaFontFamily, fontSize = 11.sp, color = StarlightWhite))
          val targetLbl = textMeasurer.measure(target.name, TextStyle(fontFamily = FredokaFontFamily, fontSize = 11.sp, color = target.color))
          drawText(earthLbl, topLeft = Offset(leftX - earthLbl.size.width / 2f, cy + 30f))
          drawText(targetLbl, topLeft = Offset(rightX - targetLbl.size.width / 2f, cy + 30f))
        }
      }

      // Live Telemetry Strip
      Surface(
        color = SpaceNavy,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text("Distanza: ${target.distanceLabel}", style = MaterialTheme.typography.labelMedium, color = StarlightMuted)
            val simulatedSeconds = target.lightSeconds * beamProgress
            Text(
              text = "Tempo luce reale: ${formatLightTime(target.lightSeconds)}",
              style = MaterialTheme.typography.titleMedium,
              color = CosmicAmber,
            )
            if (beamProgress > 0f && beamProgress < 1f) {
              Text(
                text = "In viaggio: ${formatLightTime(simulatedSeconds)}...",
                style = MaterialTheme.typography.labelSmall,
                color = CosmicCyan,
              )
            } else if (beamProgress >= 1f) {
              Text(
                text = "Messaggio ricevuto su ${target.name}!",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraGreen,
              )
            }
          }
          Button(
            onClick = { sendTrigger++ },
            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = Modifier.testTag("lab_light_send_button"),
          ) {
            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isSending) "In volo" else "Invia!", style = MaterialTheme.typography.labelLarge)
          }
        }
      }

      ChibiComicBubble(
        profile = chibiProfile,
        expression = if (beamProgress >= 1f) ChibiExpression.AMAZED else ChibiExpression.THINKING,
        dialogue = target.funComparison,
        avatarSize = 62.dp,
      )
    }
  }
}

private fun formatLightTime(seconds: Float): String {
  val totalSec = seconds.roundToInt()
  return when {
    seconds < 10f -> String.format(Locale.ITALIAN, "%.2f secondi", seconds)
    totalSec < 60 -> "$totalSec secondi"
    totalSec < 3600 -> {
      val m = totalSec / 60
      val s = totalSec % 60
      if (s == 0) "$m min" else "$m min $s sec"
    }
    else -> {
      val h = totalSec / 3600
      val m = (totalSec % 3600) / 60
      "$h ore e $m min"
    }
  }
}
