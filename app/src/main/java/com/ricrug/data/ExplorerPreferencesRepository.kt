package com.ricrug.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ricrug.model.ChibiProfile
import com.ricrug.model.HairStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.explorerDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "astro_explorer_prefs")

class ExplorerPreferencesRepository(private val context: Context) {

  private object Keys {
    val CHIBI_NAME = stringPreferencesKey("chibi_name")
    val HAIR_STYLE = stringPreferencesKey("hair_style")
    val HAIR_COLOR = intPreferencesKey("hair_color")
    val SKIN_TONE = intPreferencesKey("skin_tone")
    val SUIT_COLOR = intPreferencesKey("suit_color")
    val HAS_GLASSES = booleanPreferencesKey("has_glasses")
    val HAS_FRECKLES = booleanPreferencesKey("has_freckles")
    val EXPLORED_PLANETS = stringSetPreferencesKey("explored_planets")
    val BEST_QUIZ_SCORE = intPreferencesKey("best_quiz_score")
    val QUIZ_STAR_UNLOCKED = booleanPreferencesKey("quiz_star_unlocked")
    val BEST_ASTEROID_DODGE_SCORE = intPreferencesKey("best_asteroid_dodge_score")
    val TOTAL_EXPLORATION_XP = intPreferencesKey("total_exploration_xp")
    val DISCOVERED_POIS = stringSetPreferencesKey("discovered_pois")
    val COLLECTED_SAMPLES = stringSetPreferencesKey("collected_samples")
  }

  val chibiProfileFlow: Flow<ChibiProfile> =
    context.explorerDataStore.data.map { prefs ->
      val hairStyleName = prefs[Keys.HAIR_STYLE] ?: HairStyle.CIUFFO.name
      val hairStyle = HairStyle.entries.firstOrNull { it.name == hairStyleName } ?: HairStyle.CIUFFO
      ChibiProfile(
        name = prefs[Keys.CHIBI_NAME] ?: "SpaceBloom",
        hairStyle = hairStyle,
        hairColorIndex = prefs[Keys.HAIR_COLOR] ?: 0,
        skinToneIndex = prefs[Keys.SKIN_TONE] ?: 1,
        suitColorIndex = prefs[Keys.SUIT_COLOR] ?: 0,
        hasGlasses = prefs[Keys.HAS_GLASSES] ?: false,
        hasFreckles = prefs[Keys.HAS_FRECKLES] ?: false,
      )
    }

  val exploredPlanetIdsFlow: Flow<Set<String>> =
    context.explorerDataStore.data.map { prefs -> prefs[Keys.EXPLORED_PLANETS] ?: emptySet() }

  val bestQuizScoreFlow: Flow<Int> =
    context.explorerDataStore.data.map { prefs -> prefs[Keys.BEST_QUIZ_SCORE] ?: 0 }

  val quizStarUnlockedFlow: Flow<Boolean> =
    context.explorerDataStore.data.map { prefs -> prefs[Keys.QUIZ_STAR_UNLOCKED] ?: false }

  val bestAsteroidDodgeScoreFlow: Flow<Int> =
    context.explorerDataStore.data.map { prefs -> prefs[Keys.BEST_ASTEROID_DODGE_SCORE] ?: 0 }

  val totalExplorationXpFlow: Flow<Int> =
    context.explorerDataStore.data.map { prefs -> prefs[Keys.TOTAL_EXPLORATION_XP] ?: 0 }

  val discoveredPoiIdsFlow: Flow<Set<String>> =
    context.explorerDataStore.data.map { prefs -> prefs[Keys.DISCOVERED_POIS] ?: emptySet() }

  val collectedSampleIdsFlow: Flow<Set<String>> =
    context.explorerDataStore.data.map { prefs -> prefs[Keys.COLLECTED_SAMPLES] ?: emptySet() }

  suspend fun recordPoiDiscovered(poiId: String, xp: Int) {
    context.explorerDataStore.edit { prefs ->
      val currentPois = prefs[Keys.DISCOVERED_POIS] ?: emptySet()
      if (poiId !in currentPois) {
        prefs[Keys.DISCOVERED_POIS] = currentPois + poiId
        val prevXp = prefs[Keys.TOTAL_EXPLORATION_XP] ?: 0
        prefs[Keys.TOTAL_EXPLORATION_XP] = prevXp + xp
      }
    }
  }

  suspend fun recordSampleCollected(sampleId: String, xp: Int) {
    context.explorerDataStore.edit { prefs ->
      val currentSamples = prefs[Keys.COLLECTED_SAMPLES] ?: emptySet()
      if (sampleId !in currentSamples) {
        prefs[Keys.COLLECTED_SAMPLES] = currentSamples + sampleId
        val prevXp = prefs[Keys.TOTAL_EXPLORATION_XP] ?: 0
        prefs[Keys.TOTAL_EXPLORATION_XP] = prevXp + xp
      }
    }
  }

  suspend fun saveBestAsteroidDodgeScore(score: Int) {
    context.explorerDataStore.edit { prefs ->
      val prevBest = prefs[Keys.BEST_ASTEROID_DODGE_SCORE] ?: 0
      if (score > prevBest) {
        prefs[Keys.BEST_ASTEROID_DODGE_SCORE] = score
      }
    }
  }

  suspend fun saveChibiProfile(profile: ChibiProfile) {
    context.explorerDataStore.edit { prefs ->
      prefs[Keys.CHIBI_NAME] = profile.name.ifBlank { "SpaceBloom" }
      prefs[Keys.HAIR_STYLE] = profile.hairStyle.name
      prefs[Keys.HAIR_COLOR] = profile.hairColorIndex
      prefs[Keys.SKIN_TONE] = profile.skinToneIndex
      prefs[Keys.SUIT_COLOR] = profile.suitColorIndex
      prefs[Keys.HAS_GLASSES] = profile.hasGlasses
      prefs[Keys.HAS_FRECKLES] = profile.hasFreckles
    }
  }

  suspend fun markPlanetExplored(planetId: String) {
    context.explorerDataStore.edit { prefs ->
      val current = prefs[Keys.EXPLORED_PLANETS] ?: emptySet()
      prefs[Keys.EXPLORED_PLANETS] = current + planetId
    }
  }

  suspend fun recordQuizResult(score: Int) {
    context.explorerDataStore.edit { prefs ->
      val prevBest = prefs[Keys.BEST_QUIZ_SCORE] ?: 0
      if (score > prevBest) {
        prefs[Keys.BEST_QUIZ_SCORE] = score
      }
      if (score >= 6) {
        prefs[Keys.QUIZ_STAR_UNLOCKED] = true
      }
    }
  }
}
