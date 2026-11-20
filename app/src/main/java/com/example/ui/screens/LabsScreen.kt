package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brightness3
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ChibiExpression
import com.example.model.ChibiProfile
import com.example.ui.components.ChibiAvatar
import com.example.ui.screens.labs.CosmicBathtubLab
import com.example.ui.screens.labs.LightSpeedLab
import com.example.ui.screens.labs.MoonPhasesAndEclipseLab
import com.example.ui.screens.labs.OrbitsAndSeasonsLab
import com.example.ui.screens.labs.SpaceJumpLab
import com.example.ui.screens.labs.SpaceThermometerLab
import com.example.ui.theme.AuroraGreen
import com.example.ui.theme.ComicInk
import com.example.ui.theme.CosmicAmber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.NebulaMagenta
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceNavy
import com.example.ui.theme.SpaceVoid
import com.example.ui.theme.StarlightMuted
import com.example.ui.theme.StarlightWhite

private enum class SpaceLabKind(
  val id: String,
  val title: String,
  val icon: ImageVector,
  val accent: Color,
) {
  JUMP("jump", "Salto", Icons.Rounded.Height, AuroraGreen),
  MOON("moon", "Fasi & Eclissi", Icons.Rounded.Brightness3, CosmicAmber),
  LIGHT("light", "Velocità Luce", Icons.Rounded.Bolt, CosmicCyan),
  BATHTUB("bathtub", "Vasca Cosmica", Icons.Rounded.Water, CosmicCyan),
  ORBITS("orbits", "Orbite & Stagioni", Icons.Rounded.Public, NebulaMagenta),
  WEATHER("weather", "Termometro", Icons.Rounded.Thermostat, CosmicAmber),
  WEIGHT_AGE("weight_age", "Peso & Età", Icons.Rounded.Scale, AuroraGreen),
}

@Composable
fun LabsScreen(
  chibiProfile: ChibiProfile,
  userWeightKg: Float,
  userAgeYears: Float,
  onUserWeightChange: (Float) -> Unit,
  onUserAgeChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedLab by remember { mutableStateOf(SpaceLabKind.JUMP) }
  var showAllLabs by remember { mutableStateOf(false) }

  Box(
    modifier = modifier.fillMaxSize().background(SpaceVoid),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 680.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Hero Lab Header with SpaceBloom
      item {
        Surface(
          shape = RoundedCornerShape(24.dp),
          border = BorderStroke(2.dp, AuroraGreen.copy(alpha = 0.65f)),
          color = SpaceNavy,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            ChibiAvatar(
              profile = chibiProfile,
              expression = ChibiExpression.CHEERING,
              size = 82.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Surface(
                color = AuroraGreen,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, ComicInk),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(Icons.Rounded.Science, contentDescription = null, tint = ComicInk, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "7 ESPERIMENTI SPAZIALI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ComicInk,
                  )
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Laboratori di ${chibiProfile.name}",
                style = MaterialTheme.typography.headlineSmall,
                color = StarlightWhite,
              )
              Text(
                text = "Tocca un laboratorio qui sotto per fare esperimenti sullo spazio!",
                style = MaterialTheme.typography.bodyMedium,
                color = StarlightMuted,
              )
            }
          }
        }
      }

      // Selector Bar + Show All Toggle
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = if (showAllLabs) "Tutti i 7 Laboratori" else "Scegli un esperimento:",
              style = MaterialTheme.typography.labelLarge,
              color = CosmicCyan,
            )
            FilterChip(
              selected = showAllLabs,
              onClick = { showAllLabs = !showAllLabs },
              label = { Text(if (showAllLabs) "Mostra 1 alla volta" else "Mostra tutti (7)") },
              leadingIcon = { Icon(Icons.Rounded.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp)) },
              colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CosmicAmber, selectedLabelColor = ComicInk),
              modifier = Modifier.testTag("labs_show_all_toggle"),
            )
          }

          if (!showAllLabs) {
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              SpaceLabKind.entries.forEach { kind ->
                FilterChip(
                  selected = selectedLab == kind,
                  onClick = { selectedLab = kind },
                  label = { Text(kind.title) },
                  leadingIcon = { Icon(kind.icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      containerColor = SpaceCard,
                      labelColor = StarlightWhite,
                      selectedContainerColor = kind.accent,
                      selectedLabelColor = ComicInk,
                      selectedLeadingIconColor = ComicInk,
                    ),
                  modifier = Modifier.testTag("lab_chip_${kind.id}"),
                )
              }
            }
          }
        }
      }

      // Render either Selected Lab or All 7 Labs
      if (showAllLabs || selectedLab == SpaceLabKind.JUMP) {
        item(key = "lab_jump") { SpaceJumpLab(chibiProfile = chibiProfile) }
      }
      if (showAllLabs || selectedLab == SpaceLabKind.MOON) {
        item(key = "lab_moon") { MoonPhasesAndEclipseLab(chibiProfile = chibiProfile) }
      }
      if (showAllLabs || selectedLab == SpaceLabKind.LIGHT) {
        item(key = "lab_light") { LightSpeedLab(chibiProfile = chibiProfile) }
      }
      if (showAllLabs || selectedLab == SpaceLabKind.BATHTUB) {
        item(key = "lab_bathtub") { CosmicBathtubLab(chibiProfile = chibiProfile) }
      }
      if (showAllLabs || selectedLab == SpaceLabKind.ORBITS) {
        item(key = "lab_orbits") { OrbitsAndSeasonsLab(chibiProfile = chibiProfile) }
      }
      if (showAllLabs || selectedLab == SpaceLabKind.WEATHER) {
        item(key = "lab_weather") { SpaceThermometerLab(chibiProfile = chibiProfile) }
      }
      if (showAllLabs || selectedLab == SpaceLabKind.WEIGHT_AGE) {
        item(key = "lab_weight_age") {
          SpaceWeightAndAgeLab(
            weightKg = userWeightKg,
            ageYears = userAgeYears,
            onWeightChange = onUserWeightChange,
            onAgeChange = onUserAgeChange,
          )
        }
      }

      item { Spacer(modifier = Modifier.height(16.dp)) }
    }
  }
}
