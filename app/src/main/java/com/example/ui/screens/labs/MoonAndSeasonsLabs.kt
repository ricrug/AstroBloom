package com.example.ui.screens.labs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness3
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun MoonPhasesAndEclipseLab(
  chibiProfile: ChibiProfile,
  modifier: Modifier = Modifier,
) {
  // 0 deg = Moon directly between Sun (left) and Earth (center) -> New Moon / Solar Eclipse!
  // 90 deg = First Quarter; 180 deg = Full Moon / Lunar Eclipse; 270 deg = Last Quarter
  var angleDeg by remember { mutableFloatStateOf(45f) }
  var eclipseAligned by remember { mutableStateOf(true) }

  val normalizedDeg = ((angleDeg % 360f) + 360f) % 360f
  val isSolarEclipse = eclipseAligned && (normalizedDeg <= 10f || normalizedDeg >= 350f)
  val isLunarEclipse = eclipseAligned && abs(normalizedDeg - 180f) <= 10f

  val phaseName =
    when {
      isSolarEclipse -> "ECLISSI SOLARE!"
      isLunarEclipse -> "ECLISSI LUNARE (Luna Rossa!)"
      normalizedDeg < 22.5f || normalizedDeg >= 337.5f -> "Luna Nuova"
      normalizedDeg < 67.5f -> "Luna Crescente"
      normalizedDeg < 112.5f -> "Primo Quarto"
      normalizedDeg < 157.5f -> "Gibbosa Crescente"
      normalizedDeg < 202.5f -> "Luna Piena"
      normalizedDeg < 247.5f -> "Gibbosa Calante"
      normalizedDeg < 292.5f -> "Ultimo Quarto"
      else -> "Luna Calante"
    }

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.5.dp, CosmicAmber.copy(alpha = 0.65f)),
    modifier = modifier.fillMaxWidth().testTag("lab_panel_moon"),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Brightness3, contentDescription = null, tint = CosmicAmber)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Fasi della Luna ed Eclissi",
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
      }

      Text(
        text = "Sposta la Luna attorno alla Terra per scoprire come cambia vista da casa nostra, o allineala con il Sole per creare un'eclissi!",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )

      // Orbit + Earth-View Split Canvas
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Left: Top-down Orbit diagram (Sun rays from left)
        Box(
          modifier =
            Modifier.weight(1.45f)
              .height(195.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(SpaceVoid),
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.horizontalGradient(listOf(Color(0xFF281904), Color(0xFF070C1F), Color(0xFF040712))))

            // Incoming Sun rays from left edge
            for (i in 0..4) {
              val ry = size.height * (0.18f + i * 0.16f)
              drawLine(
                color = CosmicAmber.copy(alpha = 0.42f),
                start = Offset(4f, ry),
                end = Offset(46f, ry),
                strokeWidth = 3f,
                cap = StrokeCap.Round,
              )
            }

            val earthCenter = Offset(size.width * 0.56f, size.height * 0.5f)
            val orbitR = size.minDimension * 0.35f

            // Orbit circle
            drawCircle(
              color = StarlightMuted.copy(alpha = 0.35f),
              radius = orbitR,
              center = earthCenter,
              style = Stroke(width = 2f),
            )

            // Angle 0 points left (toward the Sun)
            val rad = Math.toRadians(normalizedDeg.toDouble())
            val moonX = earthCenter.x - (cos(rad) * orbitR).toFloat()
            val moonY = earthCenter.y - (sin(rad) * orbitR).toFloat()
            val moonCenter = Offset(moonX, moonY)

            // Earth's shadow cone to the right when aligned
            if (isLunarEclipse) {
              val shadowPath =
                Path().apply {
                  moveTo(earthCenter.x, earthCenter.y - 20f)
                  lineTo(size.width, earthCenter.y - 16f)
                  lineTo(size.width, earthCenter.y + 16f)
                  lineTo(earthCenter.x, earthCenter.y + 20f)
                  close()
                }
              drawPath(shadowPath, color = Color.Black.copy(alpha = 0.65f))
            }

            // Moon shadow cone during Solar Eclipse
            if (isSolarEclipse) {
              val shadowPath =
                Path().apply {
                  moveTo(moonCenter.x, moonCenter.y - 10f)
                  lineTo(earthCenter.x, earthCenter.y - 6f)
                  lineTo(earthCenter.x, earthCenter.y + 6f)
                  lineTo(moonCenter.x, moonCenter.y + 10f)
                  close()
                }
              drawPath(shadowPath, color = Color.Black.copy(alpha = 0.72f))
            }

            // Earth (left half lit by Sun, right half dark)
            val earthClip = Path().apply { addOval(androidx.compose.ui.geometry.Rect(earthCenter.x - 19f, earthCenter.y - 19f, earthCenter.x + 19f, earthCenter.y + 19f)) }
            clipPath(earthClip) {
              drawCircle(color = Color(0xFF2B6CB0), radius = 19f, center = earthCenter)
              drawCircle(color = Color(0xFF48BB78), radius = 7f, center = Offset(earthCenter.x - 5f, earthCenter.y - 5f))
              drawRect(
                color = Color.Black.copy(alpha = 0.55f),
                topLeft = Offset(earthCenter.x, earthCenter.y - 20f),
                size = Size(22f, 40f),
              )
            }

            // Moon in orbit (left half facing Sun always lit!)
            val moonTint = if (isLunarEclipse) Color(0xFFB83B2E) else Color(0xFFE2E8F0)
            val moonClip = Path().apply { addOval(androidx.compose.ui.geometry.Rect(moonCenter.x - 10f, moonCenter.y - 10f, moonCenter.x + 10f, moonCenter.y + 10f)) }
            clipPath(moonClip) {
              drawCircle(color = moonTint, radius = 10f, center = moonCenter)
              drawRect(
                color = Color(0xFF1A202C).copy(alpha = if (isLunarEclipse) 0.45f else 0.78f),
                topLeft = Offset(moonCenter.x, moonCenter.y - 11f),
                size = Size(12f, 22f),
              )
            }
            drawCircle(color = CosmicCyan, radius = 12f, center = moonCenter, style = Stroke(width = 1.5f))
          }
        }

        // Right: View from Earth preview!
        Surface(
          color = SpaceNavy,
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.dp, if (isSolarEclipse || isLunarEclipse) CosmicAmber else SpaceVoid),
          modifier = Modifier.weight(1f).height(195.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Text(
              text = "Vista dalla Terra",
              style = MaterialTheme.typography.labelSmall,
              color = StarlightMuted,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.size(86.dp)) {
              val r = size.minDimension * 0.43f
              val c = Offset(size.width / 2f, size.height / 2f)

              if (isSolarEclipse) {
                // Corona + black Moon silhouette over Sun
                drawCircle(
                  brush = Brush.radialGradient(listOf(CosmicAmber, Color.Transparent)),
                  radius = r * 1.18f,
                  center = c,
                )
                drawCircle(color = Color(0xFF0A0D18), radius = r * 0.84f, center = c)
              } else {
                val baseColor = if (isLunarEclipse) Color(0xFFC04832) else Color(0xFFEDF2F7)
                val darkColor = Color(0xFF1A2035)
                drawCircle(color = darkColor, radius = r, center = c)

                // Illumination fraction from phase angle (0 = New, 180 = Full)
                val phaseRad = Math.toRadians(normalizedDeg.toDouble())
                val illum = ((1.0 - cos(phaseRad)) / 2.0).toFloat()
                // Waxing: 0..180 lit on right; Waning: 180..360 lit on left
                val waxing = normalizedDeg <= 180f

                val moonClip = Path().apply {
                  addOval(androidx.compose.ui.geometry.Rect(c.x - r, c.y - r, c.x + r, c.y + r))
                }
                clipPath(moonClip) {
                  if (illum > 0.01f) {
                    // Draw lit half + terminator ellipse
                    if (waxing) {
                      drawRect(color = baseColor, topLeft = Offset(c.x, c.y - r), size = Size(r, r * 2f))
                    } else {
                      drawRect(color = baseColor, topLeft = Offset(c.x - r, c.y - r), size = Size(r, r * 2f))
                    }
                    val rx = r * abs(cos(phaseRad)).toFloat()
                    val overlayColor = if (illum >= 0.5f) baseColor else darkColor
                    drawOval(
                      color = overlayColor,
                      topLeft = Offset(c.x - rx, c.y - r),
                      size = Size(rx * 2f, r * 2f),
                    )
                  }
                }
                drawCircle(color = StarlightMuted.copy(alpha = 0.35f), radius = r, center = c, style = Stroke(width = 1.5f))
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = phaseName,
              style = MaterialTheme.typography.labelLarge,
              color = if (isSolarEclipse || isLunarEclipse) CosmicAmber else CosmicCyan,
              modifier = Modifier.testTag("lab_moon_phase_name"),
            )
          }
        }
      }

      // Orbit slider
      Slider(
        value = angleDeg,
        onValueChange = { angleDeg = it },
        valueRange = 0f..360f,
        colors = SliderDefaults.colors(thumbColor = CosmicAmber, activeTrackColor = CosmicAmber),
        modifier = Modifier.testTag("lab_moon_slider"),
      )

      // Preset phase chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        listOf(
          0f to "Nuova",
          90f to "1° Quarto",
          180f to "Piena",
          270f to "Ultimo Q.",
        ).forEach { (deg, lbl) ->
          FilterChip(
            selected = abs(normalizedDeg - deg) < 12f,
            onClick = { angleDeg = deg },
            label = { Text(lbl, style = MaterialTheme.typography.labelSmall) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicAmber, selectedLabelColor = ComicInk),
            modifier = Modifier.weight(1f),
          )
        }
      }

      FilterChip(
        selected = eclipseAligned,
        onClick = { eclipseAligned = !eclipseAligned },
        label = { Text(if (eclipseAligned) "Modalità Eclissi Attiva (0° / 180°)" else "Orbita normale inclinata 5°") },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NebulaMagenta, selectedLabelColor = StarlightWhite),
      )

      val msg =
        when {
          isSolarEclipse -> "Incredibile! La Luna è proprio davanti al Sole: proietta la sua ombra sulla Terra ed è giorno buio per qualche minuto!"
          isLunarEclipse -> "La Terra blocca la luce del Sole e la Luna diventa rossa rame come al tramonto! Si chiama Luna di Sangue!"
          else -> "La Luna non brilla di luce propria: riflette la luce del Sole come uno specchio gigante! Noi vediamo solo la parte illuminata."
        }
      ChibiComicBubble(
        profile = chibiProfile,
        expression = if (isSolarEclipse || isLunarEclipse) ChibiExpression.AMAZED else ChibiExpression.HAPPY,
        dialogue = msg,
        avatarSize = 62.dp,
      )
    }
  }
}

