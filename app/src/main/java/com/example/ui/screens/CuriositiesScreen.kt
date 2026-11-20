package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SolarSystemData
import com.example.model.ChibiExpression
import com.example.model.ChibiProfile
import com.example.model.CuriosityCategory
import com.example.model.CuriosityItem
import com.example.ui.components.ChibiAvatar
import com.example.ui.components.ChibiComicBubble
import com.example.ui.theme.AuroraGreen
import com.example.ui.theme.ComicInk
import com.example.ui.theme.CosmicAmber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.NebulaMagenta
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardElevated
import com.example.ui.theme.SpaceNavy
import com.example.ui.theme.SpaceOutline
import com.example.ui.theme.SpaceVoid
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.StarlightWhite
import kotlin.math.roundToInt

@Composable
fun CuriositiesScreen(
  chibiProfile: ChibiProfile,
  userWeightKg: Float,
  userAgeYears: Float,
  onUserWeightChange: (Float) -> Unit,
  onUserAgeChange: (Float) -> Unit,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenLabs: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  var selectedCategory by remember { mutableStateOf(CuriosityCategory.ALL) }
  var expandedIds by remember { mutableStateOf(setOf("cur_1")) }

  val filteredCuriosities =
    remember(selectedCategory) {
      if (selectedCategory == CuriosityCategory.ALL) {
        SolarSystemData.curiosities
      } else {
        SolarSystemData.curiosities.filter { it.category == selectedCategory }
      }
    }

  Box(
    modifier = modifier.fillMaxSize().background(SpaceVoid),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 680.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Nebula Comic Hero Header
      item {
        Surface(
          shape = RoundedCornerShape(24.dp),
          border = BorderStroke(2.dp, NebulaMagenta.copy(alpha = 0.65f)),
          color = SpaceNavy,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
              drawRect(
                brush =
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF191B4B), Color(0xFF3A144D), Color(0xFF0E2954)),
                  ),
              )
              drawCircle(
                brush = Brush.radialGradient(listOf(NebulaMagenta.copy(alpha = 0.35f), Color.Transparent)),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.85f, size.height * 0.25f),
              )
              drawCircle(
                brush = Brush.radialGradient(listOf(CosmicCyan.copy(alpha = 0.28f), Color.Transparent)),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.15f, size.height * 0.8f),
              )
              // Shooting star streak
              drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(size.width * 0.62f, size.height * 0.15f),
                end = Offset(size.width * 0.88f, size.height * 0.30f),
                strokeWidth = 3f,
              )
            }

            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              ChibiAvatar(
                profile = chibiProfile,
                expression = ChibiExpression.AMAZED,
                size = 86.dp,
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Surface(
                  color = CosmicAmber,
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.5.dp, ComicInk),
                ) {
                  Text(
                    text = "ANGOLO DELLE MERAVIGLIE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ComicInk,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Curiosità Astronomiche",
                  style = MaterialTheme.typography.headlineSmall,
                  color = StarlightWhite,
                )
                Text(
                  text = "Segreti pazzeschi dello spazio scovati da ${chibiProfile.name}!",
                  style = MaterialTheme.typography.bodyMedium,
                  color = StarlightMuted,
                )
              }
            }
          }
        }
      }

      // Interactive Space Weight & Age Lab
      item {
        SpaceWeightAndAgeLab(
          weightKg = userWeightKg,
          ageYears = userAgeYears,
          onWeightChange = onUserWeightChange,
          onAgeChange = onUserAgeChange,
          onOpenLabs = onOpenLabs,
        )
      }

      // Category Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          CuriosityCategory.entries.forEach { cat ->
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(cat.label) },
              colors =
                FilterChipDefaults.filterChipColors(
                  containerColor = SpaceCard,
                  labelColor = StarlightWhite,
                  selectedContainerColor = CosmicCyan,
                  selectedLabelColor = ComicInk,
                ),
              modifier = Modifier.testTag("curiosity_filter_${cat.name}"),
            )
          }
        }
      }

      // Curiosity Comic Cards
      items(filteredCuriosities, key = { it.id }) { item ->
        val isExpanded = expandedIds.contains(item.id)
        CuriosityComicCard(
          item = item,
          chibiProfile = chibiProfile,
          isExpanded = isExpanded,
          onToggleExpand = {
            expandedIds =
              if (isExpanded) expandedIds - item.id else expandedIds + item.id
          },
          onOpenPlanetDetail = onOpenPlanetDetail,
        )
      }

      item { Spacer(modifier = Modifier.height(12.dp)) }
    }
  }
}

