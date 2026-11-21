package com.ricrug.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FlightLand
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricrug.data.SolarSystemData
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.components.ChibiComicBubble
import com.ricrug.ui.components.Interactive3DPlanetCanvas
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceCard
import com.ricrug.ui.theme.SpaceCardElevated
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.SpaceOutline
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetDetailScreen(
  planetId: String,
  chibiProfile: ChibiProfile,
  userWeightKg: Float,
  onUserWeightChange: (Float) -> Unit,
  onMarkExplored: (String) -> Unit,
  onBack: () -> Unit,
  onStartPlanetQuiz: (String) -> Unit,
  onOpenLandingSimulator: (String) -> Unit,
  onOpenSurfaceExploration: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val body = SolarSystemData.getPlanetById(planetId)

  LaunchedEffect(planetId) { onMarkExplored(planetId) }

  var autoSpin by remember(planetId) { mutableStateOf(true) }
  var showAxialTilt by remember(planetId) { mutableStateOf(false) }
  var compareActive by remember(planetId) { mutableStateOf(false) }
  var compareTargetId by remember(planetId) {
    mutableStateOf(if (planetId == "earth") "moon" else "earth")
  }
  val compareTargetBody = remember(compareTargetId) { SolarSystemData.getPlanetById(compareTargetId) }
  var zoom by remember(planetId) { mutableFloatStateOf(1f) }
  var storyIndex by remember(planetId) { mutableIntStateOf(0) }

  Column(
    modifier = modifier.fillMaxSize().background(SpaceVoid),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    TopAppBar(
      title = {
        Column {
          Text(body.name, style = MaterialTheme.typography.titleLarge, color = StarlightWhite)
          Text(body.nickname, style = MaterialTheme.typography.labelMedium, color = CosmicAmber)
        }
      },
      navigationIcon = {
        IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Torna indietro",
            tint = StarlightWhite,
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = SpaceNavy),
    )

    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 680.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Interactive 3D Planet Lab Card
      Surface(
        color = SpaceNavy,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, body.primaryColor.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column {
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .height(275.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
          ) {
            Interactive3DPlanetCanvas(
              body = body,
              autoSpin = autoSpin,
              showAxialTilt = showAxialTilt,
              compareWithPlanetId = if (compareActive) compareTargetId else null,
              externalZoom = zoom,
              onZoomChange = { zoom = it },
              modifier = Modifier.fillMaxSize().testTag("planet_3d_canvas"),
            )

            // Top-left 3D Hint Pill
            Surface(
              color = SpaceVoid.copy(alpha = 0.72f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            ) {
              Text(
                text = "Trascina in 3D • Pizzica per zoom",
                style = MaterialTheme.typography.labelSmall,
                color = StarlightMuted,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }

            // Zoom +/- Controls on top-right
            Row(
              modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Surface(
                onClick = { zoom = (zoom - 0.2f).coerceIn(0.65f, 1.85f) },
                color = SpaceCard.copy(alpha = 0.85f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Rounded.Remove, contentDescription = "Rimpicciolisci", tint = StarlightWhite, modifier = Modifier.size(18.dp))
                }
              }
              Surface(
                onClick = { zoom = (zoom + 0.2f).coerceIn(0.65f, 1.85f) },
                color = SpaceCard.copy(alpha = 0.85f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Rounded.Add, contentDescription = "Ingrandisci", tint = StarlightWhite, modifier = Modifier.size(18.dp))
                }
              }
            }
          }

          // 3D Lab Controls Row
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FilterChip(
              selected = autoSpin,
              onClick = { autoSpin = !autoSpin },
              label = { Text("Gira 3D") },
              leadingIcon = { Icon(Icons.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp)) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicAmber, selectedLabelColor = ComicInk),
            )
            FilterChip(
              selected = showAxialTilt,
              onClick = { showAxialTilt = !showAxialTilt },
              label = { Text("Asse ${body.axialTiltDeg.roundToInt()}°") },
              leadingIcon = { Icon(Icons.Rounded.ScreenRotation, contentDescription = null, modifier = Modifier.size(16.dp)) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicCyan, selectedLabelColor = ComicInk),
            )
            FilterChip(
              selected = compareActive,
              onClick = { compareActive = !compareActive },
              label = { Text(if (body.id == "earth") "Confronta" else "vs Terra") },
              leadingIcon = { Icon(Icons.Rounded.Compare, contentDescription = null, modifier = Modifier.size(16.dp)) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AuroraGreen, selectedLabelColor = ComicInk),
              modifier = Modifier.testTag("compare_scale_chip"),
            )
          }

          AnimatedVisibility(visible = compareActive) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              val ratioSummary =
                remember(body.id, compareTargetBody.id) {
                  val dMain = body.diameterKm.toFloat()
                  val dComp = compareTargetBody.diameterKm.toFloat()
                  if (dMain >= dComp) {
                    val f = dMain / dComp
                    val fmt = if (f >= 10f) "${f.roundToInt()}" else String.format(java.util.Locale.ITALIAN, "%.1f", f)
                    "${body.name} ha un diametro ${fmt}x più grande di ${compareTargetBody.name}"
                  } else {
                    val f = dComp / dMain
                    val fmt = if (f >= 10f) "${f.roundToInt()}" else String.format(java.util.Locale.ITALIAN, "%.1f", f)
                    "${body.name} è ${fmt}x più piccolo di ${compareTargetBody.name}"
                  }
                }
              Surface(
                color = SpaceCardElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AuroraGreen.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth().testTag("compare_scale_summary"),
              ) {
                Text(
                  text = "Scala reale: $ratioSummary",
                  style = MaterialTheme.typography.labelLarge,
                  color = AuroraGreen,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
              }

              val candidateTargets =
                remember(body.id) {
                  val others = SolarSystemData.celestialBodies.filter { it.id != body.id }
                  val earthFirst = others.filter { it.id == "earth" } + others.filter { it.id != "earth" }
                  earthFirst
                }
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Confronta con:",
                  style = MaterialTheme.typography.labelMedium,
                  color = StarlightMuted,
                )
                candidateTargets.forEach { target ->
                  FilterChip(
                    selected = compareTargetId == target.id,
                    onClick = { compareTargetId = target.id },
                    label = { Text(target.name) },
                    colors =
                      FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CosmicCyan,
                        selectedLabelColor = ComicInk,
                      ),
                    modifier = Modifier.testTag("compare_target_${target.id}"),
                  )
                }
              }
            }
          }
        }
      }

      // Size Analogy Strip for 10-year-olds
      Surface(
        color = SpaceCardElevated,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, CosmicAmber.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "Quanto è grande? ${body.sizeAnalogy}",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = StarlightWhite,
          modifier = Modifier.padding(14.dp),
        )
      }

      // Interactive Comic-Strip Story Chapters with Chibi Character
      val stories = body.comicStories
      val currentStory = stories.getOrElse(storyIndex) { stories.first() }
      ChibiComicBubble(
        profile = chibiProfile,
        expression = currentStory.expression,
        badgeText = "${currentStory.badge} (${storyIndex + 1}/${stories.size})",
        headline = currentStory.title,
        dialogue = currentStory.dialogue,
        avatarSize = 88.dp,
        footerContent = {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedButton(
              onClick = { if (storyIndex > 0) storyIndex-- },
              enabled = storyIndex > 0,
              border = BorderStroke(1.5.dp, if (storyIndex > 0) ComicInk else Color.Gray),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("story_prev_button"),
            ) {
              Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = ComicInk)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Indietro", color = ComicInk, style = MaterialTheme.typography.labelLarge)
            }

            // Chapter dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              stories.indices.forEach { idx ->
                Box(
                  modifier =
                    Modifier.size(if (idx == storyIndex) 10.dp else 7.dp)
                      .clip(CircleShape)
                      .background(if (idx == storyIndex) NebulaMagenta else Color.Gray.copy(alpha = 0.45f)),
                )
              }
            }

            Button(
              onClick = {
                storyIndex = if (storyIndex + 1 < stories.size) storyIndex + 1 else 0
              },
              colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              modifier = Modifier.testTag("story_next_button"),
            ) {
              Text(
                text = if (storyIndex + 1 < stories.size) "Avanti" else "Rileggi",
                style = MaterialTheme.typography.labelLarge,
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = if (storyIndex + 1 < stories.size) Icons.AutoMirrored.Rounded.ArrowForward else Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        },
      )

      // Planetary ID Card (Carta d'Identità Spaziale)
      Surface(
        color = SpaceCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, SpaceOutline),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Carta d'Identità Spaziale",
            style = MaterialTheme.typography.titleLarge,
            color = CosmicCyan,
          )
          StatGridRow("Distanza dal Sole", body.distanceFromSunLabel, "Diametro", "${body.diameterKm} km")
          StatGridRow("Durata del Giorno", body.dayLengthLabel, "Durata dell'Anno", body.yearLengthLabel)
          StatGridRow("Temperatura", body.temperatureLabel, "Lune", "${body.moonsCount}")
          if (body.notableMoons.isNotEmpty()) {
            Text(
              text = "Lune famose: ${body.notableMoons.joinToString(", ")}",
              style = MaterialTheme.typography.bodyMedium,
              color = CosmicAmber,
            )
          }
        }
      }

      // Interactive Mini Gravity Calculator on this Planet
      Surface(
        color = SpaceCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, NebulaMagenta.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Quanto peseresti qui?",
            style = MaterialTheme.typography.titleLarge,
            color = NebulaMagenta,
          )
          Spacer(modifier = Modifier.height(4.dp))
          val planetWeight = (userWeightKg * body.gravityRatio)
          Text(
            text = "Se sulla Terra pesi ${userWeightKg.roundToInt()} kg, su ${body.name} la bilancia segnerebbe ${planetWeight.roundToInt()} kg!",
            style = MaterialTheme.typography.bodyLarge,
            color = StarlightWhite,
          )
          Slider(
            value = userWeightKg,
            onValueChange = onUserWeightChange,
            valueRange = 20f..85f,
            colors =
              SliderDefaults.colors(
                thumbColor = CosmicAmber,
                activeTrackColor = NebulaMagenta,
              ),
          )
          Text(
            text =
              when {
                body.gravityRatio < 0.5f -> "Gravità leggerissima! Potresti fare salti altissimi!"
                body.gravityRatio > 1.8f -> "Gravità fortissima! Ti sentiresti pesantissimo!"
                else -> "Una gravità abbastanza vicina a quella della Terra!"
              },
            style = MaterialTheme.typography.bodyMedium,
            color = StarlightMuted,
          )
        }
      }

      // Landing Simulator, Surface Exploration & Planet Quiz CTAs
      if (body.id != "sun" && body.id != "earth") {
        Button(
          onClick = { onOpenSurfaceExploration(body.id) },
          colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag("planet_surface_explore_cta"),
        ) {
          Icon(Icons.Rounded.Explore, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "🥾 Sbarca ed Esplora ${body.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
          )
        }

        Button(
          onClick = { onOpenLandingSimulator(body.id) },
          colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag("planet_landing_cta"),
        ) {
          Icon(Icons.Rounded.FlightLand, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Simula Atterraggio su ${body.name}",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            softWrap = false,
          )
        }
      } else if (body.id == "earth") {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = SpaceCard,
          border = BorderStroke(1.2.dp, CosmicCyan.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("🌍", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "Pianeta Casa & Spazioporto",
                style = MaterialTheme.typography.titleMedium,
                color = CosmicCyan,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "La Terra è il nostro punto di partenza per l'esplorazione del cosmo. Per effettuare sbarchi o simulare atterraggi, seleziona la Luna, Marte o gli altri pianeti!",
                style = MaterialTheme.typography.bodySmall,
                color = StarlightMuted,
              )
            }
          }
        }
      }

      Button(
        onClick = { onStartPlanetQuiz(body.id) },
        colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("planet_quiz_cta"),
      ) {
        Icon(Icons.Rounded.Quiz, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Fai il Quiz su ${body.name}",
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          softWrap = false,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun StatGridRow(
  label1: String,
  value1: String,
  label2: String,
  value2: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    StatBox(label = label1, value = value1, modifier = Modifier.weight(1f))
    StatBox(label = label2, value = value2, modifier = Modifier.weight(1f))
  }
}

@Composable
private fun StatBox(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = SpaceNavy,
    shape = RoundedCornerShape(12.dp),
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = StarlightMuted)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = value, style = MaterialTheme.typography.labelMedium, color = StarlightWhite)
    }
  }
}
