package com.ricrug.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricrug.model.ChibiExpression
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.components.ChibiAvatar
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.FredokaFontFamily
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceCard
import com.ricrug.ui.theme.SpaceCardElevated
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private data class GameAsteroid(
  val id: Long,
  var x: Float,
  var y: Float,
  val vx: Float,
  val vy: Float,
  val radius: Float,
  var rotation: Float,
  val rotSpeed: Float,
  var hp: Int,
  val color: Color,
  val isIceComet: Boolean = false,
)

private data class GameLaser(
  val id: Long,
  var x: Float,
  var y: Float,
  val vy: Float = -1200f,
)

private data class GameDrop(
  val id: Long,
  var x: Float,
  var y: Float,
  val vy: Float = 220f,
  val type: DropType,
)

private enum class DropType {
  SHIELD,
  WARP,
  STAR,
  REPAIR,
}

private data class GameParticle(
  var x: Float,
  var y: Float,
  val vx: Float,
  val vy: Float,
  val color: Color,
  var alpha: Float = 1f,
  val radius: Float = 3f,
)

private data class GameStage(
  val name: String,
  val fromPlanet: String,
  val toPlanet: String,
  val toPlanetColor: Color,
  val targetDistanceM: Float,
  val asteroidRate: Float,
)

@Composable
fun AsteroidDodgeScreen(
  chibiProfile: ChibiProfile,
  bestScore: Int,
  onSaveHighScore: (Int) -> Unit,
  onExitGame: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()

  BackHandler { onExitGame() }

  val stages = remember {
    listOf(
      GameStage("Dalla Terra a Marte", "Terra", "Marte", Color(0xFFE27B58), 900f, 1.0f),
      GameStage("Cintura di Asteroidi", "Marte", "Giove", Color(0xFFD4A373), 2200f, 1.4f),
      GameStage("Anelli di Saturno", "Giove", "Saturno", Color(0xFFE9C46A), 3800f, 1.8f),
      GameStage("Spazio Profondo", "Saturno", "Nettuno", Color(0xFF4A90E2), 5600f, 2.2f),
      GameStage("Cintura di Kuiper", "Nettuno", "Plutone", Color(0xFFB5838D), 8000f, 2.6f),
    )
  }

  // Game lifecycle
  var isPlaying by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var stageClearDialog by remember { mutableStateOf(false) }
  var score by remember { mutableIntStateOf(0) }
  var distanceMeters by remember { mutableFloatStateOf(0f) }
  var currentStageIndex by remember { mutableIntStateOf(0) }
  var lives by remember { mutableIntStateOf(3) }
  var shieldTimerSec by remember { mutableFloatStateOf(0f) }
  var warpTimerSec by remember { mutableFloatStateOf(0f) }

  // Ship position (normalized 0..1)
  var shipNormX by remember { mutableFloatStateOf(0.5f) }
  var shipNormY by remember { mutableFloatStateOf(0.82f) }

  // Game objects
  val asteroids = remember { mutableStateListOf<GameAsteroid>() }
  val lasers = remember { mutableStateListOf<GameLaser>() }
  val drops = remember { mutableStateListOf<GameDrop>() }
  val particles = remember { mutableStateListOf<GameParticle>() }

  // Fire laser trigger
  var autoFire by remember { mutableStateOf(true) }
  var lastLaserNanos by remember { mutableStateOf(0L) }
  var nextEntityId by remember { mutableStateOf(1L) }
  var spawnTimer by remember { mutableFloatStateOf(0f) }

  // Three-layer parallax starfield
  val starsLayer1 = remember { List(35) { Offset(Random.nextFloat(), Random.nextFloat()) to (0.4f + Random.nextFloat() * 0.4f) } }
  val starsLayer2 = remember { List(45) { Offset(Random.nextFloat(), Random.nextFloat()) to (0.5f + Random.nextFloat() * 0.5f) } }
  val starsLayer3 = remember { List(30) { Offset(Random.nextFloat(), Random.nextFloat()) to (0.7f + Random.nextFloat() * 0.3f) } }
  var starScroll1 by remember { mutableFloatStateOf(0f) }
  var starScroll2 by remember { mutableFloatStateOf(0f) }
  var starScroll3 by remember { mutableFloatStateOf(0f) }

  fun restartGame() {
    asteroids.clear()
    lasers.clear()
    drops.clear()
    particles.clear()
    score = 0
    distanceMeters = 0f
    currentStageIndex = 0
    lives = 3
    shieldTimerSec = 4f
    warpTimerSec = 0f
    shipNormX = 0.5f
    shipNormY = 0.82f
    isGameOver = false
    stageClearDialog = false
    isPlaying = true
  }

  // 60fps Game Loop
  LaunchedEffect(isPlaying, isGameOver, stageClearDialog) {
    if (!isPlaying || isGameOver || stageClearDialog) return@LaunchedEffect

    var lastFrameNanos = 0L
    while (isPlaying && !isGameOver && !stageClearDialog) {
      withFrameNanos { nanos ->
        if (lastFrameNanos != 0L) {
          val dt = ((nanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)

          val speedMult = if (warpTimerSec > 0f) 2.6f else 1.0f
          val forwardSpeed = 85f * speedMult
          distanceMeters += forwardSpeed * dt
          score += (forwardSpeed * dt * 0.5f).roundToInt()

          // Timers
          if (shieldTimerSec > 0f) shieldTimerSec = (shieldTimerSec - dt).coerceAtLeast(0f)
          if (warpTimerSec > 0f) warpTimerSec = (warpTimerSec - dt).coerceAtLeast(0f)

          // Parallax star scroll
          val scrollBase = dt * (0.15f * speedMult)
          starScroll1 = (starScroll1 + scrollBase * 0.4f) % 1f
          starScroll2 = (starScroll2 + scrollBase * 0.8f) % 1f
          starScroll3 = (starScroll3 + scrollBase * 1.5f) % 1f

          val curStage = stages[currentStageIndex.coerceIn(0, stages.size - 1)]

          // Check Stage Victory
          if (distanceMeters >= curStage.targetDistanceM) {
            score += 500 * (currentStageIndex + 1)
            if (score > bestScore) onSaveHighScore(score)
            if (currentStageIndex < stages.size - 1) {
              currentStageIndex++
              stageClearDialog = true
            } else {
              // Beaten all stages!
              isGameOver = true
            }
          }

          // Auto-fire laser
          if (autoFire && (nanos - lastLaserNanos) > (if (warpTimerSec > 0f) 160_000_000L else 280_000_000L)) {
            lastLaserNanos = nanos
            lasers.add(GameLaser(nextEntityId++, shipNormX - 0.025f, shipNormY - 0.04f))
            lasers.add(GameLaser(nextEntityId++, shipNormX + 0.025f, shipNormY - 0.04f))
          }

          // Move Lasers
          val laserIter = lasers.iterator()
          while (laserIter.hasNext()) {
            val l = laserIter.next()
            l.y += (l.vy / 1000f) * dt
            if (l.y < -0.05f) laserIter.remove()
          }

          // Spawn Asteroids
          spawnTimer += dt * curStage.asteroidRate * (if (warpTimerSec > 0f) 0.6f else 1f)
          if (spawnTimer >= 0.75f) {
            spawnTimer = 0f
            val isComet = currentStageIndex >= 2 && Random.nextFloat() < 0.35f
            val r = if (isComet) Random.nextFloat() * 14f + 16f else Random.nextFloat() * 26f + 20f
            val baseColor = when {
              isComet -> Color(0xFF80D8FF)
              currentStageIndex == 0 -> Color(0xFFC06C54)
              currentStageIndex == 1 -> Color(0xFFA09080)
              currentStageIndex == 2 -> Color(0xFFE6CCB2)
              else -> Color(0xFF90A4AE)
            }
            asteroids.add(
              GameAsteroid(
                id = nextEntityId++,
                x = Random.nextFloat() * 0.90f + 0.05f,
                y = -0.1f,
                vx = (Random.nextFloat() - 0.5f) * 0.12f,
                vy = (Random.nextFloat() * 0.35f + 0.30f) * (if (isComet) 1.5f else 1f) * speedMult,
                radius = r,
                rotation = Random.nextFloat() * 360f,
                rotSpeed = (Random.nextFloat() - 0.5f) * 160f,
                hp = if (r > 34f) 2 else 1,
                color = baseColor,
                isIceComet = isComet,
              ),
            )
          }

          // Move & Update Asteroids
          val astIter = asteroids.iterator()
          while (astIter.hasNext()) {
            val a = astIter.next()
            a.y += a.vy * dt
            a.x += a.vx * dt
            a.rotation = (a.rotation + a.rotSpeed * dt) % 360f

            // Off screen
            if (a.y > 1.15f || a.x < -0.15f || a.x > 1.15f) {
              astIter.remove()
              continue
            }

            // Laser collision
            val lIter = lasers.iterator()
            var destroyedByLaser = false
            while (lIter.hasNext()) {
              val l = lIter.next()
              val dx = (l.x - a.x) * 400f
              val dy = (l.y - a.y) * 800f
              if (hypot(dx, dy) < a.radius + 12f) {
                lIter.remove()
                a.hp--
                if (a.hp <= 0) {
                  destroyedByLaser = true
                  break
                }
              }
            }

            if (destroyedByLaser) {
              score += (a.radius * 2).roundToInt()
              // Spawn explosion particles
              repeat(10) {
                val ang = Random.nextFloat() * 2 * PI.toFloat()
                val spd = Random.nextFloat() * 160f + 60f
                particles.add(GameParticle(a.x, a.y, cos(ang) * spd, sin(ang) * spd, a.color, 1f, Random.nextFloat() * 3f + 2f))
              }
              // Chance to spawn drop
              if (Random.nextFloat() < 0.22f) {
                val dropType = when (Random.nextInt(4)) {
                  0 -> DropType.SHIELD
                  1 -> DropType.WARP
                  2 -> DropType.REPAIR
                  else -> DropType.STAR
                }
                drops.add(GameDrop(nextEntityId++, a.x, a.y, 0.25f, dropType))
              }
              astIter.remove()
              continue
            }

            // Ship collision
            val shipDistX = (shipNormX - a.x) * 400f
            val shipDistY = (shipNormY - a.y) * 800f
            val hitDist = hypot(shipDistX, shipDistY)

            if (hitDist < a.radius + 24f) {
              // Collision event!
              if (warpTimerSec > 0f) {
                // Smashes asteroid in warp
                score += 100
                repeat(8) {
                  val ang = Random.nextFloat() * 2 * PI.toFloat()
                  particles.add(GameParticle(a.x, a.y, cos(ang) * 120f, sin(ang) * 120f, NebulaMagenta, 1f, 3f))
                }
                astIter.remove()
              } else if (shieldTimerSec > 0f) {
                // Shield absorbs hit
                shieldTimerSec = 0.5f // brief flicker
                repeat(8) {
                  val ang = Random.nextFloat() * 2 * PI.toFloat()
                  particles.add(GameParticle(shipNormX, shipNormY, cos(ang) * 140f, sin(ang) * 140f, CosmicCyan, 1f, 3f))
                }
                astIter.remove()
              } else {
                // Player takes damage
                lives--
                shieldTimerSec = 2.5f // Invulnerability grace period
                repeat(12) {
                  val ang = Random.nextFloat() * 2 * PI.toFloat()
                  particles.add(GameParticle(shipNormX, shipNormY, cos(ang) * 160f, sin(ang) * 160f, Color(0xFFE63946), 1f, 4f))
                }
                astIter.remove()
                if (lives <= 0) {
                  isGameOver = true
                  if (score > bestScore) onSaveHighScore(score)
                }
              }
            }
          }

          // Move & Collect Drops
          val dropIter = drops.iterator()
          while (dropIter.hasNext()) {
            val d = dropIter.next()
            d.y += d.vy * dt
            if (d.y > 1.1f) {
              dropIter.remove()
              continue
            }
            val dx = (shipNormX - d.x) * 400f
            val dy = (shipNormY - d.y) * 800f
            if (hypot(dx, dy) < 32f) {
              when (d.type) {
                DropType.SHIELD -> shieldTimerSec = 8f
                DropType.WARP -> warpTimerSec = 5f
                DropType.STAR -> score += 150
                DropType.REPAIR -> lives = (lives + 1).coerceAtMost(4)
              }
              repeat(6) {
                val ang = Random.nextFloat() * 2 * PI.toFloat()
                particles.add(GameParticle(d.x, d.y, cos(ang) * 90f, sin(ang) * 90f, CosmicAmber, 1f, 2.5f))
              }
              dropIter.remove()
            }
          }

          // Update Particles
          val partIter = particles.iterator()
          while (partIter.hasNext()) {
            val p = partIter.next()
            p.x += (p.vx / 400f) * dt
            p.y += (p.vy / 800f) * dt
            p.alpha -= dt * 1.8f
            if (p.alpha <= 0f) partIter.remove()
          }
        }
        lastFrameNanos = nanos
      }
    }
  }

  Box(
    modifier = modifier.fillMaxSize().background(SpaceVoid).testTag("asteroid_dodge_screen"),
  ) {
    val curStage = stages[currentStageIndex.coerceIn(0, stages.size - 1)]

    // 1. Arcade Space Canvas
    Canvas(
      modifier =
        Modifier.fillMaxSize()
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              if (isPlaying && !isGameOver && !stageClearDialog) {
                shipNormX = (shipNormX + dragAmount.x / size.width).coerceIn(0.06f, 0.94f)
                shipNormY = (shipNormY + dragAmount.y / size.height).coerceIn(0.12f, 0.92f)
              }
            }
          }
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              if (isPlaying && !isGameOver && !stageClearDialog) {
                // Tap steers immediately toward tap location or fires extra burst
                lasers.add(GameLaser(nextEntityId++, shipNormX - 0.03f, shipNormY - 0.05f))
                lasers.add(GameLaser(nextEntityId++, shipNormX + 0.03f, shipNormY - 0.05f))
              }
            }
          },
    ) {
      val w = size.width
      val h = size.height

      // Deep space radial background
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF060B1C), Color(0xFF0C142E), SpaceVoid),
        ),
      )

      // Parallax Stars
      fun drawStarLayer(stars: List<Pair<Offset, Float>>, scroll: Float, starSize: Float) {
        stars.forEach { (pos, b) ->
          val y = ((pos.y + scroll) % 1f) * h
          val x = pos.x * w
          drawCircle(
            color = Color.White.copy(alpha = b),
            radius = starSize,
            center = Offset(x, y),
          )
        }
      }
      drawStarLayer(starsLayer1, starScroll1, 1.2f)
      drawStarLayer(starsLayer2, starScroll2, 1.8f)
      drawStarLayer(starsLayer3, starScroll3, 2.5f)

      // Approaching Destination Planet in the distance (grows as stage advances)
      val stageProgress = (distanceMeters / curStage.targetDistanceM).coerceIn(0f, 1f)
      val destPlanetR = w * (0.12f + stageProgress * 0.18f)
      val destPlanetCenter = Offset(w * 0.5f, -destPlanetR * 0.25f + stageProgress * (h * 0.15f))
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(curStage.toPlanetColor, curStage.toPlanetColor.copy(alpha = 0.5f), Color.Transparent),
          center = destPlanetCenter,
          radius = destPlanetR * 1.35f,
        ),
        radius = destPlanetR * 1.35f,
        center = destPlanetCenter,
      )
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(curStage.toPlanetColor, Color(0xFF1A1A24)),
          center = Offset(destPlanetCenter.x - destPlanetR * 0.25f, destPlanetCenter.y - destPlanetR * 0.25f),
          radius = destPlanetR,
        ),
        radius = destPlanetR,
        center = destPlanetCenter,
      )

      // Draw Drops / PowerUps
      drops.forEach { d ->
        val dropCenter = Offset(d.x * w, d.y * h)
        val dropColor = when (d.type) {
          DropType.SHIELD -> CosmicCyan
          DropType.WARP -> NebulaMagenta
          DropType.STAR -> CosmicAmber
          DropType.REPAIR -> AuroraGreen
        }
        drawCircle(color = dropColor.copy(alpha = 0.35f), radius = 18f, center = dropCenter)
        drawCircle(color = dropColor, radius = 12f, center = dropCenter)
        drawCircle(color = StarlightWhite, radius = 5f, center = dropCenter)
      }

      // Draw Lasers
      lasers.forEach { l ->
        val lx = l.x * w
        val ly = l.y * h
        drawLine(
          color = CosmicCyan,
          start = Offset(lx, ly),
          end = Offset(lx, ly + 22f),
          strokeWidth = 4.5f,
          cap = StrokeCap.Round,
        )
        drawLine(
          color = Color.White,
          start = Offset(lx, ly),
          end = Offset(lx, ly + 14f),
          strokeWidth = 2f,
          cap = StrokeCap.Round,
        )
      }

      // Draw Asteroids
      asteroids.forEach { a ->
        val ax = a.x * w
        val ay = a.y * h
        val rad = a.radius

        if (a.isIceComet) {
          // Comet Tail
          drawLine(
            brush = Brush.verticalGradient(listOf(Color(0xFF80D8FF).copy(alpha = 0.7f), Color.Transparent), startY = ay - rad, endY = ay - rad * 3f),
            start = Offset(ax, ay - rad),
            end = Offset(ax, ay - rad * 3.5f),
            strokeWidth = rad * 1.1f,
            cap = StrokeCap.Round,
          )
        }

        // Rocky Polygon Asteroid with craters
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(a.color, Color(0xFF1E212D)),
            center = Offset(ax - rad * 0.3f, ay - rad * 0.3f),
            radius = rad,
          ),
          radius = rad,
          center = Offset(ax, ay),
        )
        drawCircle(
          color = ComicInk,
          radius = rad,
          center = Offset(ax, ay),
          style = Stroke(width = 2.2f),
        )
        // Little craters
        drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = rad * 0.25f, center = Offset(ax + rad * 0.25f, ay - rad * 0.2f))
        drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = rad * 0.20f, center = Offset(ax - rad * 0.3f, ay + rad * 0.25f))
      }

      // Draw Explosion Particles
      particles.forEach { p ->
        drawCircle(
          color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
          radius = p.radius,
          center = Offset(p.x * w, p.y * h),
        )
      }

      // Draw Player Spaceship
      val shipX = shipNormX * w
      val shipY = shipNormY * h

      // Twin Plasma Exhaust Flames
      val flameH = if (warpTimerSec > 0f) 46f else 24f + (sin((distanceMeters * 0.8f).toDouble()).toFloat() * 6f)
      val leftExhaust = Offset(shipX - 11f, shipY + 22f)
      val rightExhaust = Offset(shipX + 11f, shipY + 22f)
      val flameColor = if (warpTimerSec > 0f) NebulaMagenta else CosmicCyan

      drawLine(color = flameColor, start = leftExhaust, end = Offset(leftExhaust.x, leftExhaust.y + flameH), strokeWidth = 5f, cap = StrokeCap.Round)
      drawLine(color = Color.White, start = leftExhaust, end = Offset(leftExhaust.x, leftExhaust.y + flameH * 0.6f), strokeWidth = 2.5f, cap = StrokeCap.Round)
      drawLine(color = flameColor, start = rightExhaust, end = Offset(rightExhaust.x, rightExhaust.y + flameH), strokeWidth = 5f, cap = StrokeCap.Round)
      drawLine(color = Color.White, start = rightExhaust, end = Offset(rightExhaust.x, rightExhaust.y + flameH * 0.6f), strokeWidth = 2.5f, cap = StrokeCap.Round)

      // Spaceship Hull Path
      val shipPath = Path().apply {
        moveTo(shipX, shipY - 26f) // Nose
        lineTo(shipX + 14f, shipY + 8f) // Right body
        lineTo(shipX + 24f, shipY + 20f) // Right wingtip
        lineTo(shipX + 12f, shipY + 22f) // Right engine
        lineTo(shipX, shipY + 16f) // Center tail
        lineTo(shipX - 12f, shipY + 22f) // Left engine
        lineTo(shipX - 24f, shipY + 20f) // Left wingtip
        lineTo(shipX - 14f, shipY + 8f) // Left body
        close()
      }
      drawPath(
        path = shipPath,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFF4A90E2), Color(0xFF1E3A8A), Color(0xFF0F172A)),
          startY = shipY - 26f,
          endY = shipY + 22f,
        ),
      )
      drawPath(path = shipPath, color = ComicInk, style = Stroke(width = 2.2f))

      // Cockpit Canopy on Ship
      drawOval(
        brush = Brush.radialGradient(listOf(Color(0xFFE0F2FE), CosmicCyan), center = Offset(shipX, shipY - 6f), radius = 10f),
        topLeft = Offset(shipX - 5.5f, shipY - 14f),
        size = Size(11f, 18f),
      )

      // Shield Aura
      if (shieldTimerSec > 0f) {
        val shieldR = 34f
        drawCircle(
          color = CosmicCyan.copy(alpha = 0.35f),
          radius = shieldR,
          center = Offset(shipX, shipY),
        )
        drawCircle(
          color = CosmicCyan,
          radius = shieldR,
          center = Offset(shipX, shipY),
          style = Stroke(width = 2.4f),
        )
      }

      // Warp Aura
      if (warpTimerSec > 0f) {
        drawCircle(
          brush = Brush.radialGradient(listOf(Color.Transparent, NebulaMagenta.copy(alpha = 0.5f)), center = Offset(shipX, shipY), radius = 42f),
          radius = 42f,
          center = Offset(shipX, shipY),
          style = Stroke(width = 3.5f),
        )
      }
    }

    // 2. Top HUD Bar (Score, Health, Stage Progress, Exit)
    Column(
      modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Back / Exit button
        Surface(
          onClick = onExitGame,
          color = SpaceNavy.copy(alpha = 0.88f),
          shape = CircleShape,
          border = BorderStroke(1.2.dp, CosmicCyan.copy(alpha = 0.5f)),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Esci dal gioco",
            tint = StarlightWhite,
            modifier = Modifier.padding(8.dp).size(20.dp),
          )
        }

        // Score & High Score
        Surface(
          color = SpaceNavy.copy(alpha = 0.88f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.2.dp, CosmicAmber.copy(alpha = 0.6f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "PUNTI: $score",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = CosmicAmber,
            )
            Text(
              text = "BEST: ${bestScore.coerceAtLeast(score)}",
              style = MaterialTheme.typography.labelSmall,
              color = StarlightMuted,
            )
          }
        }

        // Lives (Hearts)
        Surface(
          color = SpaceNavy.copy(alpha = 0.88f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.2.dp, Color(0xFFE63946).copy(alpha = 0.6f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            repeat(lives) {
              Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                tint = Color(0xFFE63946),
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }

      // Stage Progress Bar (Terra ---> Marte)
      Surface(
        color = Color(0xD90B1124),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(curStage.fromPlanet, style = MaterialTheme.typography.labelSmall, color = StarlightMuted)
          Row(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val prog = (distanceMeters / curStage.targetDistanceM).coerceIn(0f, 1f)
            Box(
              modifier = Modifier.fillMaxWidth().height(6.dp).background(SpaceNavy, RoundedCornerShape(3.dp)),
            ) {
              Box(
                modifier = Modifier.fillMaxWidth(prog).height(6.dp).background(
                  Brush.horizontalGradient(listOf(CosmicCyan, AuroraGreen, curStage.toPlanetColor)),
                  RoundedCornerShape(3.dp),
                ),
              )
            }
          }
          Text(curStage.toPlanet, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = curStage.toPlanetColor)
        }
      }
    }

    // 3. Start / Title Overlay
    if (!isPlaying && !isGameOver) {
      Surface(
        modifier = Modifier.align(Alignment.Center).padding(20.dp),
        color = Color(0xF20B1124),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, CosmicCyan),
        shadowElevation = 18.dp,
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          ChibiAvatar(profile = chibiProfile, expression = ChibiExpression.CHEERING, size = 96.dp)

          Text(
            text = "🚀 Corsa Galattica",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CosmicAmber,
          )
          Text(
            text = "Scansa gli asteroidi e viaggia tra i pianeti del Sistema Solare! Raccogli scudi, turbo curvatura e distruggi i meteoriti con i laser al plasma.",
            style = MaterialTheme.typography.bodyMedium,
            color = StarlightWhite,
            modifier = Modifier.width(280.dp),
          )

          Button(
            onClick = { restartGame() },
            colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(48.dp).fillMaxWidth(),
          ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Decollo Immediato!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 4. Stage Victory Celebration Overlay
    if (stageClearDialog) {
      Surface(
        modifier = Modifier.align(Alignment.Center).padding(20.dp),
        color = Color(0xF20B1124),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, AuroraGreen),
        shadowElevation = 18.dp,
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          ChibiAvatar(profile = chibiProfile, expression = ChibiExpression.AMAZED, size = 96.dp)

          Text(
            text = "🎉 Pianeta Raggiunto!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AuroraGreen,
          )
          Text(
            text = "Hai superato la rotta ed hai raggiunto con successo ${curStage.toPlanet}!\n+500 Punti Bonus!",
            style = MaterialTheme.typography.bodyMedium,
            color = StarlightWhite,
            modifier = Modifier.width(280.dp),
          )

          Button(
            onClick = {
              stageClearDialog = false
              distanceMeters = 0f
              shieldTimerSec = 3f
            },
            colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(48.dp).fillMaxWidth(),
          ) {
            Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Continua verso la Prossima Tappa!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 5. Game Over Overlay
    if (isGameOver) {
      Surface(
        modifier = Modifier.align(Alignment.Center).padding(20.dp),
        color = Color(0xF20B1124),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, Color(0xFFE63946)),
        shadowElevation = 18.dp,
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          ChibiAvatar(profile = chibiProfile, expression = ChibiExpression.THINKING, size = 90.dp)

          Text(
            text = "💥 Missione Terminata",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE63946),
          )

          Surface(
            color = SpaceNavy,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Text("PUNTEGGIO FINALE", style = MaterialTheme.typography.labelSmall, color = StarlightMuted)
              Text("$score", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CosmicAmber)
              if (score >= bestScore && score > 0) {
                Text("🏆 NUOVO RECORD GALATTICO!", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AuroraGreen)
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = { restartGame() },
              colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f).height(46.dp),
            ) {
              Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Riprova", fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = onExitGame,
              colors = ButtonDefaults.buttonColors(containerColor = SpaceCard, contentColor = StarlightWhite),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f).height(46.dp),
            ) {
              Text("Esci")
            }
          }
        }
      }
    }
  }
}
