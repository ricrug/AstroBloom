package com.example.ui.screens.labs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Water
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.sin

private data class DensityWorld(
  val id: String,
  val name: String,
  val densityGcm3: Float,
  val color: Color,
  val hasRings: Boolean = false,
  val note: String,
)

private val densityWorlds =
  listOf(
    DensityWorld("saturn", "Saturno", 0.69f, Color(0xFFF4D58D), hasRings = true, note = "Galleggia! È fatto soprattutto di gas leggerissimi (idrogeno ed elio) ed è meno denso dell'acqua!"),
    DensityWorld("uranus", "Urano", 1.27f, Color(0xFF56CFE1), note = "Affonda piano! È un gigante di ghiaccio appena più denso dell'acqua."),
    DensityWorld("jupiter", "Giove", 1.33f, Color(0xFFE9C46A), note = "Affonda! Anche se è gassoso, la sua enorme gravità schiaccia il nucleo rendendolo più denso dell'acqua."),
    DensityWorld("pluto", "Plutone", 1.86f, Color(0xFFD4A373), note = "Affonda! È fatto per metà di ghiaccio e per metà di roccia."),
    DensityWorld("moon", "La Luna", 3.34f, Color(0xFFD6DCE8), note = "Affonda veloce! È un mondo roccioso fatto di silicati."),
    DensityWorld("mars", "Marte", 3.93f, Color(0xFFE76F51), note = "Affonda subito come un sasso: è fatto di roccia e ferro!"),
    DensityWorld("earth", "La Terra", 5.51f, Color(0xFF2B6CB0), note = "Il pianeta più denso del Sistema Solare! Ha un grande nucleo pesante di ferro e nichel."),
  )

@Composable
fun CosmicBathtubLab(
  chibiProfile: ChibiProfile,
  modifier: Modifier = Modifier,
) {
  var selectedIdx by remember { mutableIntStateOf(6) } // Default Earth so switching to Saturn is an awesome surprise!
  var wavePhase by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(Unit) {
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          wavePhase = (wavePhase + dt * 2.8f) % (2f * PI.toFloat())
        }
        lastNanos = nanos
      }
    }
  }

  val world = densityWorlds[selectedIdx]
  val floats = world.densityGcm3 < 1.0f
  // Normalized vertical depth: 0.30 = floating at water line; 0.55..0.82 = sunk to bottom
  val targetDepthFrac =
    if (floats) 0.34f else (0.56f + ((world.densityGcm3 - 1f) / 4.6f) * 0.24f).coerceIn(0.56f, 0.82f)
  val animatedDepth by animateFloatAsState(
    targetValue = targetDepthFrac,
    animationSpec = spring(dampingRatio = 0.55f, stiffness = 220f),
    label = "bathtubDrop",
  )

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.65f)),
    modifier = modifier.fillMaxWidth().testTag("lab_panel_bathtub"),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Water, contentDescription = null, tint = CosmicCyan)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "La Vasca Cosmica: Chi Galleggia?",
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
      }

      Text(
        text = "L'acqua ha densità 1,00 g/cm³. Tuffa un pianeta nella piscina spaziale per scoprire quale pianeta galleggerebbe!",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )

      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        densityWorlds.forEachIndexed { idx, w ->
          FilterChip(
            selected = selectedIdx == idx,
            onClick = { selectedIdx = idx },
            label = { Text(w.name) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicCyan, selectedLabelColor = ComicInk),
            modifier = Modifier.testTag("lab_bathtub_planet_${w.id}"),
          )
        }
      }

      // Cosmic Bathtub Canvas
      val textMeasurer = rememberTextMeasurer()
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SpaceVoid),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawRect(Brush.verticalGradient(listOf(Color(0xFF0A1128), Color(0xFF0E1B3A))))

          val waterTop = size.height * 0.34f
          val bobOffset = if (floats) sin(wavePhase) * 5f else 0f
          val planetCenter = Offset(size.width * 0.5f, size.height * animatedDepth + bobOffset)
          val pr = 34f

          // Draw Saturn back rings or planet sphere behind water tint
          if (world.hasRings) {
            drawOval(
              color = Color(0xFFE6C88A).copy(alpha = 0.75f),
              topLeft = Offset(planetCenter.x - pr * 1.9f, planetCenter.y - pr * 0.32f),
              size = Size(pr * 3.8f, pr * 0.64f),
              style = Stroke(width = 7f),
            )
          }
          drawCircle(color = world.color, radius = pr, center = planetCenter)

          // Animated wavy water fill
          val waterPath = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, waterTop)
            val steps = 28
            for (i in 0..steps) {
              val x = (size.width / steps) * i
              val y = waterTop + sin(wavePhase + i * 0.45f) * 4.5f
              lineTo(x, y)
            }
            lineTo(size.width, size.height)
            close()
          }
          drawPath(
            path = waterPath,
            brush = Brush.verticalGradient(listOf(CosmicCyan.copy(alpha = 0.38f), Color(0xFF0B4F8A).copy(alpha = 0.68f))),
          )
          drawLine(
            color = Color.White.copy(alpha = 0.55f),
            start = Offset(0f, waterTop),
            end = Offset(size.width, waterTop),
            strokeWidth = 2f,
          )

          // Pool floor
          drawRect(
            color = SpaceNavy,
            topLeft = Offset(0f, size.height - 14f),
            size = Size(size.width, 14f),
          )

          val badgeTxt = if (floats) "GALLEGGIA!" else "AFFONDA!"
          val badgeCol = if (floats) AuroraGreen else CosmicAmber
          val meas =
            textMeasurer.measure(
              badgeTxt,
              TextStyle(fontFamily = FredokaFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = badgeCol),
            )
          drawText(meas, topLeft = Offset(14f, 12f))

          val dText =
            textMeasurer.measure(
              "Densità: ${String.format(Locale.ITALIAN, "%.2f", world.densityGcm3)} g/cm³",
              TextStyle(fontFamily = SpaceMonoFontFamily, fontSize = 11.sp, color = StarlightWhite),
            )
          drawText(dText, topLeft = Offset(size.width - dText.size.width - 14f, 14f))
        }
      }

      ChibiComicBubble(
        profile = chibiProfile,
        expression = if (floats) ChibiExpression.AMAZED else ChibiExpression.THINKING,
        dialogue = world.note,
        avatarSize = 62.dp,
      )
    }
  }
}

