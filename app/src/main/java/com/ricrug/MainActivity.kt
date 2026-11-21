package com.ricrug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ricrug.ui.components.ChibiCustomizerSheet
import com.ricrug.ui.screens.AsteroidDodgeScreen
import com.ricrug.ui.screens.CuriositiesScreen
import com.ricrug.ui.screens.FirstPersonSimulatorScreen
import com.ricrug.ui.screens.FreeExploreScreen
import com.ricrug.ui.screens.LabsScreen
import com.ricrug.ui.screens.LandingSimulatorScreen
import com.ricrug.ui.screens.PlanetDetailScreen
import com.ricrug.ui.screens.PlanetsHomeScreen
import com.ricrug.ui.screens.QuizScreen
import com.ricrug.ui.screens.SplashScreen
import com.ricrug.ui.screens.SurfaceExplorationScreen
import com.ricrug.ui.theme.AstroViaggioTheme
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.NunitoFontFamily
import com.ricrug.ui.theme.SpaceCardElevated
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite
import com.ricrug.viewmodel.AstroViewModel

private sealed class AstroTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  data object Planets : AstroTab("planets", "Pianeti", Icons.Filled.Public, Icons.Outlined.Public)
  data object FreeExplore : AstroTab("free_explore", "Esplora 3D", Icons.Filled.Explore, Icons.Outlined.Explore)
  data object Labs : AstroTab("labs", "Laboratori", Icons.Filled.Science, Icons.Outlined.Science)
  data object Curiosities : AstroTab("curiosities", "Curiosità", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
  data object Quiz : AstroTab("quiz", "Quiz", Icons.Filled.Quiz, Icons.Outlined.Quiz)
}

private const val ROUTE_PLANET_DETAIL = "planet_detail/{planetId}"
private const val ROUTE_LANDING_SIM = "landing_sim/{planetId}"
private const val ROUTE_SURFACE_EXPLORE = "surface_explore/{planetId}"
private const val ROUTE_FIRST_PERSON_SIM = "first_person_sim"
private const val ROUTE_ASTEROID_DODGE = "asteroid_dodge"

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AstroViaggioTheme {
        var showSplash by rememberSaveable { mutableStateOf(true) }
        Crossfade(
          targetState = showSplash,
          animationSpec = tween(durationMillis = 450),
          label = "splashTransition",
        ) { splashVisible ->
          if (splashVisible) {
            SplashScreen(onSplashFinished = { showSplash = false })
          } else {
            AstroViaggioApp()
          }
        }
      }
    }
  }
}

