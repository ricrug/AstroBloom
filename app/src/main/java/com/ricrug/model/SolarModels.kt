package com.ricrug.model

import androidx.compose.ui.graphics.Color

enum class PlanetSurfaceStyle {
  SUN,
  MERCURY,
  VENUS,
  EARTH,
  MOON,
  MARS,
  JUPITER,
  SATURN,
  URANUS,
  NEPTUNE,
  PLUTO,
}

enum class ChibiExpression {
  HAPPY,
  AMAZED,
  THINKING,
  CHEERING,
  ROCKET,
}

enum class HairStyle(val label: String) {
  CIUFFO("Ciuffo"),
  RICCIO("Riccio"),
  CORTO("Corto"),
  LUNGO("Lungo"),
  CODINI("Codini"),
}

data class ChibiProfile(
  val name: String = "SpaceBloom",
  val hairStyle: HairStyle = HairStyle.CIUFFO,
  val hairColorIndex: Int = 0,
  val skinToneIndex: Int = 1,
  val suitColorIndex: Int = 0,
  val hasGlasses: Boolean = false,
  val hasFreckles: Boolean = false,
)

data class ComicPanel(
  val badge: String,
  val title: String,
  val dialogue: String,
  val expression: ChibiExpression,
)

data class CelestialBody(
  val id: String,
  val name: String,
  val nickname: String,
  val category: String,
  val orderIndex: Int,
  val diameterKm: Int,
  val earthSizeRatio: Float,
  val distanceFromSunLabel: String,
  val dayLengthLabel: String,
  val yearLengthLabel: String,
  val yearLengthEarthYears: Float,
  val temperatureLabel: String,
  val moonsCount: Int,
  val notableMoons: List<String>,
  val gravityRatio: Float,
  val axialTiltDeg: Float,
  val orbitDistanceScene: Float,
  val orbitSpeedDegPerSec: Float,
  val sceneVisualRadius: Float,
  val primaryColor: Color,
  val secondaryColor: Color,
  val accentColor: Color,
  val atmosphereColor: Color?,
  val hasRings: Boolean = false,
  val ringColor: Color = Color.Transparent,
  val surfaceStyle: PlanetSurfaceStyle,
  val quickHook: String,
  val sizeAnalogy: String,
  val comicStories: List<ComicPanel>,
)

enum class CuriosityCategory(val label: String) {
  ALL("Tutte"),
  EXTREME_PLANETS("Pianeti Estremi"),
  COSMIC_RECORDS("Record del Cosmo"),
  MOONS("Sulle Lune"),
  WEIRD_SPACE("Bizzarrie Spaziali"),
  MYSTERIES("Misteri e Stelle"),
}

data class CuriosityItem(
  val id: String,
  val title: String,
  val category: CuriosityCategory,
  val badgeTag: String,
  val summary: String,
  val deepDive: String,
  val chibiReaction: String,
  val relatedPlanetId: String?,
  val accentColor: Color,
)

data class QuizQuestion(
  val id: String,
  val planetId: String?,
  val isTrueFalse: Boolean = false,
  val question: String,
  val options: List<String>,
  val correctIndex: Int,
  val chibiRightComment: String,
  val chibiWrongHint: String,
)

data class SpaceBadge(
  val id: String,
  val title: String,
  val description: String,
  val accentColor: Color,
)
