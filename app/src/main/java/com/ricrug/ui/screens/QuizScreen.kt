package com.ricrug.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ricrug.data.SolarSystemData
import com.ricrug.model.ChibiExpression
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.components.ChibiAvatar
import com.ricrug.ui.components.ChibiComicBubble
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
import com.ricrug.viewmodel.QuizUiState

@Composable
fun QuizScreen(
  chibiProfile: ChibiProfile,
  quizState: QuizUiState,
  bestQuizScore: Int,
  unlockedBadgeIds: Set<String>,
  onStartGalacticQuiz: () -> Unit,
  onStartTrueFalseQuiz: () -> Unit,
  onStartPlanetQuiz: (String) -> Unit,
  onSelectAnswer: (Int) -> Unit,
  onNextQuestion: () -> Unit,
  onExitQuiz: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize().background(SpaceVoid),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 680.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      when {
        !quizState.isPlaying -> {
          QuizHubContent(
            chibiProfile = chibiProfile,
            bestQuizScore = bestQuizScore,
            unlockedBadgeIds = unlockedBadgeIds,
            onStartGalacticQuiz = onStartGalacticQuiz,
            onStartTrueFalseQuiz = onStartTrueFalseQuiz,
            onStartPlanetQuiz = onStartPlanetQuiz,
          )
        }
        quizState.isFinished -> {
          QuizResultsContent(
            chibiProfile = chibiProfile,
            quizState = quizState,
            onReplay = {
              when (quizState.mode) {
                com.ricrug.viewmodel.QuizMode.GALACTIC_CHALLENGE -> onStartGalacticQuiz()
                com.ricrug.viewmodel.QuizMode.TRUE_OR_FALSE -> onStartTrueFalseQuiz()
                com.ricrug.viewmodel.QuizMode.PLANET_SPECIAL -> onStartGalacticQuiz()
              }
            },
            onBackToHub = onExitQuiz,
          )
        }
        else -> {
          ActiveQuizContent(
            chibiProfile = chibiProfile,
            quizState = quizState,
            onSelectAnswer = onSelectAnswer,
            onNextQuestion = onNextQuestion,
            onExitQuiz = onExitQuiz,
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun QuizHubContent(
  chibiProfile: ChibiProfile,
  bestQuizScore: Int,
  unlockedBadgeIds: Set<String>,
  onStartGalacticQuiz: () -> Unit,
  onStartTrueFalseQuiz: () -> Unit,
  onStartPlanetQuiz: (String) -> Unit,
) {
  ChibiComicBubble(
    profile = chibiProfile,
    expression = ChibiExpression.CHEERING,
    badgeText = "SFIDA SPAZIALE",
    headline = "Pronto a metterti alla prova?",
    dialogue =
      "Rispondi alle mie domande divertenti sul Sistema Solare! Ogni risposta giusta ti avvicina al grado di Comandante Galattico!",
  )

  // Two main quiz modes
  Surface(
    onClick = onStartGalacticQuiz,
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(2.dp, CosmicAmber),
    modifier = Modifier.fillMaxWidth().testTag("start_galactic_quiz"),
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        color = CosmicAmber,
        shape = CircleShape,
        border = BorderStroke(2.dp, ComicInk),
        modifier = Modifier.size(54.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = ComicInk, modifier = Modifier.size(28.dp))
        }
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text("Sfida Galattica", style = MaterialTheme.typography.titleLarge, color = StarlightWhite)
        Text(
          "8 domande miste su tutti i pianeti del Sistema Solare!",
          style = MaterialTheme.typography.bodyMedium,
          color = StarlightMuted,
        )
        if (bestQuizScore > 0) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Record personale: $bestQuizScore punti",
            style = MaterialTheme.typography.labelSmall,
            color = AuroraGreen,
          )
        }
      }
      Icon(Icons.Rounded.PlayArrow, contentDescription = "Inizia", tint = CosmicAmber, modifier = Modifier.size(28.dp))
    }
  }

  Surface(
    onClick = onStartTrueFalseQuiz,
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(2.dp, CosmicCyan),
    modifier = Modifier.fillMaxWidth().testTag("start_tf_quiz"),
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        color = CosmicCyan,
        shape = CircleShape,
        border = BorderStroke(2.dp, ComicInk),
        modifier = Modifier.size(54.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Rounded.Bolt, contentDescription = null, tint = ComicInk, modifier = Modifier.size(28.dp))
        }
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text("Vero o Falso Lampo", style = MaterialTheme.typography.titleLarge, color = StarlightWhite)
        Text(
          "Scopri subito cosa è vero e cosa è una bufala spaziale!",
          style = MaterialTheme.typography.bodyMedium,
          color = StarlightMuted,
        )
      }
      Icon(Icons.Rounded.PlayArrow, contentDescription = "Inizia", tint = CosmicCyan, modifier = Modifier.size(28.dp))
    }
  }

  // Planet-Specific Quiz Selector
  Text(
    text = "Quiz per Singolo Pianeta",
    style = MaterialTheme.typography.titleLarge,
    color = StarlightWhite,
  )
  Row(
    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    SolarSystemData.celestialBodies.forEach { body ->
      FilterChip(
        selected = false,
        onClick = { onStartPlanetQuiz(body.id) },
        label = { Text(body.name) },
        leadingIcon = {
          Box(modifier = Modifier.size(10.dp).background(body.primaryColor, CircleShape))
        },
        colors =
          FilterChipDefaults.filterChipColors(
            containerColor = SpaceCardElevated,
            labelColor = StarlightWhite,
          ),
        modifier = Modifier.testTag("quiz_planet_chip_${body.id}"),
      )
    }
  }

  // Explorer Badges Showcase
  Text(
    text = "I Tuoi Distintivi Spaziali",
    style = MaterialTheme.typography.titleLarge,
    color = StarlightWhite,
  )
  SolarSystemData.badges.forEach { badge ->
    val unlocked = unlockedBadgeIds.contains(badge.id)
    Surface(
      color = SpaceCard,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.5.dp, if (unlocked) badge.accentColor else SpaceOutline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = if (unlocked) badge.accentColor else SpaceNavy,
          shape = CircleShape,
          modifier = Modifier.size(44.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (unlocked) Icons.Rounded.EmojiEvents else Icons.Rounded.Lock,
              contentDescription = null,
              tint = if (unlocked) ComicInk else StarlightMuted,
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = badge.title,
            style = MaterialTheme.typography.titleMedium,
            color = if (unlocked) StarlightWhite else StarlightMuted,
          )
          Text(
            text = badge.description,
            style = MaterialTheme.typography.bodyMedium,
            color = StarlightMuted,
          )
        }
        if (unlocked) {
          Icon(Icons.Rounded.CheckCircle, contentDescription = "Sbloccato", tint = AuroraGreen)
        }
      }
    }
  }
}