@Composable
fun AstroViaggioApp(viewModel: AstroViewModel = viewModel()) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  val currentRoute = currentDestination?.route

  val chibiProfile by viewModel.chibiProfile.collectAsStateWithLifecycle()
  val exploredPlanetIds by viewModel.exploredPlanetIds.collectAsStateWithLifecycle()
  val bestQuizScore by viewModel.bestQuizScore.collectAsStateWithLifecycle()
  val unlockedBadgeIds by viewModel.unlockedBadgeIds.collectAsStateWithLifecycle()
  val quizState by viewModel.quizState.collectAsStateWithLifecycle()
  val userWeightKg by viewModel.userWeightKg.collectAsStateWithLifecycle()
  val userAgeYears by viewModel.userAgeYears.collectAsStateWithLifecycle()
  val bestAsteroidDodgeScore by viewModel.bestAsteroidDodgeScore.collectAsStateWithLifecycle()
  val totalExplorationXp by viewModel.totalExplorationXp.collectAsStateWithLifecycle()
  val discoveredPoiIds by viewModel.discoveredPoiIds.collectAsStateWithLifecycle()
  val collectedSampleIds by viewModel.collectedSampleIds.collectAsStateWithLifecycle()

  var showChibiSheet by remember { mutableStateOf(false) }

  val tabs = listOf(AstroTab.Planets, AstroTab.FreeExplore, AstroTab.Labs, AstroTab.Curiosities, AstroTab.Quiz)
  val isTopLevelRoute = currentRoute == null || tabs.any { it.route == currentRoute }

  fun navigateToTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavRail = maxWidth >= 600.dp

    val isFullScreenCockpit =
      currentRoute == ROUTE_FIRST_PERSON_SIM ||
        currentRoute == ROUTE_ASTEROID_DODGE ||
        currentRoute?.startsWith("surface_explore") == true

    Scaffold(
      containerColor = SpaceVoid,
      contentWindowInsets =
        if (isFullScreenCockpit) WindowInsets(0, 0, 0, 0) else WindowInsets.safeDrawing,
      bottomBar = {
        if (isTopLevelRoute && !useNavRail) {
          NavigationBar(containerColor = SpaceNavy) {
            tabs.forEach { tab ->
              val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(tab.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = {
                  Text(
                    text = tab.label,
                    style =
                      MaterialTheme.typography.labelSmall.copy(
                        fontFamily = NunitoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                      ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = ComicInk,
                    selectedTextColor = CosmicAmber,
                    indicatorColor = CosmicAmber,
                    unselectedIconColor = StarlightMuted,
                    unselectedTextColor = StarlightMuted,
                  ),
                modifier = Modifier.testTag("nav_tab_${tab.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (isTopLevelRoute && useNavRail) {
          NavigationRail(containerColor = SpaceNavy) {
            tabs.forEach { tab ->
              val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(tab.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                colors =
                  NavigationRailItemDefaults.colors(
                    selectedIconColor = ComicInk,
                    selectedTextColor = CosmicAmber,
                    indicatorColor = CosmicAmber,
                    unselectedIconColor = StarlightMuted,
                    unselectedTextColor = StarlightMuted,
                  ),
                modifier = Modifier.testTag("rail_tab_${tab.route}"),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          NavHost(
            navController = navController,
            startDestination = AstroTab.Planets.route,
          ) {
            composable(AstroTab.Planets.route) {
              PlanetsHomeScreen(
                chibiProfile = chibiProfile,
                exploredPlanetIds = exploredPlanetIds,
                onOpenPlanetDetail = { planetId ->
                  navController.navigate("planet_detail/$planetId")
                },
                onOpenFreeExplore = {
                  navigateToTab(AstroTab.FreeExplore.route)
                },
                onOpenChibiCustomizer = { showChibiSheet = true },
                onOpenAsteroidDodge = {
                  navController.navigate(ROUTE_ASTEROID_DODGE)
                },
              )
            }

            composable(AstroTab.FreeExplore.route) {
              FreeExploreScreen(
                chibiProfile = chibiProfile,
                onOpenPlanetDetail = { planetId ->
                  navController.navigate("planet_detail/$planetId")
                },
                onOpenLandingSimulator = { planetId ->
                  navController.navigate("landing_sim/$planetId")
                },
                onOpenFirstPersonCockpit = {
                  navController.navigate(ROUTE_FIRST_PERSON_SIM)
                },
                onOpenAsteroidDodge = {
                  navController.navigate(ROUTE_ASTEROID_DODGE)
                },
              )
            }

            composable(AstroTab.Labs.route) {
              LabsScreen(
                chibiProfile = chibiProfile,
                userWeightKg = userWeightKg,
                userAgeYears = userAgeYears,
                onUserWeightChange = viewModel::setUserWeightKg,
                onUserAgeChange = viewModel::setUserAgeYears,
              )
            }

            composable(AstroTab.Curiosities.route) {
              CuriositiesScreen(
                chibiProfile = chibiProfile,
                userWeightKg = userWeightKg,
                userAgeYears = userAgeYears,
                onUserWeightChange = viewModel::setUserWeightKg,
                onUserAgeChange = viewModel::setUserAgeYears,
                onOpenPlanetDetail = { planetId ->
                  navController.navigate("planet_detail/$planetId")
                },
                onOpenLabs = {
                  navigateToTab(AstroTab.Labs.route)
                },
              )
            }

            composable(AstroTab.Quiz.route) {
              QuizScreen(
                chibiProfile = chibiProfile,
                quizState = quizState,
                bestQuizScore = bestQuizScore,
                unlockedBadgeIds = unlockedBadgeIds,
                onStartGalacticQuiz = viewModel::startGalacticQuiz,
                onStartTrueFalseQuiz = viewModel::startTrueFalseQuiz,
                onStartPlanetQuiz = viewModel::startPlanetQuiz,
                onSelectAnswer = viewModel::selectQuizAnswer,
                onNextQuestion = viewModel::nextQuizQuestion,
                onExitQuiz = viewModel::exitQuiz,
              )
            }

            composable(
              route = ROUTE_PLANET_DETAIL,
              arguments = listOf(navArgument("planetId") { type = NavType.StringType }),
            ) { backStackEntry ->
              val planetId = backStackEntry.arguments?.getString("planetId") ?: "earth"
              PlanetDetailScreen(
                planetId = planetId,
                chibiProfile = chibiProfile,
                userWeightKg = userWeightKg,
                onUserWeightChange = viewModel::setUserWeightKg,
                onMarkExplored = viewModel::markPlanetExplored,
                onBack = { navController.popBackStack() },
                onStartPlanetQuiz = { targetId ->
                  viewModel.startPlanetQuiz(targetId)
                  navController.navigate(AstroTab.Quiz.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                      saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                  }
                },
                onOpenLandingSimulator = { targetId ->
                  navController.navigate("landing_sim/$targetId")
                },
                onOpenSurfaceExploration = { targetId ->
                  navController.navigate("surface_explore/$targetId")
                },
              )
            }

            composable(
              route = ROUTE_LANDING_SIM,
              arguments = listOf(navArgument("planetId") { type = NavType.StringType }),
            ) { backStackEntry ->
              val planetId = backStackEntry.arguments?.getString("planetId") ?: "moon"
              if (planetId == "earth") {
                LaunchedEffect(Unit) {
                  navController.popBackStack()
                }
              } else {
                LandingSimulatorScreen(
                  planetId = planetId,
                  chibiProfile = chibiProfile,
                  onMarkExplored = viewModel::markPlanetExplored,
                  onBack = { navController.popBackStack() },
                  onStartSurfaceExploration = { targetId ->
                    navController.navigate("surface_explore/$targetId")
                  },
                )
              }
            }

            composable(
              route = ROUTE_SURFACE_EXPLORE,
              arguments = listOf(navArgument("planetId") { type = NavType.StringType }),
            ) { backStackEntry ->
              val planetId = backStackEntry.arguments?.getString("planetId") ?: "moon"
              if (planetId == "earth") {
                LaunchedEffect(Unit) {
                  navController.popBackStack()
                }
              } else {
                SurfaceExplorationScreen(
                  planetId = planetId,
                  chibiProfile = chibiProfile,
                  savedTotalXp = totalExplorationXp,
                  initialDiscoveredPoiIds = discoveredPoiIds,
                  initialCollectedSampleIds = collectedSampleIds,
                  onRecordPoiDiscovered = viewModel::recordPoiDiscovered,
                  onRecordSampleCollected = viewModel::recordSampleCollected,
                  onExitExploration = { navController.popBackStack() },
                  onMarkPlanetExplored = viewModel::markPlanetExplored,
                )
              }
            }

            composable(route = ROUTE_FIRST_PERSON_SIM) {
              FirstPersonSimulatorScreen(
                chibiProfile = chibiProfile,
                onOpenPlanetDetail = { planetId ->
                  navController.navigate("planet_detail/$planetId")
                },
                onOpenLandingSimulator = { planetId ->
                  navController.navigate("landing_sim/$planetId")
                },
                onExitSimulator = { navController.popBackStack() },
                onOpenAsteroidDodge = {
                  navController.navigate(ROUTE_ASTEROID_DODGE)
                },
              )
            }

            composable(route = ROUTE_ASTEROID_DODGE) {
              AsteroidDodgeScreen(
                chibiProfile = chibiProfile,
                bestScore = bestAsteroidDodgeScore,
                onSaveHighScore = viewModel::saveAsteroidDodgeScore,
                onExitGame = { navController.popBackStack() },
              )
            }
          }
        }
      }
    }
  }

  if (showChibiSheet) {
    ChibiCustomizerSheet(
      profile = chibiProfile,
      onDismiss = { showChibiSheet = false },
      onSave = viewModel::updateChibiProfile,
    )
  }
}