private data class WeatherWorld(
  val id: String,
  val name: String,
  val tempC: Int,
  val benchmark: String,
  val weatherTitle: String,
  val weatherNote: String,
  val skyColors: List<Color>,
  val particleColor: Color,
  val isHot: Boolean,
)

private val weatherWorlds =
  listOf(
    WeatherWorld("pluto", "Plutone", -229, "Molto più freddo dell'azoto liquido!", "Neve di Azoto Ghiacciato", "Su Plutone fa così freddo che persino l'aria gela e cade al suolo come neve brillante!", listOf(Color(0xFF0A142E), Color(0xFF1A294B)), Color(0xFFE0FBFC), false),
    WeatherWorld("neptune", "Nettuno", -201, "11 volte più freddo del freezer di casa!", "Pioggia di Diamanti & Venti a 2.000 km/h", "Nelle profondità di Nettuno la pressione schiaccia il carbonio creando veri diamanti che piovono giù!", listOf(Color(0xFF0A1C4E), Color(0xFF1D4E89)), Color(0xFF90E0EF), false),
    WeatherWorld("mars", "Marte", -63, "Come una notte al Polo Sud!", "Tempeste di Polvere Ruggine", "Venti leggeri sollevano polvere rossa che può coprire tutto il pianeta per settimane!", listOf(Color(0xFF3E1C14), Color(0xFF7C3626)), Color(0xFFE76F51), false),
    WeatherWorld("earth", "La Terra", 15, "Perfetto per vivere e fare picnic!", "Nuvole d'Acqua & Arcobaleni", "L'unico mondo conosciuto dove l'acqua cade come pioggia liquida e crea mari azzurri!", listOf(Color(0xFF1D4E89), Color(0xFF56CFE1)), Color.White, false),
    WeatherWorld("mercury", "Mercurio", 167, "Più caldo di un'acqua che bolle (media giorno/notte)!", "Sole Gigante senza Aria", "Di giorno arriva a +430°C ma di notte scende a -180°C perché non ha atmosfera come coperta!", listOf(Color(0xFF3B2418), Color(0xFF6B4226)), Color(0xFFFFB703), true),
    WeatherWorld("venus", "Venere", 465, "Più caldo di un forno per la pizza (400°C)!", "Effetto Serra & Piogge Acide", "È il pianeta più bollente di tutti anche se è più lontano di Mercurio: le sue nubi spesse intrappolano il calore!", listOf(Color(0xFF5E1B08), Color(0xFFB54414)), Color(0xFFFF9E00), true),
    WeatherWorld("sun", "Il Sole", 5500, "55 volte più caldo dell'acqua bollente!", "Tempesta di Plasma Incandescente", "È una gigantesca stella di gas infuocato: la sua superficie brilla a 5.500°C!", listOf(Color(0xFF7B1E02), Color(0xFFFF8800)), Color(0xFFFFEA00), true),
  )