@Composable
private fun ActiveQuizContent(
  chibiProfile: ChibiProfile,
  quizState: QuizUiState,
  onSelectAnswer: (Int) -> Unit,
  onNextQuestion: () -> Unit,
  onExitQuiz: () -> Unit,
) {
  val question = quizState.questions.getOrNull(quizState.currentIndex) ?: return
  val progress = (quizState.currentIndex + 1).toFloat() / quizState.questions.size.toFloat()
  val answered = quizState.selectedOptionIndex != null
  val isCorrect = quizState.selectedOptionIndex == question.correctIndex

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Surface(
        color = SpaceCardElevated,
        shape = RoundedCornerShape(10.dp),
      ) {
        Text(
          text = "Domanda ${quizState.currentIndex + 1}/${quizState.questions.size}",
          style = MaterialTheme.typography.labelMedium,
          color = StarlightWhite,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
      }
      if (quizState.currentStreak > 0) {
        Surface(
          color = CosmicAmber,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.5.dp, ComicInk),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = ComicInk, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Serie x${quizState.currentStreak}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ComicInk)
          }
        }
      }
    }
    IconButton(onClick = onExitQuiz, modifier = Modifier.testTag("quiz_exit_button")) {
      Icon(Icons.Rounded.Close, contentDescription = "Esci dal quiz", tint = StarlightMuted)
    }
  }

  LinearProgressIndicator(
    progress = { progress },
    color = CosmicCyan,
    trackColor = SpaceNavy,
    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
  )

  // Question Card
  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border = BorderStroke(2.dp, SpaceOutline),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Text(
        text = quizState.targetPlanetName?.let { "Speciale $it" } ?: quizState.mode.title,
        style = MaterialTheme.typography.labelMedium,
        color = CosmicCyan,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = question.question,
        style = MaterialTheme.typography.headlineSmall,
        color = StarlightWhite,
      )
    }
  }

  // Answer Options
  question.options.forEachIndexed { index, optionText ->
    val isThisSelected = quizState.selectedOptionIndex == index
    val isThisRight = index == question.correctIndex
    val containerColor =
      when {
        !answered -> SpaceCardElevated
        isThisRight -> AuroraGreen.copy(alpha = 0.25f)
        isThisSelected && !isThisRight -> NebulaMagenta.copy(alpha = 0.28f)
        else -> SpaceCard.copy(alpha = 0.6f)
      }
    val borderColor =
      when {
        !answered -> SpaceOutline
        isThisRight -> AuroraGreen
        isThisSelected && !isThisRight -> NebulaMagenta
        else -> SpaceOutline.copy(alpha = 0.4f)
      }

    Surface(
      onClick = { onSelectAnswer(index) },
      enabled = !answered,
      color = containerColor,
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(2.dp, borderColor),
      modifier = Modifier.fillMaxWidth().testTag("quiz_option_$index"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = optionText,
          style = MaterialTheme.typography.titleMedium,
          color = StarlightWhite,
          modifier = Modifier.weight(1f),
        )
        if (answered && isThisRight) {
          Icon(Icons.Rounded.CheckCircle, contentDescription = "Risposta corretta", tint = AuroraGreen)
        }
      }
    }
  }

  // Instant Chibi Reaction Bubble after answering
  if (answered) {
    ChibiComicBubble(
      profile = chibiProfile,
      expression = if (isCorrect) ChibiExpression.CHEERING else ChibiExpression.THINKING,
      badgeText = if (isCorrect) "BRAVISSIMO!" else "QUASI! ECCO IL SEGRETO:",
      dialogue = if (isCorrect) question.chibiRightComment else question.chibiWrongHint,
      avatarSize = 76.dp,
      footerContent = {
        Button(
          onClick = onNextQuestion,
          colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("quiz_next_button"),
        ) {
          Text(
            text = if (quizState.currentIndex + 1 >= quizState.questions.size) "Vedi Risultato" else "Prossima Domanda",
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
        }
      },
    )
  } else {
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      ChibiAvatar(profile = chibiProfile, expression = ChibiExpression.THINKING, size = 56.dp)
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "${chibiProfile.name} fa il tifo per te!",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )
    }
  }
}

