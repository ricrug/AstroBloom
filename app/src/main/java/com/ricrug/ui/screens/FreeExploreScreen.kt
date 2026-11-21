package com.ricrug.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.components.FreeSpaceExplorer3D

@Composable
fun FreeExploreScreen(
  chibiProfile: ChibiProfile,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenLandingSimulator: (String) -> Unit,
  onOpenFirstPersonCockpit: () -> Unit = {},
  onOpenAsteroidDodge: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  FreeSpaceExplorer3D(
    chibiProfile = chibiProfile,
    onOpenPlanetDetail = onOpenPlanetDetail,
    onOpenLandingSimulator = onOpenLandingSimulator,
    onOpenFirstPersonCockpit = onOpenFirstPersonCockpit,
    onOpenAsteroidDodge = onOpenAsteroidDodge,
    modifier = modifier.fillMaxSize(),
  )
}