@Composable
fun SpaceThermometerLab(
  chibiProfile: ChibiProfile,
  modifier: Modifier = Modifier,
) {
  var selectedIdx by remember { mutableIntStateOf(5) } // Default Venus so kids discover why it's hotter than Mercury!
  var weatherPhase by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(Unit) {
    var lastNanos = 0L
    while (true) {
      withFrameNanos { nanos ->
        if (lastNanos != 0L) {
          val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
          weatherPhase = (weatherPhase + dt * 1.4f) % 100f
        }
        lastNanos = nanos
      }
    }
  }

  val w = weatherWorlds[selectedIdx]

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.5.dp, CosmicAmber.copy(alpha = 0.65f)),
    modifier = modifier.fillMaxWidth().testTag("lab_panel_weather"),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Thermostat, contentDescription = null, tint = CosmicAmber)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Termometro Spaziale & Meteo Estremo",
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
      }

      Text(
        text = "Scopri che tempo fa sugli altri mondi e confronta le temperature con cose che conosci!",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )

      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        weatherWorlds.forEachIndexed { idx, item ->
          FilterChip(
            selected = selectedIdx == idx,
            onClick = { selectedIdx = idx },
            label = { Text(item.name) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicAmber, selectedLabelColor = ComicInk),
            modifier = Modifier.testTag("lab_weather_world_${item.id}"),
          )
        }
      }

      // Animated Weather + Thermometer Card
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(175.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SpaceVoid),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawRect(Brush.verticalGradient(w.skyColors))

          // Animated falling snow/diamonds or rising heat embers
          for (i in 0..24) {
            val seedX = ((i * 53 + 17) % 100) / 100f
            val baseY = ((i * 79 + 29) % 100) / 100f
            val dir = if (w.isHot) -1f else 1f
            val py = (((baseY + dir * weatherPhase * (0.25f + (i % 3) * 0.1f)) % 1f + 1f) % 1f) * size.height
            val px = (seedX * size.width + sin(weatherPhase * 2f + i) * 10f)
            if (w.id == "neptune") {
              // Sparkly diamonds!
              val p = Path().apply {
                moveTo(px, py - 5f)
                lineTo(px + 4f, py)
                lineTo(px, py + 5f)
                lineTo(px - 4f, py)
                close()
              }
              drawPath(p, color = w.particleColor.copy(alpha = 0.85f))
            } else {
              drawCircle(color = w.particleColor.copy(alpha = 0.72f), radius = 3.5f + (i % 3), center = Offset(px, py))
            }
          }
        }

        Column(
          modifier = Modifier.fillMaxSize().padding(14.dp),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              color = SpaceVoid.copy(alpha = 0.78f),
              shape = RoundedCornerShape(12.dp),
            ) {
              Text(
                text = w.weatherTitle,
                style = MaterialTheme.typography.labelLarge,
                color = StarlightWhite,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
            Surface(
              color = if (w.isHot) Color(0xFFE63946) else CosmicCyan,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.5.dp, ComicInk),
            ) {
              Text(
                text = "${if (w.tempC > 0) "+" else ""}${w.tempC}°C",
                style = MaterialTheme.typography.titleMedium,
                color = if (w.isHot) StarlightWhite else ComicInk,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }

          Surface(
            color = SpaceVoid.copy(alpha = 0.82f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "Confronto: ${w.benchmark}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = CosmicAmber,
              modifier = Modifier.padding(10.dp),
            )
          }
        }
      }

      ChibiComicBubble(
        profile = chibiProfile,
        expression = if (w.isHot) ChibiExpression.AMAZED else ChibiExpression.THINKING,
        dialogue = w.weatherNote,
        avatarSize = 62.dp,
      )
    }
  }
}