@Composable
private fun QuizResultsContent(
  chibiProfile: ChibiProfile,
  quizState: QuizUiState,
  onReplay: () -> Unit,
  onBackToHub: () -> Unit,
) {
  val total = quizState.questions.size.coerceAtLeast(1)
  val ratio = quizState.score.toFloat() / total.toFloat()
  val rankTitle =
    when {
      ratio >= 0.85f -> "Comandante Galattico!"
      ratio >= 0.60f -> "Pilota Stellare!"
      else -> "Cadetto Spaziale!"
    }

  Surface(
    color = SpaceCard,
    shape = RoundedCornerShape(24.dp),
    border = BorderStroke(2.dp, CosmicAmber),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      ChibiAvatar(
        profile = chibiProfile,
        expression = if (ratio >= 0.6f) ChibiExpression.CHEERING else ChibiExpression.HAPPY,
        size = 156.dp,
        useFullBody = true,
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = rankTitle,
        style = MaterialTheme.typography.headlineMedium,
        color = CosmicAmber,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Hai totalizzato ${quizState.score} su $total risposte esatte!",
        style = MaterialTheme.typography.titleMedium,
        color = StarlightWhite,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Serie migliore: ${quizState.maxStreak} di fila",
        style = MaterialTheme.typography.bodyMedium,
        color = AuroraGreen,
      )

      Spacer(modifier = Modifier.height(18.dp))
      Button(
        onClick = onReplay,
        colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("quiz_replay_button"),
      ) {
        Icon(Icons.Rounded.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Gioca Ancora!", style = MaterialTheme.typography.titleMedium)
      }
      Spacer(modifier = Modifier.height(10.dp))
      OutlinedButton(
        onClick = onBackToHub,
        border = BorderStroke(1.5.dp, SpaceOutline),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("quiz_hub_button"),
      ) {
        Text("Torna ai Quiz", color = StarlightWhite, style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}
