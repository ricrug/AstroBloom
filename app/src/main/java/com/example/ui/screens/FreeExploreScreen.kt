package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.ChibiProfile
import com.example.ui.components.FreeSpaceExplorer3D

@Composable
fun FreeExploreScreen(
  chibiProfile: ChibiProfile,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenLandingSimulator: (String) -> Unit,
  onOpenFirstPersonCockpit: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  FreeSpaceExplorer3D(
    chibiProfile = chibiProfile,
    onOpenPlanetDetail = onOpenPlanetDetail,
    onOpenLandingSimulator = onOpenLandingSimulator,
    onOpenFirstPersonCockpit = onOpenFirstPersonCockpit,
    modifier = modifier.fillMaxSize(),
  )
}

