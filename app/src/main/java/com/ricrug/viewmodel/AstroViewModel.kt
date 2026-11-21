package com.ricrug.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ricrug.data.ExplorerPreferencesRepository
import com.ricrug.data.SolarSystemData
import com.ricrug.model.ChibiProfile
import com.ricrug.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuizMode(val title: String, val subtitle: String) {
  GALACTIC_CHALLENGE("Sfida Galattica", "8 domande miste su tutto il Sistema Solare"),
  TRUE_OR_FALSE("Vero o Falso Lampo", "Rispondi a raffica alle bizzarrie spaziali"),
  PLANET_SPECIAL("Quiz sul Pianeta", "Domande mirate sul pianeta scelto"),
}

data class QuizUiState(
  val isPlaying: Boolean = false,
  val mode: QuizMode = QuizMode.GALACTIC_CHALLENGE,
  val targetPlanetName: String? = null,
  val questions: List<QuizQuestion> = emptyList(),
  val currentIndex: Int = 0,
  val selectedOptionIndex: Int? = null,
  val score: Int = 0,
  val currentStreak: Int = 0,
  val maxStreak: Int = 0,
  val isFinished: Boolean = false,
)

class AstroViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ExplorerPreferencesRepository(application.applicationContext)

  val chibiProfile: StateFlow<ChibiProfile> =
    repository.chibiProfileFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = ChibiProfile(),
    )

  val exploredPlanetIds: StateFlow<Set<String>> =
    repository.exploredPlanetIdsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet(),
    )

  val bestQuizScore: StateFlow<Int> =
    repository.bestQuizScoreFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = 0,
    )

  val bestAsteroidDodgeScore: StateFlow<Int> =
    repository.bestAsteroidDodgeScoreFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = 0,
    )

  val totalExplorationXp: StateFlow<Int> =
    repository.totalExplorationXpFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = 0,
    )

  val discoveredPoiIds: StateFlow<Set<String>> =
    repository.discoveredPoiIdsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet(),
    )

  val collectedSampleIds: StateFlow<Set<String>> =
    repository.collectedSampleIdsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet(),
    )

  fun recordPoiDiscovered(poiId: String, xp: Int) {
    viewModelScope.launch { repository.recordPoiDiscovered(poiId, xp) }
  }

  fun recordSampleCollected(sampleId: String, xp: Int) {
    viewModelScope.launch { repository.recordSampleCollected(sampleId, xp) }
  }

  fun saveAsteroidDodgeScore(score: Int) {
    viewModelScope.launch { repository.saveBestAsteroidDodgeScore(score) }
  }

  val unlockedBadgeIds: StateFlow<Set<String>> =
    combine(exploredPlanetIds, repository.quizStarUnlockedFlow) { explored, quizStar ->
        val badges = mutableSetOf<String>()
        if (explored.isNotEmpty()) badges.add("badge_first_planet")
        val innerPlanets = setOf("mercury", "venus", "earth", "mars")
        if (explored.containsAll(innerPlanets)) badges.add("badge_inner_system")
        if (explored.size >= SolarSystemData.celestialBodies.size) {
          badges.add("badge_all_planets")
        }
        if (quizStar) badges.add("badge_quiz_star")
        badges
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
      )

  private val _quizState = MutableStateFlow(QuizUiState())
  val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

  private val _userWeightKg = MutableStateFlow(35f)
  val userWeightKg: StateFlow<Float> = _userWeightKg.asStateFlow()

  private val _userAgeYears = MutableStateFlow(10f)
  val userAgeYears: StateFlow<Float> = _userAgeYears.asStateFlow()

  fun updateChibiProfile(profile: ChibiProfile) {
    viewModelScope.launch { repository.saveChibiProfile(profile) }
  }

  fun markPlanetExplored(planetId: String) {
    viewModelScope.launch { repository.markPlanetExplored(planetId) }
  }

  fun setUserWeightKg(weight: Float) {
    _userWeightKg.value = weight.coerceIn(15f, 120f)
  }

  fun setUserAgeYears(age: Float) {
    _userAgeYears.value = age.coerceIn(5f, 99f)
  }

  fun startGalacticQuiz() {
    val selected = SolarSystemData.quizQuestions.shuffled().take(8)
    _quizState.value =
      QuizUiState(
        isPlaying = true,
        mode = QuizMode.GALACTIC_CHALLENGE,
        questions = selected,
      )
  }

  fun startTrueFalseQuiz() {
    val tf = SolarSystemData.quizQuestions.filter { it.isTrueFalse }.shuffled()
    _quizState.value =
      QuizUiState(
        isPlaying = true,
        mode = QuizMode.TRUE_OR_FALSE,
        questions = tf,
      )
  }

  fun startPlanetQuiz(planetId: String) {
    val body = SolarSystemData.getPlanetById(planetId)
    val planetSpecific = SolarSystemData.quizQuestions.filter { it.planetId == planetId }
    val filler =
      SolarSystemData.quizQuestions.filter { it.planetId != planetId }.shuffled().take(
        (4 - planetSpecific.size).coerceAtLeast(0)
      )
    val combined = (planetSpecific + filler).ifEmpty { SolarSystemData.quizQuestions.take(4) }
    _quizState.value =
      QuizUiState(
        isPlaying = true,
        mode = QuizMode.PLANET_SPECIAL,
        targetPlanetName = body.name,
        questions = combined,
      )
  }

  fun selectQuizAnswer(optionIndex: Int) {
    val current = _quizState.value
    if (current.selectedOptionIndex != null || current.isFinished) return
    val question = current.questions.getOrNull(current.currentIndex) ?: return
    val isCorrect = optionIndex == question.correctIndex
    val newScore = if (isCorrect) current.score + 1 else current.score
    val newStreak = if (isCorrect) current.currentStreak + 1 else 0
    _quizState.update {
      it.copy(
        selectedOptionIndex = optionIndex,
        score = newScore,
        currentStreak = newStreak,
        maxStreak = maxOf(it.maxStreak, newStreak),
      )
    }
  }

  fun nextQuizQuestion() {
    val current = _quizState.value
    if (current.currentIndex + 1 >= current.questions.size) {
      _quizState.update { it.copy(isFinished = true) }
      viewModelScope.launch { repository.recordQuizResult(current.score) }
    } else {
      _quizState.update {
        it.copy(currentIndex = it.currentIndex + 1, selectedOptionIndex = null)
      }
    }
  }

  fun exitQuiz() {
    _quizState.value = QuizUiState()
  }
}