@Composable
fun SpaceWeightAndAgeLab(
  weightKg: Float,
  ageYears: Float,
  onWeightChange: (Float) -> Unit,
  onAgeChange: (Float) -> Unit,
  onOpenLabs: (() -> Unit)? = null,
) {
  val samplePlanetIds = listOf("mercury", "mars", "jupiter", "saturn", "pluto")
  val planets = samplePlanetIds.map { SolarSystemData.getPlanetById(it) }

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Science, contentDescription = null, tint = CosmicCyan)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Laboratorio: Peso & Età nello Spazio",
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Regola i tuoi dati terrestri e scopri cosa succede sugli altri mondi!",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Il tuo peso sulla Terra: ${weightKg.roundToInt()} kg",
        style = MaterialTheme.typography.labelLarge,
        color = CosmicAmber,
      )
      Slider(
        value = weightKg,
        onValueChange = onWeightChange,
        valueRange = 20f..85f,
        colors = SliderDefaults.colors(thumbColor = CosmicAmber, activeTrackColor = CosmicAmber),
        modifier = Modifier.testTag("curiosity_weight_slider"),
      )

      Text(
        text = "La tua età sulla Terra: ${ageYears.roundToInt()} anni",
        style = MaterialTheme.typography.labelLarge,
        color = AuroraGreen,
      )
      Slider(
        value = ageYears,
        onValueChange = onAgeChange,
        valueRange = 6f..60f,
        colors = SliderDefaults.colors(thumbColor = AuroraGreen, activeTrackColor = AuroraGreen),
        modifier = Modifier.testTag("curiosity_age_slider"),
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        planets.forEach { planet ->
          val weightHere = (weightKg * planet.gravityRatio).roundToInt()
          val ageHere = ageYears / planet.yearLengthEarthYears
          val formattedAge =
            if (ageHere >= 10f) "${ageHere.roundToInt()} anni"
            else String.format(java.util.Locale.ITALIAN, "%.1f anni", ageHere)
          Surface(
            color = SpaceNavy,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, planet.primaryColor.copy(alpha = 0.6f)),
            modifier = Modifier.width(126.dp),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = planet.name,
                style = MaterialTheme.typography.labelLarge,
                color = planet.accentColor,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Peso: $weightHere kg",
                style = MaterialTheme.typography.labelMedium,
                color = StarlightWhite,
              )
              Text(
                text = "Età: $formattedAge",
                style = MaterialTheme.typography.labelSmall,
                color = StarlightMuted,
              )
            }
          }
        }
      }

      if (onOpenLabs != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
          onClick = onOpenLabs,
          colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("curiosity_open_all_labs_button"),
        ) {
          Icon(Icons.Rounded.Science, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Apri tutti i 7 Laboratori Spaziali!", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}

@Composable
private fun CuriosityComicCard(
  item: CuriosityItem,
  chibiProfile: ChibiProfile,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onOpenPlanetDetail: (String) -> Unit,
) {
  Surface(
    onClick = onToggleExpand,
    color = SpaceCard,
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(1.5.dp, if (isExpanded) item.accentColor else SpaceOutline),
    modifier = Modifier.fillMaxWidth().testTag("curiosity_card_${item.id}"),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = item.accentColor,
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.5.dp, ComicInk),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = ComicInk, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = item.badgeTag,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = ComicInk,
            )
          }
        }
        Icon(
          imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
          contentDescription = if (isExpanded) "Chiudi" else "Espandi",
          tint = StarlightMuted,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = item.title,
        style = MaterialTheme.typography.titleLarge,
        color = StarlightWhite,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = item.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = StarlightMuted,
      )

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
          Surface(
            color = SpaceCardElevated,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = item.deepDive,
              style = MaterialTheme.typography.bodyMedium,
              color = StarlightWhite,
              modifier = Modifier.padding(12.dp),
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          ChibiComicBubble(
            profile = chibiProfile,
            expression = ChibiExpression.AMAZED,
            dialogue = item.chibiReaction,
            avatarSize = 66.dp,
            footerContent =
              if (item.relatedPlanetId != null) {
                {
                  val planet = SolarSystemData.getPlanetById(item.relatedPlanetId)
                  Button(
                    onClick = { onOpenPlanetDetail(planet.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  ) {
                    Icon(Icons.Rounded.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Vedi ${planet.name} in 3D", style = MaterialTheme.typography.labelLarge)
                  }
                }
              } else {
                null
              },
          )
        }
      }
    }
  }
}