@Composable
fun OrbitsAndSeasonsLab(
  chibiProfile: ChibiProfile,
  modifier: Modifier = Modifier,
) {
  var showSeasonsMode by remember { mutableStateOf(false) }

  // Orbit race state
  var raceRunning by remember { mutableStateOf(false) }
  var earthYearsElapsed by remember { mutableFloatStateOf(0f) }
  var raceSpeed by remember { mutableFloatStateOf(1.5f) }

  // Seasons state (0 = Dicembre/Inverno IT, 90 = Primavera, 180 = Giugno/Estate IT, 270 = Autunno)
  var seasonOrbitDeg by remember { mutableFloatStateOf(180f) }
  var useUranusTilt by remember { mutableStateOf(false) }

  val racePlanets = remember {
    listOf("mercury", "venus", "earth", "mars", "jupiter", "neptune").map { SolarSystemData.getPlanetById(it) }
  }

  LaunchedEffect(raceRunning, showSeasonsMode) {
    if (!raceRunning || showSeasonsMode) return@LaunchedEffect
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          earthYearsElapsed += dt * 0.35f * raceSpeed
        }
        lastNanos = nanos
      }
    }
  }

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.5.dp, NebulaMagenta.copy(alpha = 0.65f)),
    modifier = modifier.fillMaxWidth().testTag("lab_panel_orbits"),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Public, contentDescription = null, tint = NebulaMagenta)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Orbite dei Pianeti & Stagioni",
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = !showSeasonsMode,
          onClick = { showSeasonsMode = false },
          label = { Text("Corsa delle Orbite") },
          colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NebulaMagenta, selectedLabelColor = StarlightWhite),
          modifier = Modifier.testTag("lab_orbits_tab_race"),
        )
        FilterChip(
          selected = showSeasonsMode,
          onClick = { showSeasonsMode = true },
          label = { Text("Perché le Stagioni?") },
          colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicCyan, selectedLabelColor = ComicInk),
          modifier = Modifier.testTag("lab_orbits_tab_seasons"),
        )
      }

      val textMeasurer = rememberTextMeasurer()

      if (!showSeasonsMode) {
        // PLANETARY ORBIT RACE
        Text(
          text = "Fai partire tutti i pianeti dalla stessa linea: scopri quanto corrono veloci quelli vicini al Sole rispetto ai giganti lontani!",
          style = MaterialTheme.typography.bodyMedium,
          color = StarlightMuted,
        )

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(225.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(SpaceVoid),
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(listOf(Color(0xFF1A163A), Color(0xFF050816))))
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            val maxR = size.minDimension * 0.44f

            // Starting line to the right
            drawLine(
              color = CosmicAmber.copy(alpha = 0.55f),
              start = center,
              end = Offset(center.x + maxR + 8f, center.y),
              strokeWidth = 2f,
            )

            // Sun
            drawCircle(color = CosmicAmber, radius = 14f, center = center)

            racePlanets.forEachIndexed { idx, p ->
              val orbitR = 24f + ((maxR - 24f) / (racePlanets.size - 1)) * idx
              drawCircle(
                color = p.primaryColor.copy(alpha = 0.28f),
                radius = orbitR,
                center = center,
                style = Stroke(width = 1.5f),
              )
              val laps = earthYearsElapsed / p.yearLengthEarthYears
              val angleRad = (laps * 2.0 * PI)
              val px = center.x + (cos(angleRad) * orbitR).toFloat()
              val py = center.y - (sin(angleRad) * orbitR).toFloat()
              drawCircle(color = p.primaryColor, radius = if (p.id == "jupiter") 8.5f else 6f, center = Offset(px, py))
            }
          }
        }

        // Controls & Lap counters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = { raceRunning = !raceRunning },
            colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).testTag("lab_orbits_play_button"),
          ) {
            Icon(if (raceRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (raceRunning) "Pausa" else "Via alla corsa!")
          }
          OutlinedButton(
            onClick = {
              raceRunning = false
              earthYearsElapsed = 0f
            },
            shape = RoundedCornerShape(14.dp),
          ) {
            Icon(Icons.Rounded.Replay, contentDescription = "Ricomincia", tint = StarlightWhite)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(racePlanets[0], racePlanets[2], racePlanets[4], racePlanets[5]).forEach { p ->
            val laps = earthYearsElapsed / p.yearLengthEarthYears
            Surface(
              color = SpaceNavy,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f),
            ) {
              Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(p.name, style = MaterialTheme.typography.labelSmall, color = p.accentColor)
                Text(
                  text = String.format(Locale.ITALIAN, "%.1f giri", laps),
                  style = MaterialTheme.typography.labelMedium,
                  color = StarlightWhite,
                )
              }
            }
          }
        }

        ChibiComicBubble(
          profile = chibiProfile,
          expression = ChibiExpression.CHEERING,
          dialogue = "Mentre la Terra fa 1 giro attorno al Sole, Mercurio ne fa già più di 4! Nettuno invece impiega 165 anni terrestri per un solo giro!",
          avatarSize = 62.dp,
        )
      } else {
        // SEASONS & AXIAL TILT
        val tiltDeg = if (useUranusTilt) 97.8f else 23.4f
        val monthLabel =
          when {
            seasonOrbitDeg < 45f || seasonOrbitDeg >= 315f -> "Dicembre • Inverno in Italia (Polo Nord al buio!)"
            seasonOrbitDeg < 135f -> "Marzo • Primavera (Equinozio!)"
            seasonOrbitDeg < 225f -> "Giugno • Estate in Italia (Raggi dritti al Nord!)"
            else -> "Settembre • Autunno (Equinozio!)"
          }

        Text(
          text = "Non è la distanza dal Sole a fare le stagioni, ma l'inclinazione dell'asse! Muovi il pianeta lungo i mesi dell'anno.",
          style = MaterialTheme.typography.bodyMedium,
          color = StarlightMuted,
        )

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(190.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(SpaceVoid),
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.horizontalGradient(listOf(Color(0xFF2B1A03), Color(0xFF070C1F))))
            val sunCenter = Offset(46f, size.height * 0.5f)
            drawCircle(
              brush = Brush.radialGradient(listOf(CosmicAmber, Color(0xFFFF6B00), Color.Transparent)),
              radius = 48f,
              center = sunCenter,
            )

            // Sun rays hitting planet on the right
            val planetCenter = Offset(size.width * 0.68f, size.height * 0.5f)
            val pr = 46f
            for (i in -2..2) {
              val ry = planetCenter.y + i * 16f
              drawLine(
                color = CosmicAmber.copy(alpha = 0.5f),
                start = Offset(98f, ry),
                end = Offset(planetCenter.x - pr - 8f, ry),
                strokeWidth = 2.5f,
              )
            }

            // Effective tilt toward/away from Sun varies as cos(seasonOrbitDeg)
            val facingTiltRad = Math.toRadians(tiltDeg.toDouble()) * (-cos(Math.toRadians(seasonOrbitDeg.toDouble())))
            val axisDx = (sin(facingTiltRad) * (pr + 18f)).toFloat()
            val axisDy = (cos(facingTiltRad) * (pr + 18f)).toFloat()

            // Planet sphere
            val planetBase = if (useUranusTilt) Color(0xFF56CFE1) else Color(0xFF2B6CB0)
            drawCircle(color = planetBase, radius = pr, center = planetCenter)
            if (!useUranusTilt) {
              drawCircle(color = Color(0xFF48BB78), radius = 14f, center = Offset(planetCenter.x - 14f, planetCenter.y - 16f))
            }
            // Night side shade on right half
            val clip = Path().apply { addOval(androidx.compose.ui.geometry.Rect(planetCenter.x - pr, planetCenter.y - pr, planetCenter.x + pr, planetCenter.y + pr)) }
            clipPath(clip) {
              drawRect(
                color = Color.Black.copy(alpha = 0.55f),
                topLeft = Offset(planetCenter.x, planetCenter.y - pr),
                size = Size(pr, pr * 2f),
              )
            }

            // Equator & Axis rod
            drawLine(
              color = CosmicAmber,
              start = Offset(planetCenter.x - axisDx, planetCenter.y - axisDy),
              end = Offset(planetCenter.x + axisDx, planetCenter.y + axisDy),
              strokeWidth = 4f,
              cap = StrokeCap.Round,
            )
            val northLbl = textMeasurer.measure("N", TextStyle(fontFamily = FredokaFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicAmber))
            drawText(northLbl, topLeft = Offset(planetCenter.x - axisDx - northLbl.size.width / 2f, planetCenter.y - axisDy - 18f))
          }
        }

        Surface(
          color = SpaceCardElevated,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = if (useUranusTilt) "Urano (98°): un polo ha 42 anni di estate e 42 anni di buio!" else monthLabel,
            style = MaterialTheme.typography.labelLarge,
            color = CosmicCyan,
            modifier = Modifier.padding(10.dp),
          )
        }

        Slider(
          value = seasonOrbitDeg,
          onValueChange = { seasonOrbitDeg = it },
          valueRange = 0f..360f,
          colors = SliderDefaults.colors(thumbColor = CosmicCyan, activeTrackColor = CosmicCyan),
        )

        FilterChip(
          selected = useUranusTilt,
          onClick = { useUranusTilt = !useUranusTilt },
          label = { Text(if (useUranusTilt) "Urano sdraiato (98°) — Torna alla Terra" else "Prova l'asse sdraiato di Urano (98°)!") },
          colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicCyan, selectedLabelColor = ComicInk),
        )

        ChibiComicBubble(
          profile = chibiProfile,
          expression = ChibiExpression.THINKING,
          dialogue =
            if (useUranusTilt) "Su Urano l'asse è così inclinato che rotola come una palla! Avresti 42 anni di sole continuo seguiti da 42 anni di notte polare!"
            else "Quando il Polo Nord è inclinato verso il Sole, i raggi arrivano belli dritti e fa caldo (Estate)! Sei mesi dopo è inclinato via dal Sole ed è Inverno!",
          avatarSize = 62.dp,
        )
      }
    }
  }
}
