package com.ricrug.ui.components

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricrug.data.OfficialPhotoRecord
import com.ricrug.data.PlanetEnvironmentConfig
import com.ricrug.data.SurfaceExplorationRepository
import com.ricrug.data.SurfacePoi
import com.ricrug.data.SurfaceSample
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.FredokaFontFamily
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightWhite
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private data class SurfaceDustParticle(
  var x: Float,
  var y: Float,
  var z: Float,
  var vx: Float,
  var vy: Float,
  var vz: Float,
  var life: Float,
  var maxLife: Float,
)

/**
 * Simulatore 3D in Prima Persona per lo Sbarco e l'Esplorazione Planetaria (EVA).
 * Integra fisica della gravità realistica specifica per pianeta, salti lunari/marziani,
 * scanner spettrometrico, raccolta campioni minerali e punti di interesse storici documentati.
 */
@Composable
fun SurfaceFirstPersonViewer3D(
  planetId: String,
  chibiProfile: ChibiProfile,
  onInspectPoi: (SurfacePoi, OfficialPhotoRecord?) -> Unit,
  onCollectSample: (SurfaceSample, OfficialPhotoRecord?) -> Unit,
  onOpenPhotoGallery: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val env = remember(planetId) { SurfaceExplorationRepository.getEnvironmentConfig(planetId) }
  val pois = remember(planetId) { SurfaceExplorationRepository.getPoisForPlanet(planetId) }
  val samples = remember(planetId) { SurfaceExplorationRepository.getSamplesForPlanet(planetId) }
  val collectedSampleIds = remember { mutableStateListOf<String>() }
  val discoveredPoiIds = remember { mutableStateListOf<String>() }

  // Rocce e massi 3D sparsi proceduralmente sul terreno per dare orientamento e scala reale
  val proceduralRocks = remember {
    listOf(
      Triple(-8f, 12f, 0.45f), Triple(10f, 15f, 0.65f), Triple(-15f, 22f, 0.85f), Triple(18f, 8f, 0.40f),
      Triple(-5f, 28f, 0.70f), Triple(12f, 35f, 0.90f), Triple(-22f, 14f, 0.55f), Triple(25f, 26f, 0.80f),
      Triple(-12f, -8f, 0.50f), Triple(14f, -12f, 0.60f), Triple(-20f, -18f, 0.75f), Triple(8f, -22f, 0.55f),
      Triple(-30f, 32f, 1.10f), Triple(32f, 18f, 1.25f), Triple(-4f, 40f, 0.85f), Triple(16f, 44f, 0.95f),
      Triple(-26f, 48f, 1.30f), Triple(28f, 42f, 1.15f), Triple(-10f, 55f, 1.40f), Triple(6f, 60f, 1.20f),
      Triple(-35f, 10f, 0.80f), Triple(38f, -8f, 0.90f), Triple(-18f, -28f, 0.70f), Triple(22f, -32f, 0.85f)
    )
  }

  // Crateri d'impatto volumetrici 3D sul suolo del pianeta
  val proceduralCraters = remember {
    listOf(
      Triple(-14f, 20f, 3.2f),
      Triple(16f, 28f, 4.5f),
      Triple(-28f, 38f, 6.0f),
      Triple(24f, 15f, 2.8f),
      Triple(-8f, 45f, 5.2f),
      Triple(30f, 50f, 7.5f),
      Triple(-22f, -14f, 3.8f),
      Triple(18f, -20f, 4.2f)
    )
  }

  // Stato sbarco: true se inizialmente a bordo sul portellone dell'astronave, false dopo essere scesi sul suolo
  var isInsideShip by remember(planetId) { mutableStateOf(true) }

  // Posizione e Fisica Giocatore in Prima Persona (Coordinate in metri dal modulo astronave 0,0)
  var playerX by remember(planetId) { mutableFloatStateOf(0f) }
  var playerZ by remember(planetId) { mutableFloatStateOf(1.6f) } // inizia al portellone dell'astronave
  var playerY by remember(planetId) { mutableFloatStateOf(3.2f) } // quota elevata portellone (3.2m da terra)
  var verticalVelocity by remember { mutableFloatStateOf(0f) }
  var isGrounded by remember { mutableStateOf(true) }

  // Orientamento sguardo della tuta
  var playerYawDeg by remember(planetId) { mutableFloatStateOf(0f) } // guarda verso l'esterno
  var playerPitchDeg by remember(planetId) { mutableFloatStateOf(-8f) } // sguardo verso la scaletta e il suolo

  // Stick di movimento analogico virtuale
  var stickActive by remember { mutableStateOf(false) }
  var stickDeltaX by remember { mutableFloatStateOf(0f) }
  var stickDeltaY by remember { mutableFloatStateOf(0f) }

  // Inerzia fisica del movimento e particelle
  var velocityX by remember { mutableFloatStateOf(0f) }
  var velocityZ by remember { mutableFloatStateOf(0f) }
  val dustParticles = remember { mutableStateListOf<SurfaceDustParticle>() }

  // Passo e oscillazione (head bobbing)
  var walkCycle by remember { mutableFloatStateOf(0f) }
  var oxygenLevel by remember { mutableFloatStateOf(100f) }
  var simTimeSec by remember { mutableFloatStateOf(0f) }

  // Bersaglio attualmente agganciato nel mirino
  var activeTargetPoi by remember { mutableStateOf<SurfacePoi?>(null) }
  var activeTargetSample by remember { mutableStateOf<SurfaceSample?>(null) }
  var targetDistanceMeters by remember { mutableFloatStateOf(0f) }

  // Azione per scendere dall'astronave sul suolo del pianeta
  val onDisembark = {
    isInsideShip = false
    playerX = 0f
    playerZ = 5.2f
    playerY = 1.75f
    playerYawDeg = 180f // Si volta di 180° per guardare e ammirare l'astronave dall'esterno!
    playerPitchDeg = 8f
    isGrounded = true
  }

  // Azione per risalire a bordo dell'astronave
  val onBoardShip = {
    isInsideShip = true
    playerX = 0f
    playerZ = 1.6f
    playerY = 3.2f
    playerYawDeg = 0f
    playerPitchDeg = -8f
    isGrounded = true
  }

  val shipDist = sqrt(playerX * playerX + playerZ * playerZ)

  val textMeasurer = rememberTextMeasurer()

  // Loop Fisico e Animazione a 60 FPS
  LaunchedEffect(planetId) {
    var lastNanoTime = 0L
    while (true) {
      withFrameNanos { nowNanos ->
        if (lastNanoTime == 0L) lastNanoTime = nowNanos
        val dt = ((nowNanos - lastNanoTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastNanoTime = nowNanos
        simTimeSec += dt

        // Consumo ossigeno graduale
        oxygenLevel = (oxygenLevel - dt * 0.012f).coerceIn(0f, 100f)

        // 1. Movimento Orizzontale con Inerzia Fisica della Tuta Spaziale
        val isLowGravity = env.gravityRatio < 0.6f
        val maxWalkSpeed = if (isGrounded) (if (isLowGravity) 4.2f else 3.6f) else 2.5f
        val yawRad = playerYawDeg * PI.toFloat() / 180f
        val fwdX = sin(yawRad)
        val fwdZ = cos(yawRad)
        val rightX = cos(yawRad)
        val rightZ = -sin(yawRad)

        var targetVX = 0f
        var targetVZ = 0f

        if (stickActive && !isInsideShip) {
          val moveMag = sqrt(stickDeltaX * stickDeltaX + stickDeltaY * stickDeltaY).coerceIn(0f, 1f)
          if (moveMag > 0.04f) {
            val moveDirX = rightX * stickDeltaX + fwdX * (-stickDeltaY)
            val moveDirZ = rightZ * stickDeltaX + fwdZ * (-stickDeltaY)
            targetVX = moveDirX * maxWalkSpeed
            targetVZ = moveDirZ * maxWalkSpeed

            if (isGrounded) {
              val hopFreq = if (isLowGravity) 5.8f else 7.2f
              walkCycle += dt * hopFreq * moveMag
            }
          }
        }

        // Rampa di accelerazione e inerzia progressiva
        val accelRate = if (isGrounded) (if (isLowGravity) 8.5f else 12f) else 3.0f
        velocityX += (targetVX - velocityX) * (accelRate * dt).coerceIn(0f, 1f)
        velocityZ += (targetVZ - velocityZ) * (accelRate * dt).coerceIn(0f, 1f)

        playerX += velocityX * dt
        playerZ += velocityZ * dt

        // Particelle di regolite sollevate dai passi sul suolo
        val currentHorizSpeed = sqrt(velocityX * velocityX + velocityZ * velocityZ)
        if (isGrounded && currentHorizSpeed > 0.6f && (simTimeSec * 14).toInt() != ((simTimeSec - dt) * 14).toInt()) {
          val pSide = if (sin(walkCycle) > 0f) -0.32f else 0.32f
          dustParticles.add(
            SurfaceDustParticle(
              x = playerX + rightX * pSide + (cos(simTimeSec * 19f) * 0.12f),
              y = 0.04f,
              z = playerZ + rightZ * pSide + (sin(simTimeSec * 19f) * 0.12f),
              vx = -velocityX * 0.20f + (cos(simTimeSec * 31f) * 0.35f),
              vy = 0.7f + (env.gravityRatio * 0.35f),
              vz = -velocityZ * 0.20f + (sin(simTimeSec * 31f) * 0.35f),
              life = 0f,
              maxLife = if (isLowGravity) 1.25f else 0.7f,
            )
          )
          if (dustParticles.size > 24) dustParticles.removeAt(0)
        }

        val pIter = dustParticles.iterator()
        while (pIter.hasNext()) {
          val p = pIter.next()
          p.life += dt
          if (p.life >= p.maxLife) {
            pIter.remove()
          } else {
            p.vy -= env.gravityMs2 * 0.55f * dt
            p.x += p.vx * dt
            p.y = (p.y + p.vy * dt).coerceAtLeast(0.01f)
            p.z += p.vz * dt
          }
        }

        // 2. Fisica Gravitazionale Realistica (Salto e Caduta libera)
        if (!isGrounded) {
          verticalVelocity -= env.gravityMs2 * dt
          playerY += verticalVelocity * dt
          if (playerY <= 1.75f) {
            playerY = 1.75f
            verticalVelocity = 0f
            isGrounded = true
          }
        }

        // 3. Rilevamento Prossimità e Lock-On Bersagli nel Mirino
        var closestSample: SurfaceSample? = null
        var closestSampleDist = 999f

        for (sample in samples) {
          if (sample.id in collectedSampleIds) continue
          val dx = sample.posX - playerX
          val dz = sample.posZ - playerZ
          val dist = sqrt(dx * dx + dz * dz)
          if (dist < 5.5f) {
            // Controlla se è nel cono visivo frontale (~60°)
            val dot = (dx * fwdX + dz * fwdZ) / max(0.1f, dist)
            if (dot > 0.65f && dist < closestSampleDist) {
              closestSampleDist = dist
              closestSample = sample
            }
          }
        }

        var closestPoi: SurfacePoi? = null
        var closestPoiDist = 999f

        for (poi in pois) {
          val dx = poi.posX - playerX
          val dz = poi.posZ - playerZ
          val dist = sqrt(dx * dx + dz * dz)
          if (dist < 8.0f) {
            val dot = (dx * fwdX + dz * fwdZ) / max(0.1f, dist)
            if (dot > 0.60f && dist < closestPoiDist) {
              closestPoiDist = dist
              closestPoi = poi
            }
          }
        }

        if (closestSample != null) {
          activeTargetSample = closestSample
          activeTargetPoi = null
          targetDistanceMeters = closestSampleDist
        } else if (closestPoi != null) {
          activeTargetPoi = closestPoi
          activeTargetSample = null
          targetDistanceMeters = closestPoiDist
        } else {
          activeTargetSample = null
          activeTargetPoi = null
          targetDistanceMeters = 0f
        }
      }
    }
  }

  // Tasto Salto ad impulso gravitazionale (proporzionato al pianeta)
  val onTriggerJump = {
    if (isGrounded) {
      isGrounded = false
      // Sulla Luna impulso morbido per fluttuare in alto, su Venere serve maggior spinta
      val jumpForce = when {
        env.gravityMs2 < 2.0f -> 4.2f // Luna: salti spettacolari di oltre 3 metri!
        env.gravityMs2 < 5.0f -> 4.8f // Marte/Mercurio: salti agili
        else -> 5.2f // Terra/Venere: balzo standard
      }
      verticalVelocity = jumpForce
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          // Swipe per orientare lo sguardo a 360°
          detectDragGestures { change, dragAmount ->
            change.consume()
            playerYawDeg = (playerYawDeg + dragAmount.x * 0.18f + 360f) % 360f
            playerPitchDeg = (playerPitchDeg - dragAmount.y * 0.15f).coerceIn(-50f, 50f)
          }
        },
    ) {
      val canvasW = size.width
      val canvasH = size.height

      // Head bobbing e balzo a gravità ridotta (lunar hop) durante la camminata
      val isLowGravity = env.gravityRatio < 0.6f
      val currentHorizSpeed = sqrt(velocityX * velocityX + velocityZ * velocityZ)
      val hopHeight = if (isLowGravity) 0.12f else 0.05f
      val bobbingOffset = if (isGrounded && currentHorizSpeed > 0.1f) abs(sin(walkCycle)) * hopHeight else 0f
      val currentEyeY = playerY + bobbingOffset

      // Oscillazione di rollio laterale durante la camminata / balzo
      val rollSway = if (isGrounded && currentHorizSpeed > 0.1f) sin(walkCycle * 0.5f) * (if (isLowGravity) 1.6f else 0.7f) else 0f

      // Fattore prospettico FOV e scala angolare per allineamento 3D perfetto del cielo
      val fovFactor = canvasW * 0.95f
      val pxPerDeg = fovFactor * (PI.toFloat() / 180f)

      // Linea dell'Orizzonte prospettica basata sul Pitch dello sguardo
      val horizonY = canvasH * 0.5f + playerPitchDeg * 7.5f

      rotate(degrees = rollSway, pivot = Offset(canvasW * 0.5f, canvasH * 0.5f)) {
        // =========================================================================
        // 1. SKYBOX & PANORAMA PLANETARIO OLTRE L'ORIZZONTE
        // =========================================================================
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(env.skyTopColor, env.skyHorizonColor),
            startY = 0f,
            endY = max(1f, horizonY),
          ),
          topLeft = Offset.Zero,
          size = Size(canvasW, max(1f, horizonY)),
        )

        // 1.1 Campo Stellare Iper-Dettagliato (Sincronizzato a 360° con la Rotazione)
        val starAlpha = when {
          env.pressureAtm == 0f -> 0.90f // Vuoto cosmico lunare o mercuriano
          env.pressureAtm < 0.05f -> 0.45f // Atmosfera sottile marziana
          else -> 0.10f // Venere / Terra
        }
        if (starAlpha > 0.08f) {
          for (i in 0 until 90) {
            val starAzimuth = (i * 37.7f) % 360f
            val starElevation = 18f + (i * 39f) % 240f
            val starRelYaw = ((starAzimuth - playerYawDeg + 540f) % 360f) - 180f
            if (abs(starRelYaw) < 85f) {
              val sx = canvasW * 0.5f + starRelYaw * pxPerDeg
              val sy = horizonY - starElevation
              if (sy > 0f) {
                val isBright = i % 7 == 0
                drawCircle(
                  color = if (isBright) CosmicCyan.copy(alpha = starAlpha) else Color.White.copy(alpha = starAlpha * (0.5f + (i % 3) * 0.25f)),
                  radius = if (isBright) 2.4f else 1.2f,
                  center = Offset(sx, sy),
                )
              }
            }
          }
        }

        // 1.2 Sole 3D Proporzionato alla Vera Distanza Orbitale
        val sunAzimuth = 180f
        val sunRelYaw = ((sunAzimuth - playerYawDeg + 540f) % 360f) - 180f
        if (abs(sunRelYaw) < 85f) {
          val sunX = canvasW * 0.5f + sunRelYaw * pxPerDeg
          val sunY = horizonY - 140f
          val sunBaseR = (22f * env.sunScale).coerceIn(8f, 95f)

          // Corona Solare Radiante
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(Color(0xFFFFFBEB), CosmicAmber.copy(alpha = 0.55f), Color.Transparent),
              center = Offset(sunX, sunY),
              radius = sunBaseR * 3.2f,
            ),
            radius = sunBaseR * 3.2f,
            center = Offset(sunX, sunY),
          )
          drawCircle(color = Color.White, radius = sunBaseR, center = Offset(sunX, sunY))

          // Raggi solari su mondi senza atmosfera (Mercurio / Luna)
          if (env.pressureAtm == 0f) {
            for (ray in 0 until 6) {
              val rAngle = (ray * 60f + simTimeSec * 4f) * PI.toFloat() / 180f
              drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(sunX + cos(rAngle) * sunBaseR, sunY + sin(rAngle) * sunBaseR),
                end = Offset(sunX + cos(rAngle) * sunBaseR * 2.2f, sunY + sin(rAngle) * sunBaseR * 2.2f),
                strokeWidth = 2.0f,
              )
            }
          }
        }

        // 1.3 Corpi Celesti Monumentali Visibili nel Cielo (Sincronizzati a 360°)
        val celestialAzimuth = 70f
        val celestialRelYaw = ((celestialAzimuth - playerYawDeg + 540f) % 360f) - 180f
        val celestialInSkyX = canvasW * 0.5f + celestialRelYaw * pxPerDeg
        val celestialInSkyY = horizonY - 185f
        val isCelestialVisible = abs(celestialRelYaw) < 95f

        if (isCelestialVisible) {
          if (planetId.lowercase() == "moon") {
            // 🌍 LA TERRA (THE BLUE MARBLE) GIGANTE DALLA LUNA
            val earthR = 68f
            // Alone atmosferico azzurro
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.6f), Color.Transparent),
                center = Offset(celestialInSkyX, celestialInSkyY),
                radius = earthR * 1.35f,
              ),
              radius = earthR * 1.35f,
              center = Offset(celestialInSkyX, celestialInSkyY),
            )
            // Oceano azzurro profondo
            drawCircle(color = Color(0xFF1D4ED8), radius = earthR, center = Offset(celestialInSkyX, celestialInSkyY))
            // Continenti verdi/ocra (Africa / Eurasia)
            drawCircle(color = Color(0xFF15803D), radius = earthR * 0.55f, center = Offset(celestialInSkyX - earthR * 0.2f, celestialInSkyY - earthR * 0.15f))
            drawCircle(color = Color(0xFFD97706), radius = earthR * 0.35f, center = Offset(celestialInSkyX + earthR * 0.3f, celestialInSkyY + earthR * 0.2f))
            // Vortici di nuvole bianche
            drawOval(
              color = Color.White.copy(alpha = 0.75f),
              topLeft = Offset(celestialInSkyX - earthR * 0.7f, celestialInSkyY - earthR * 0.35f),
              size = Size(earthR * 1.4f, earthR * 0.35f),
            )
            drawOval(
              color = Color.White.copy(alpha = 0.65f),
              topLeft = Offset(celestialInSkyX - earthR * 0.5f, celestialInSkyY + earthR * 0.15f),
              size = Size(earthR * 1.1f, earthR * 0.25f),
            )
          } else if (planetId.lowercase() == "jupiter") {
            // 🪐 GIOVE COLOSSALE CHE DOMINA IL CIELO DI EUROPA
            val jupR = 105f
            drawCircle(color = Color(0xFFC2410C), radius = jupR, center = Offset(celestialInSkyX, celestialInSkyY))
            // Fasce nuvolose animate zonate
            for (b in -3..3) {
              val bCol = if (b % 2 == 0) Color(0xFF78350F) else Color(0xFFD97706)
              drawLine(
                color = bCol.copy(alpha = 0.85f),
                start = Offset(celestialInSkyX - jupR * 0.95f, celestialInSkyY + b * 24f),
                end = Offset(celestialInSkyX + jupR * 0.95f, celestialInSkyY + b * 24f),
                strokeWidth = 14f,
              )
            }
            // Grande Macchia Rossa
            drawOval(
              color = Color(0xFFB91C1C),
              topLeft = Offset(celestialInSkyX + jupR * 0.25f, celestialInSkyY + 18f),
              size = Size(34f, 20f),
            )
          } else if (planetId.lowercase() == "mars") {
            // Luna marziana Phobos che sfreccia nel cielo
            drawCircle(color = Color(0xFF94A3B8), radius = 9f, center = Offset(celestialInSkyX, celestialInSkyY))
            drawCircle(color = Color(0xFF475569), radius = 3.5f, center = Offset(celestialInSkyX - 2f, celestialInSkyY - 1f))
          } else if (planetId.lowercase() == "saturn") {
            // Saturno con anelli dorati inclinati
            val satR = 54f
            drawCircle(color = Color(0xFFD97706), radius = satR, center = Offset(celestialInSkyX, celestialInSkyY))
            // Anelli dorati ellittici inclinati
            rotate(degrees = -25f, pivot = Offset(celestialInSkyX, celestialInSkyY)) {
              drawOval(
                color = Color(0xFFFDE68A).copy(alpha = 0.75f),
                topLeft = Offset(celestialInSkyX - satR * 2.1f, celestialInSkyY - satR * 0.45f),
                size = Size(satR * 4.2f, satR * 0.9f),
                style = Stroke(width = 8f),
              )
            }
          }
        }

        // 1.4 Catene Montuose a 3 Livelli di Profondità (Parallasse Panoramico)
        val farOffset = -playerYawDeg * pxPerDeg * 0.35f
        val distantMountainPath = Path().apply {
          moveTo(0f, horizonY)
          for (x in 0..canvasW.toInt() step 25) {
            val phase = (x - farOffset) * 0.0075f
            val dH = (sin(phase) * 55f + cos(phase * 2.1f) * 28f + sin(phase * 4.3f) * 12f).coerceAtLeast(0f)
            lineTo(x.toFloat(), horizonY - dH)
          }
          lineTo(canvasW, horizonY)
          close()
        }
        drawPath(
          path = distantMountainPath,
          color = env.terrainSecondaryColor.copy(alpha = 0.45f),
        )

        val midOffset = -playerYawDeg * pxPerDeg * 0.65f
        val midHillsPath = Path().apply {
          moveTo(0f, horizonY)
          for (x in 0..canvasW.toInt() step 20) {
            val phase = (x - midOffset) * 0.014f
            val mH = (sin(phase) * 32f + cos(phase * 2.8f) * 18f).coerceAtLeast(0f)
            lineTo(x.toFloat(), horizonY - mH)
          }
          lineTo(canvasW, horizonY)
          close()
        }
        drawPath(
          path = midHillsPath,
          color = env.terrainSecondaryColor.copy(alpha = 0.75f),
        )

        val nearOffset = -playerYawDeg * pxPerDeg
        val nearHorizonPath = Path().apply {
          moveTo(0f, horizonY)
          for (x in 0..canvasW.toInt() step 15) {
            val phase = (x - nearOffset) * 0.022f
            val nH = (sin(phase) * 14f + cos(phase * 3.5f) * 8f).coerceAtLeast(0f)
            lineTo(x.toFloat(), horizonY - nH)
          }
          lineTo(canvasW, horizonY)
          close()
        }
        drawPath(
          path = nearHorizonPath,
          color = env.terrainSecondaryColor,
        )

        // =========================================================================
        // 2. TERRENO SUPERFICIALE PROSPETTICO 3D NAVIGABILE
        // =========================================================================
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(env.skyHorizonColor, env.terrainSecondaryColor, env.terrainPrimaryColor),
            startY = horizonY,
            endY = canvasH,
          ),
          topLeft = Offset(0f, horizonY),
          size = Size(canvasW, canvasH - horizonY),
        )

      val yawRad = playerYawDeg * PI.toFloat() / 180f
      val cosYaw = cos(yawRad)
      val sinYaw = sin(yawRad)

      // Proiezione Prospettica da Coordinate Mondiali (X, Z, Y) in Pixel Schermo
      fun projectWorldToScreen(worldX: Float, worldZ: Float, worldY: Float = 0f): Offset? {
        val relX = worldX - playerX
        val relZ = worldZ - playerZ
        val camX = relX * cosYaw - relZ * sinYaw
        val camZ = relX * sinYaw + relZ * cosYaw

        if (camZ <= 0.35f) return null
        val fovFactor = canvasW * 0.95f
        val screenX = canvasW * 0.5f + (camX / camZ) * fovFactor
        val screenY = horizonY + ((currentEyeY - worldY) / camZ) * fovFactor
        return Offset(screenX, screenY)
      }

      // Funzione di utilità per tracciare segmenti del terreno in coordinate mondiali con clipping frontale
      fun drawWorldSegment(wX1: Float, wZ1: Float, wX2: Float, wZ2: Float, color: Color, strokeWidth: Float) {
        val relX1 = wX1 - playerX
        val relZ1 = wZ1 - playerZ
        var camX1 = relX1 * cosYaw - relZ1 * sinYaw
        var camZ1 = relX1 * sinYaw + relZ1 * cosYaw

        val relX2 = wX2 - playerX
        val relZ2 = wZ2 - playerZ
        var camX2 = relX2 * cosYaw - relZ2 * sinYaw
        var camZ2 = relX2 * sinYaw + relZ2 * cosYaw

        val nearClip = 0.40f
        if (camZ1 < nearClip && camZ2 < nearClip) return

        if (camZ1 < nearClip) {
          val t = (nearClip - camZ1) / (camZ2 - camZ1)
          camX1 += t * (camX2 - camX1)
          camZ1 = nearClip
        } else if (camZ2 < nearClip) {
          val t = (nearClip - camZ2) / (camZ1 - camZ2)
          camX2 += t * (camX1 - camX2)
          camZ2 = nearClip
        }

        val fovFactor = canvasW * 0.95f
        val sX1 = canvasW * 0.5f + (camX1 / camZ1) * fovFactor
        val sY1 = horizonY + (currentEyeY / camZ1) * fovFactor
        val sX2 = canvasW * 0.5f + (camX2 / camZ2) * fovFactor
        val sY2 = horizonY + (currentEyeY / camZ2) * fovFactor

        drawLine(color = color, start = Offset(sX1, sY1), end = Offset(sX2, sY2), strokeWidth = strokeWidth)
      }

      // 2.1 Griglia Prospettica e Ondulazioni Naturali del Regolite (Sposta e orienta con il giocatore)
      val gridStep = 4.0f
      val snapZ = (playerZ / gridStep).toInt() * gridStep
      val snapX = (playerX / gridStep).toInt() * gridStep

      // Linee trasversali (World Z costante)
      for (zOffset in -2..16) {
        val wz = snapZ + zOffset * gridStep
        val distZ = abs(wz - playerZ)
        if (distZ < 65f) {
          val alpha = (0.28f - distZ * 0.004f).coerceIn(0.03f, 0.28f)
          val isMajor = ((wz.roundToInt() % 16) == 0)
          val lineColor = if (isMajor) env.rockColor.copy(alpha = alpha * 1.4f) else Color.Black.copy(alpha = alpha * 0.75f)
          drawWorldSegment(
            wX1 = playerX - 42f,
            wZ1 = wz,
            wX2 = playerX + 42f,
            wZ2 = wz,
            color = lineColor,
            strokeWidth = if (isMajor) 2.2f else 1.2f,
          )
        }
      }

      // Linee longitudinali (World X costante)
      for (xOffset in -10..10) {
        val wx = snapX + xOffset * gridStep
        val distX = abs(wx - playerX)
        if (distX < 42f) {
          val alpha = (0.22f - distX * 0.005f).coerceIn(0.02f, 0.22f)
          val isMajor = ((wx.roundToInt() % 16) == 0)
          val lineColor = if (isMajor) env.rockColor.copy(alpha = alpha * 1.4f) else Color.Black.copy(alpha = alpha * 0.65f)
          drawWorldSegment(
            wX1 = wx,
            wZ1 = playerZ - 8f,
            wX2 = wx,
            wZ2 = playerZ + 60f,
            color = lineColor,
            strokeWidth = if (isMajor) 2.0f else 1.0f,
          )
        }
      }

      // 2.2 Tracce di Cingoli del Rover e Orme di Stivali Impresse nel Terreno
      // Tracce continue dei cingoli del Rover dal modulo astronave (0,0) verso l'esterno
      for (step in 0 until 18) {
        val tZ1 = 1.8f + step * 2.4f
        val tZ2 = 1.8f + (step + 0.65f) * 2.4f
        val curvedX1 = sin(tZ1 * 0.08f) * 3.5f
        val curvedX2 = sin(tZ2 * 0.08f) * 3.5f
        // Cingolo sinistro
        drawWorldSegment(curvedX1 - 1.1f, tZ1, curvedX2 - 1.1f, tZ2, Color(0x7702050E), 3.2f)
        // Cingolo destro
        drawWorldSegment(curvedX1 + 1.1f, tZ1, curvedX2 + 1.1f, tZ2, Color(0x7702050E), 3.2f)
      }

      // Orme degli stivali dell'astronauta (EVA Bootprints) impresse nella polvere del regolite
      for (b in 1..8) {
        val bZ = 1.8f + b * 0.65f
        val bX = if (b % 2 == 0) -0.35f else 0.35f
        val bPos = projectWorldToScreen(bX, bZ, 0.01f)
        val bDist = sqrt((bX - playerX) * (bX - playerX) + (bZ - playerZ) * (bZ - playerZ))
        if (bPos != null && bDist > 0.4f) {
          val bScale = (18f / bDist).coerceIn(2.5f, 18f)
          drawOval(
            color = Color(0x8802050E),
            topLeft = Offset(bPos.x - bScale * 0.5f, bPos.y - bScale * 0.25f),
            size = Size(bScale, bScale * 0.5f),
          )
        }
      }

      // 2.3 Crateri d'Impatto 3D Volumetrici sul Suolo
      proceduralCraters.forEach { crater ->
        val cPos = projectWorldToScreen(crater.first, crater.second, 0f)
        val dist = sqrt((crater.first - playerX) * (crater.first - playerX) + (crater.second - playerZ) * (crater.second - playerZ))
        if (cPos != null && dist > 1f) {
          val crScale = (canvasW * 0.95f / dist * (crater.third * 0.22f)).coerceIn(4f, 180f)
          // Bordo del cratere illuminato
          drawOval(
            color = env.terrainSecondaryColor.copy(alpha = 0.9f),
            topLeft = Offset(cPos.x - crScale, cPos.y - crScale * 0.35f),
            size = Size(crScale * 2f, crScale * 0.7f),
          )
          // Cavità centrale in ombra profonda
          drawOval(
            color = Color(0x8802050E),
            topLeft = Offset(cPos.x - crScale * 0.8f, cPos.y - crScale * 0.25f),
            size = Size(crScale * 1.6f, crScale * 0.5f),
          )
          drawOval(
            color = env.rockColor.copy(alpha = 0.7f),
            topLeft = Offset(cPos.x - crScale, cPos.y - crScale * 0.35f),
            size = Size(crScale * 2f, crScale * 0.7f),
            style = Stroke(width = max(1.5f, crScale * 0.05f)),
          )
        }
      }

      // 2.4 Massi e Rocce 3D Sparsi sul Terreno (Scalatura e orientamento prospettico)
      proceduralRocks.forEach { rock ->
        val rPos = projectWorldToScreen(rock.first, rock.second, 0f)
        val dist = sqrt((rock.first - playerX) * (rock.first - playerX) + (rock.second - playerZ) * (rock.second - playerZ))
        if (rPos != null && dist > 0.5f) {
          val rScale = (canvasW * 0.95f / dist * (rock.third * 0.25f)).coerceIn(2.5f, 75f)
          // Ombra proiettata del masso a terra
          drawOval(
            color = Color(0x6602050E),
            topLeft = Offset(rPos.x - rScale * 1.1f, rPos.y),
            size = Size(rScale * 2.2f, rScale * 0.6f),
          )
          // Sagoma poligonale sfaccettata del masso 3D
          val rockPath = Path().apply {
            moveTo(rPos.x - rScale * 0.9f, rPos.y)
            lineTo(rPos.x - rScale * 0.4f, rPos.y - rScale * 1.2f)
            lineTo(rPos.x + rScale * 0.6f, rPos.y - rScale * 1.0f)
            lineTo(rPos.x + rScale * 1.0f, rPos.y)
            close()
          }
          drawPath(rockPath, color = env.rockColor)
          // Lato illuminato dal Sole
          val sunHighlightPath = Path().apply {
            moveTo(rPos.x - rScale * 0.4f, rPos.y - rScale * 1.2f)
            lineTo(rPos.x + rScale * 0.6f, rPos.y - rScale * 1.0f)
            lineTo(rPos.x + rScale * 0.2f, rPos.y - rScale * 0.4f)
            close()
          }
          drawPath(sunHighlightPath, color = Color.White.copy(alpha = 0.25f))
          drawPath(rockPath, color = Color(0xFF1E293B), style = Stroke(width = max(1f, rScale * 0.06f)))
        }
      }

      // 2.5 Particelle 3D di Polvere e Regolite sollevate dai passi dell'astronauta
      dustParticles.forEach { p ->
        val pScreen = projectWorldToScreen(p.x, p.z, p.y)
        if (pScreen != null) {
          val pProgress = (p.life / p.maxLife).coerceIn(0f, 1f)
          val pAlpha = (1f - pProgress) * 0.75f
          val pRadius = (2.4f * (1f - pProgress * 0.5f)).coerceIn(1f, 3.5f)
          drawCircle(
            color = env.rockColor.copy(alpha = pAlpha),
            radius = pRadius,
            center = pScreen,
          )
        }
      }

      // =========================================================================
      // 3. ASTRONAVE ASTROBLOOM-I IN 3D AL SITO DI SBARCO (0, 0)
      // =========================================================================
      val shipDist = sqrt(playerX * playerX + playerZ * playerZ)

      if (!isInsideShip && shipDist > 0.4f) {
        // 3.1 Ombra proiettata dell'astronave sul suolo
        val shadowCenter = projectWorldToScreen(0f, 0f, 0.05f)
        if (shadowCenter != null) {
          val shadowW = (canvasW * 0.95f / shipDist) * 2.8f
          drawOval(
            color = Color(0x6602050E),
            topLeft = Offset(shadowCenter.x - shadowW, shadowCenter.y - shadowW * 0.45f),
            size = Size(shadowW * 2f, shadowW * 0.9f),
          )
        }

        // 3.2 Zampe d'Atterraggio (4 Zampe Telescopiche con Pistoni e Piastre al Suolo)
        val legConfigs = listOf(
          Pair(-2.4f, 2.2f),   // Anteriore Sinistra
          Pair(2.4f, 2.2f),    // Anteriore Destra
          Pair(-2.4f, -2.2f),  // Posteriore Sinistra
          Pair(2.4f, -2.2f)    // Posteriore Destra
        )
        for (leg in legConfigs) {
          val footScreen = projectWorldToScreen(leg.first, leg.second, 0.05f)
          val kneeScreen = projectWorldToScreen(leg.first * 0.65f, leg.second * 0.65f, 1.4f)
          val hipScreen = projectWorldToScreen(leg.first * 0.45f, leg.second * 0.45f, 2.1f)
          if (footScreen != null && kneeScreen != null && hipScreen != null) {
            val legStroke = (canvasW * 0.95f / shipDist * 0.12f).coerceIn(2f, 14f)
            // Piastra d'atterraggio (Footpad circolare)
            drawCircle(
              color = Color(0xFF1E293B),
              radius = legStroke * 2.2f,
              center = footScreen,
            )
            drawCircle(
              color = Color(0xFF64748B),
              radius = legStroke * 2.2f,
              center = footScreen,
              style = Stroke(width = 1.5f),
            )
            // Pistone idraulico e montante diagonale
            drawLine(color = StarlightWhite, start = hipScreen, end = kneeScreen, strokeWidth = legStroke)
            drawLine(color = Color(0xFF94A3B8), start = kneeScreen, end = footScreen, strokeWidth = legStroke * 0.8f)
          }
        }

        // 3.3 Ugello Campaniforme del Motore a Razzo Principale
        val bellTop = projectWorldToScreen(0f, 0f, 1.4f)
        val bellBottom = projectWorldToScreen(0f, 0f, 0.35f)
        if (bellTop != null && bellBottom != null) {
          val bellW = (canvasW * 0.95f / shipDist * 0.85f).coerceIn(4f, 120f)
          val bellPath = Path().apply {
            moveTo(bellTop.x - bellW * 0.4f, bellTop.y)
            lineTo(bellTop.x + bellW * 0.4f, bellTop.y)
            lineTo(bellBottom.x + bellW * 0.8f, bellBottom.y)
            lineTo(bellBottom.x - bellW * 0.8f, bellBottom.y)
            close()
          }
          drawPath(bellPath, color = Color(0xFF1E293B))
          drawPath(bellPath, color = CosmicAmber.copy(alpha = 0.75f), style = Stroke(width = 2f))
        }

        // 3.4 Fusoliera Centrale / Modulo di Servizio (Lamina Termica Dorata MLI)
        val hullBottom = projectWorldToScreen(0f, 0f, 1.6f)
        val hullTop = projectWorldToScreen(0f, 0f, 3.4f)
        if (hullBottom != null && hullTop != null) {
          val hullRadius = 2.0f
          val hLeftB = projectWorldToScreen(-hullRadius, 0f, 1.6f) ?: Offset(hullBottom.x - 40f, hullBottom.y)
          val hRightB = projectWorldToScreen(hullRadius, 0f, 1.6f) ?: Offset(hullBottom.x + 40f, hullBottom.y)
          val hLeftT = projectWorldToScreen(-hullRadius * 0.9f, 0f, 3.4f) ?: Offset(hullTop.x - 36f, hullTop.y)
          val hRightT = projectWorldToScreen(hullRadius * 0.9f, 0f, 3.4f) ?: Offset(hullTop.x + 36f, hullTop.y)

          val hullPath = Path().apply {
            moveTo(hLeftB.x, hLeftB.y)
            lineTo(hRightB.x, hRightB.y)
            lineTo(hRightT.x, hRightT.y)
            lineTo(hLeftT.x, hLeftT.y)
            close()
          }
          // Riflesso scintillante dorato dell'isolante termico aerospaziale
          drawPath(
            path = hullPath,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFFF59E0B), Color(0xFFB45309)),
              startY = hLeftT.y,
              endY = hLeftB.y,
            ),
          )
          drawPath(path = hullPath, color = Color(0xFF78350F), style = Stroke(width = 2.2f))

          // Trama a rombi della pellicola termica
          for (step in 1..4) {
            val frac = step / 5f
            val yLine = hLeftT.y + (hLeftB.y - hLeftT.y) * frac
            drawLine(
              color = Color(0xFFFDE68A).copy(alpha = 0.45f),
              start = Offset(hLeftT.x + (hLeftB.x - hLeftT.x) * frac, yLine),
              end = Offset(hRightT.x + (hRightB.x - hRightT.x) * frac, yLine),
              strokeWidth = 1.2f,
            )
          }
        }

        // 3.5 Modulo Superiore Cabina di Comando (Titanio Bianco Aerospaziale)
        val apexScreen = projectWorldToScreen(0f, 0f, 5.2f)
        if (hullTop != null && apexScreen != null) {
          val cabRadius = 1.8f
          val cLeft = projectWorldToScreen(-cabRadius * 0.85f, 0f, 3.4f) ?: Offset(hullTop.x - 30f, hullTop.y)
          val cRight = projectWorldToScreen(cabRadius * 0.85f, 0f, 3.4f) ?: Offset(hullTop.x + 30f, hullTop.y)

          val cabPath = Path().apply {
            moveTo(cLeft.x, cLeft.y)
            lineTo(cRight.x, cRight.y)
            lineTo(apexScreen.x, apexScreen.y)
            close()
          }
          drawPath(
            path = cabPath,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFFF8FAFC), Color(0xFF94A3B8)),
              startY = apexScreen.y,
              endY = cLeft.y,
            ),
          )
          drawPath(path = cabPath, color = Color(0xFF475569), style = Stroke(width = 2.2f))

          // Oblò Panoramico Esagonale della Cabina (Illuminato Ciano)
          val windowPos = projectWorldToScreen(0f, 1.2f, 4.0f)
          if (windowPos != null) {
            val winScale = (canvasW * 0.95f / shipDist * 0.35f).coerceIn(3f, 55f)
            drawCircle(color = CosmicCyan.copy(alpha = 0.85f), radius = winScale, center = windowPos)
            drawCircle(color = Color.White, radius = winScale, center = windowPos, style = Stroke(width = 1.8f))
            drawCircle(color = Color.White.copy(alpha = 0.6f), radius = winScale * 0.45f, center = Offset(windowPos.x - winScale * 0.25f, windowPos.y - winScale * 0.25f))
          }
        }

        // 3.6 Portellone Airlock Aperto (con Luce Interna Calda che Filtra all'Esterno)
        val hatchTop = projectWorldToScreen(0f, 1.6f, 3.0f)
        val hatchBottom = projectWorldToScreen(0f, 1.6f, 1.7f)
        if (hatchTop != null && hatchBottom != null) {
          val hatchW = (canvasW * 0.95f / shipDist * 0.45f).coerceIn(4f, 65f)
          val hatchH = abs(hatchBottom.y - hatchTop.y).coerceAtLeast(6f)
          // Luce interna calda
          drawRoundRect(
            color = Color(0xFFFEF08A).copy(alpha = 0.90f),
            topLeft = Offset(hatchTop.x - hatchW * 0.5f, hatchTop.y),
            size = Size(hatchW, hatchH),
            cornerRadius = CornerRadius(hatchW * 0.15f),
          )
          drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(hatchTop.x - hatchW * 0.5f, hatchTop.y),
            size = Size(hatchW, hatchH),
            cornerRadius = CornerRadius(hatchW * 0.15f),
            style = Stroke(width = 2.5f),
          )

          // 3.7 Scaletta di Sbarco Metallica con Pioli (dall'airlock al terreno)
          val ladderFootLeft = projectWorldToScreen(-0.4f, 2.3f, 0.05f)
          val ladderFootRight = projectWorldToScreen(0.4f, 2.3f, 0.05f)
          val ladderTopLeft = Offset(hatchTop.x - hatchW * 0.35f, hatchBottom.y)
          val ladderTopRight = Offset(hatchTop.x + hatchW * 0.35f, hatchBottom.y)

          if (ladderFootLeft != null && ladderFootRight != null) {
            val ladderStroke = (canvasW * 0.95f / shipDist * 0.06f).coerceIn(1.5f, 6f)
            // Montanti
            drawLine(color = StarlightWhite, start = ladderTopLeft, end = ladderFootLeft, strokeWidth = ladderStroke)
            drawLine(color = StarlightWhite, start = ladderTopRight, end = ladderFootRight, strokeWidth = ladderStroke)

            // 5 Pioli orizzontali
            for (r in 1..5) {
              val frac = r / 6f
              val stepL = Offset(ladderTopLeft.x + (ladderFootLeft.x - ladderTopLeft.x) * frac, ladderTopLeft.y + (ladderFootLeft.y - ladderTopLeft.y) * frac)
              val stepR = Offset(ladderTopRight.x + (ladderFootRight.x - ladderTopRight.x) * frac, ladderTopRight.y + (ladderFootRight.y - ladderTopRight.y) * frac)
              drawLine(color = CosmicAmber, start = stepL, end = stepR, strokeWidth = ladderStroke * 0.85f)
            }
          }
        }

        // 3.8 Bandiera della Missione AstroBloom Piantata Accanto alla Scaletta
        val flagBase = projectWorldToScreen(1.6f, 2.8f, 0.05f)
        val flagPoleTop = projectWorldToScreen(1.6f, 2.8f, 1.8f)
        if (flagBase != null && flagPoleTop != null) {
          val fScale = (canvasW * 0.95f / shipDist * 0.4f).coerceIn(4f, 60f)
          drawLine(color = StarlightWhite, start = flagBase, end = flagPoleTop, strokeWidth = 2.5f)
          val flagPath = Path().apply {
            moveTo(flagPoleTop.x, flagPoleTop.y)
            lineTo(flagPoleTop.x + fScale, flagPoleTop.y + fScale * 0.08f * sin(simTimeSec * 4f))
            lineTo(flagPoleTop.x + fScale, flagPoleTop.y + fScale * 0.6f + fScale * 0.08f * sin(simTimeSec * 4f))
            lineTo(flagPoleTop.x, flagPoleTop.y + fScale * 0.5f)
            close()
          }
          drawPath(flagPath, color = AuroraGreen)
          drawPath(flagPath, color = Color.White, style = Stroke(width = 1.2f))
        }

        // 3.9 Targa e Logo Fluttuante dell'Astronave
        if (shipDist < 16f && apexScreen != null) {
          val shipNameMeas = textMeasurer.measure(
            "🚀 ASTRONAVE ASTROBLOOM-I",
            TextStyle(fontFamily = FredokaFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = CosmicAmber),
          )
          val distMeas = textMeasurer.measure(
            "${String.format("%.1f", shipDist)}m dal modulo di discesa",
            TextStyle(fontFamily = FredokaFontFamily, fontSize = 7.5.sp, color = StarlightWhite.copy(alpha = 0.85f)),
          )
          val badgeW = maxOf(shipNameMeas.size.width, distMeas.size.width) + 16f
          val badgeH = shipNameMeas.size.height + distMeas.size.height + 6f
          val badgePos = Offset(apexScreen.x - badgeW / 2f, apexScreen.y - badgeH - 12f)
          drawRoundRect(color = Color(0xDD04091A), topLeft = badgePos, size = Size(badgeW, badgeH), cornerRadius = CornerRadius(6f))
          drawRoundRect(color = CosmicAmber.copy(alpha = 0.7f), topLeft = badgePos, size = Size(badgeW, badgeH), cornerRadius = CornerRadius(6f), style = Stroke(width = 1.2f))
          drawText(shipNameMeas, topLeft = Offset(badgePos.x + 8f, badgePos.y + 2f))
          drawText(distMeas, topLeft = Offset(badgePos.x + 8f, badgePos.y + shipNameMeas.size.height + 2f))
        }
      } else if (isInsideShip) {
        // =========================================================================
        // 3.B VISTA DAL PORTELLONE APERTO DELL'ASTRONAVE (PRIMA DI SCENDERE)
        // =========================================================================
        // Cornice interna dell'Airlock della cabina
        val frameThick = 45f
        drawRect(color = Color(0xEE1E293B), topLeft = Offset.Zero, size = Size(frameThick, canvasH))
        drawRect(color = Color(0xEE1E293B), topLeft = Offset(canvasW - frameThick, 0f), size = Size(frameThick, canvasH))
        drawRect(color = Color(0xEE0F172A), topLeft = Offset.Zero, size = Size(canvasW, frameThick))
        drawRect(color = Color(0xEE334155), topLeft = Offset(0f, canvasH - frameThick * 1.4f), size = Size(canvasW, frameThick * 1.4f))

        // Indicatori luminosi e ringhiera dell'airlock
        drawLine(color = AuroraGreen, start = Offset(frameThick - 4f, 0f), end = Offset(frameThick - 4f, canvasH), strokeWidth = 3f)
        drawLine(color = AuroraGreen, start = Offset(canvasW - frameThick + 4f, 0f), end = Offset(canvasW - frameThick + 4f, canvasH), strokeWidth = 3f)

        // La scaletta di sbarco che scende dal portellone verso la superficie
        val ladderTopL = Offset(canvasW * 0.42f, canvasH - frameThick * 1.4f)
        val ladderTopR = Offset(canvasW * 0.58f, canvasH - frameThick * 1.4f)
        val ladderBottomL = Offset(canvasW * 0.38f, canvasH)
        val ladderBottomR = Offset(canvasW * 0.62f, canvasH)

        drawLine(color = StarlightWhite, start = ladderTopL, end = ladderBottomL, strokeWidth = 5f)
        drawLine(color = StarlightWhite, start = ladderTopR, end = ladderBottomR, strokeWidth = 5f)
        for (i in 1..4) {
          val f = i / 5f
          val sL = Offset(ladderTopL.x + (ladderBottomL.x - ladderTopL.x) * f, ladderTopL.y + (ladderBottomL.y - ladderTopL.y) * f)
          val sR = Offset(ladderTopR.x + (ladderBottomR.x - ladderTopR.x) * f, ladderTopR.y + (ladderBottomR.y - ladderTopR.y) * f)
          drawLine(color = CosmicAmber, start = sL, end = sR, strokeWidth = 4f)
        }
      }

      // =========================================================================
      // 4. PUNTI DI INTERESSE (POI) SUL SUOLO CON FARI OLOGRAFICI GIGANTI E MODELLI 3D
      // =========================================================================
      pois.forEach { poi ->
        val screenPos = projectWorldToScreen(poi.posX, poi.posZ, 0f)
        val dist = sqrt((poi.posX - playerX) * (poi.posX - playerX) + (poi.posZ - playerZ) * (poi.posZ - playerZ))
        if (screenPos != null && dist > 0.5f) {
          val scale = (35f / dist).coerceIn(3.5f, 95f)

          // 4.1 Faro Olografico Gigante dal Suolo al Cielo (Sky Beacon Pillar)
          // Altissimo e luminoso a qualsiasi distanza per non perdersi mai nel vuoto planetario!
          val beaconH = max(canvasH * 0.72f, 360f)
          val beaconTopY = screenPos.y - beaconH

          // Alone diffuso del fascio
          val beamThickness = (18f / sqrt(dist)).coerceIn(4f, 26f)
          drawLine(
            brush = Brush.verticalGradient(
              colors = listOf(Color.Transparent, AuroraGreen.copy(alpha = 0.85f)),
              startY = beaconTopY,
              endY = screenPos.y,
            ),
            start = Offset(screenPos.x, beaconTopY),
            end = Offset(screenPos.x, screenPos.y),
            strokeWidth = beamThickness,
          )
          // Nucleo luminoso bianco-azzurro concentrato
          drawLine(
            brush = Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.95f)),
              startY = beaconTopY,
              endY = screenPos.y,
            ),
            start = Offset(screenPos.x, beaconTopY),
            end = Offset(screenPos.x, screenPos.y),
            strokeWidth = max(2f, beamThickness * 0.25f),
          )

          // Anelli energetici ascendenti lungo il fascio
          for (ringIdx in 0..3) {
            val ringPhase = (simTimeSec * 1.2f + ringIdx * 0.25f) % 1.0f
            val ringY = screenPos.y - ringPhase * beaconH * 0.85f
            val ringW = (32f / sqrt(dist)).coerceIn(8f, 55f) * (1f + ringPhase * 0.4f)
            val ringAlpha = ((1f - ringPhase) * 0.85f).coerceIn(0.1f, 0.85f)
            drawOval(
              color = AuroraGreen.copy(alpha = ringAlpha),
              topLeft = Offset(screenPos.x - ringW * 0.5f, ringY - 4f),
              size = Size(ringW, 8f),
              style = Stroke(width = 1.5f),
            )
          }

          // Punto di contatto alla base al suolo con onde concentriche
          drawCircle(
            color = AuroraGreen.copy(alpha = 0.4f),
            radius = max(8f, scale * 0.45f),
            center = Offset(screenPos.x, screenPos.y),
          )
          drawCircle(
            color = Color.White,
            radius = max(3f, scale * 0.18f),
            center = Offset(screenPos.x, screenPos.y),
          )

          // 4.2 Badge Olografico Fluttuante (SEMPRE VISIBILE a qualsiasi distanza!)
          val badgeDist = dist.roundToInt()
          val badgeText = "🎯 ${poi.name.take(22)} • ${badgeDist}m"
          val bMeas = textMeasurer.measure(
            badgeText,
            TextStyle(fontFamily = FredokaFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AuroraGreen),
          )
          val pillW = bMeas.size.width + 16f
          val pillH = bMeas.size.height + 8f
          // Posizionato a metà faro o sopra l'oggetto per essere sempre leggibile
          val pillY = (screenPos.y - min(beaconH * 0.55f, 150f)).coerceAtLeast(36f)
          val pillTopLeft = Offset(screenPos.x - pillW / 2f, pillY)

          drawRoundRect(
            color = Color(0xF0021008),
            topLeft = pillTopLeft,
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(6f),
          )
          drawRoundRect(
            color = AuroraGreen,
            topLeft = pillTopLeft,
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(6f),
            style = Stroke(width = 1.4f),
          )
          drawText(bMeas, topLeft = Offset(pillTopLeft.x + 8f, pillTopLeft.y + 4f))

          // 4.3 Modelli 3D specifici per POI al suolo (se entro 45 metri)
          if (dist < 45f) {
            when {
              // A) Rover Perseverance & Drone Ingenuity (Marte)
              poi.id.contains("perseverance") || poi.name.contains("Perseverance") -> {
                val rovScale = (canvasW * 0.95f / dist * 0.65f).coerceIn(12f, 140f)
                // 6 Ruote nere con mozzi dorati
                val wheelOffsets = listOf(
                  Offset(-rovScale * 0.6f, -rovScale * 0.1f),
                  Offset(0f, -rovScale * 0.05f),
                  Offset(rovScale * 0.6f, -rovScale * 0.1f),
                  Offset(-rovScale * 0.7f, rovScale * 0.15f),
                  Offset(0f, rovScale * 0.2f),
                  Offset(rovScale * 0.7f, rovScale * 0.15f),
                )
                wheelOffsets.forEach { wOff ->
                  drawCircle(color = Color(0xFF1E293B), radius = rovScale * 0.18f, center = Offset(screenPos.x + wOff.x, screenPos.y + wOff.y))
                  drawCircle(color = CosmicAmber, radius = rovScale * 0.08f, center = Offset(screenPos.x + wOff.x, screenPos.y + wOff.y))
                }
                // Bracci di sospensione rocker-bogie
                drawLine(
                  color = Color(0xFF64748B),
                  start = Offset(screenPos.x - rovScale * 0.6f, screenPos.y),
                  end = Offset(screenPos.x + rovScale * 0.6f, screenPos.y),
                  strokeWidth = 2.5f,
                )
                // Chassis centrale bianco termico
                drawRoundRect(
                  color = Color(0xFFF1F5F9),
                  topLeft = Offset(screenPos.x - rovScale * 0.5f, screenPos.y - rovScale * 0.55f),
                  size = Size(rovScale, rovScale * 0.45f),
                  cornerRadius = CornerRadius(4f),
                )
                // Albero della telecamera Mastcam-Z
                val mastTop = Offset(screenPos.x - rovScale * 0.2f, screenPos.y - rovScale * 0.95f)
                drawLine(color = Color(0xFF334155), start = Offset(screenPos.x - rovScale * 0.2f, screenPos.y - rovScale * 0.55f), end = mastTop, strokeWidth = 3f)
                // Testa della telecamera con due lenti binoculari
                drawRoundRect(color = Color(0xFF0F172A), topLeft = Offset(mastTop.x - rovScale * 0.15f, mastTop.y - rovScale * 0.12f), size = Size(rovScale * 0.3f, rovScale * 0.14f), cornerRadius = CornerRadius(2f))
                drawCircle(color = CosmicCyan, radius = rovScale * 0.04f, center = Offset(mastTop.x - rovScale * 0.07f, mastTop.y - rovScale * 0.05f))
                drawCircle(color = CosmicCyan, radius = rovScale * 0.04f, center = Offset(mastTop.x + rovScale * 0.07f, mastTop.y - rovScale * 0.05f))
                // Drone Ingenuity affiancato (a destra)
                val ingX = screenPos.x + rovScale * 1.1f
                val ingY = screenPos.y - rovScale * 0.1f
                drawCircle(color = CosmicAmber, radius = rovScale * 0.10f, center = Offset(ingX, ingY)) // cubetto dorato
                // 2 rotori coassiali in fibra di carbonio
                drawLine(color = Color.White, start = Offset(ingX - rovScale * 0.22f, ingY - rovScale * 0.12f), end = Offset(ingX + rovScale * 0.22f, ingY - rovScale * 0.12f), strokeWidth = 1.5f)
                drawLine(color = Color.White, start = Offset(ingX - rovScale * 0.22f, ingY - rovScale * 0.18f), end = Offset(ingX + rovScale * 0.22f, ingY - rovScale * 0.18f), strokeWidth = 1.5f)
              }

              // B) LEM Eagle & Bandiera Americana (Luna / Apollo 11)
              poi.id.contains("apollo11") || poi.name.contains("Eagle") || poi.name.contains("Tranquility") -> {
                val lemScale = (canvasW * 0.95f / dist * 0.75f).coerceIn(14f, 160f)
                // 4 Zampe d'atterraggio dorate
                val legEnds = listOf(
                  Offset(screenPos.x - lemScale * 0.8f, screenPos.y + lemScale * 0.1f),
                  Offset(screenPos.x + lemScale * 0.8f, screenPos.y + lemScale * 0.1f),
                )
                legEnds.forEach { lEnd ->
                  drawLine(color = Color(0xFFF59E0B), start = Offset(screenPos.x, screenPos.y - lemScale * 0.3f), end = lEnd, strokeWidth = 3f)
                  drawCircle(color = Color(0xFF78350F), radius = lemScale * 0.12f, center = lEnd)
                }
                // Stadio di discesa ottagonale dorato (Mylar foil)
                drawRoundRect(
                  brush = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309))),
                  topLeft = Offset(screenPos.x - lemScale * 0.5f, screenPos.y - lemScale * 0.5f),
                  size = Size(lemScale, lemScale * 0.35f),
                  cornerRadius = CornerRadius(4f),
                )
                // Stadio di risalita grigio alluminio
                val ascentTop = screenPos.y - lemScale * 0.9f
                drawRoundRect(
                  color = Color(0xFFCBD5E1),
                  topLeft = Offset(screenPos.x - lemScale * 0.38f, ascentTop),
                  size = Size(lemScale * 0.76f, lemScale * 0.42f),
                  cornerRadius = CornerRadius(6f),
                )
                // Finestrini triangolari del LEM
                drawCircle(color = CosmicCyan, radius = lemScale * 0.05f, center = Offset(screenPos.x - lemScale * 0.12f, ascentTop + lemScale * 0.15f))
                drawCircle(color = CosmicCyan, radius = lemScale * 0.05f, center = Offset(screenPos.x + lemScale * 0.12f, ascentTop + lemScale * 0.15f))

                // Bandiera Americana piantata nel regolite lunare (a sinistra)
                val flagPoleBase = Offset(screenPos.x - lemScale * 1.1f, screenPos.y)
                val flagPoleTop = Offset(screenPos.x - lemScale * 1.1f, screenPos.y - lemScale * 0.85f)
                drawLine(color = Color.White, start = flagPoleBase, end = flagPoleTop, strokeWidth = 2.5f)
                // Traversa orizzontale superiore
                val flagBarEnd = Offset(flagPoleTop.x + lemScale * 0.35f, flagPoleTop.y)
                drawLine(color = Color.White, start = flagPoleTop, end = flagBarEnd, strokeWidth = 1.8f)
                // Drappo della bandiera a strisce
                val flagW = lemScale * 0.35f
                val flagH = lemScale * 0.24f
                drawRect(color = Color(0xFFDC2626), topLeft = flagPoleTop, size = Size(flagW, flagH))
                drawRect(color = Color.White, topLeft = Offset(flagPoleTop.x, flagPoleTop.y + flagH * 0.33f), size = Size(flagW, flagH * 0.33f))
                drawRect(color = Color(0xFF1D4ED8), topLeft = flagPoleTop, size = Size(flagW * 0.45f, flagH * 0.55f))
              }

              // C) Sonda Storica Venera 13 / Huygens
              poi.id.contains("venera") || poi.id.contains("huygens") || poi.name.contains("Venera") || poi.name.contains("Huygens") -> {
                val probeScale = (canvasW * 0.95f / dist * 0.6f).coerceIn(10f, 130f)
                // Scudo aerodinamico circolare alla base
                drawOval(
                  color = Color(0xFF64748B),
                  topLeft = Offset(screenPos.x - probeScale * 0.6f, screenPos.y - probeScale * 0.15f),
                  size = Size(probeScale * 1.2f, probeScale * 0.3f),
                )
                // Corpo sferico a fungo
                drawCircle(
                  brush = Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFF475569))),
                  radius = probeScale * 0.35f,
                  center = Offset(screenPos.x, screenPos.y - probeScale * 0.4f),
                )
                // Disco antenna superiore
                drawOval(
                  color = Color(0xFF94A3B8),
                  topLeft = Offset(screenPos.x - probeScale * 0.35f, screenPos.y - probeScale * 0.85f),
                  size = Size(probeScale * 0.7f, probeScale * 0.15f),
                )
                drawLine(color = Color.White, start = Offset(screenPos.x, screenPos.y - probeScale * 0.75f), end = Offset(screenPos.x, screenPos.y - probeScale * 1.05f), strokeWidth = 2f)
              }

              // D) Crio-Geyser di Vapore d'Acqua (Europa / Giove)
              poi.id.contains("geyser") || poi.name.contains("Geyser") -> {
                val gScale = (canvasW * 0.95f / dist * 0.7f).coerceIn(12f, 150f)
                // Fessura nel ghiaccio azzurro
                drawOval(color = Color(0xFF0284C7), topLeft = Offset(screenPos.x - gScale * 0.6f, screenPos.y - gScale * 0.15f), size = Size(gScale * 1.2f, gScale * 0.3f))
                // Colonna di particelle di vapore che zampilla
                for (p in 0..7) {
                  val pPhase = (simTimeSec * 2.5f + p * 0.125f) % 1.0f
                  val pY = screenPos.y - pPhase * gScale * 2.2f
                  val pX = screenPos.x + sin(simTimeSec * 4f + p) * gScale * 0.25f
                  val pR = (gScale * 0.15f) * (1f + pPhase * 1.5f)
                  drawCircle(color = Color.White.copy(alpha = (1f - pPhase) * 0.8f), radius = pR, center = Offset(pX, pY))
                }
              }

              // E) Monoliti o Vette Rocciose per formazioni naturali
              else -> {
                val natScale = (canvasW * 0.95f / dist * 0.8f).coerceIn(14f, 160f)
                val peakPath = Path().apply {
                  moveTo(screenPos.x - natScale * 0.6f, screenPos.y)
                  lineTo(screenPos.x, screenPos.y - natScale * 1.1f)
                  lineTo(screenPos.x + natScale * 0.6f, screenPos.y)
                  close()
                }
                drawPath(peakPath, color = env.rockColor)
                drawPath(peakPath, color = Color.White.copy(alpha = 0.35f), style = Stroke(width = 2f))
              }
            }
          }
        }
      }

      // =========================================================================
      // 5. CAMPIONI MINERALI & RISORSE ALIENI SUL SUOLO
      // =========================================================================
      samples.forEach { sample ->
        if (sample.id !in collectedSampleIds) {
          val screenPos = projectWorldToScreen(sample.posX, sample.posZ, 0f)
          val dist = sqrt((sample.posX - playerX) * (sample.posX - playerX) + (sample.posZ - playerZ) * (sample.posZ - playerZ))
          if (screenPos != null && dist > 0.4f) {
            val scale = (25f / dist).coerceIn(2.5f, 50f)
            val pulse = (sin(simTimeSec * 5f + sample.posX) * 0.3f + 0.7f)

            // Alone particellare del minerale
            drawCircle(
              color = CosmicCyan.copy(alpha = 0.35f * pulse),
              radius = scale * 0.8f,
              center = Offset(screenPos.x, screenPos.y),
            )
            // Cristallo minerale 3D
            val diamondPath = Path().apply {
              moveTo(screenPos.x, screenPos.y - scale * 0.6f)
              lineTo(screenPos.x + scale * 0.45f, screenPos.y)
              lineTo(screenPos.x, screenPos.y + scale * 0.6f)
              lineTo(screenPos.x - scale * 0.45f, screenPos.y)
              close()
            }
            drawPath(diamondPath, color = if (sample.rarity == "Leggendario") NebulaMagenta else CosmicCyan)
            drawPath(diamondPath, color = Color.White, style = Stroke(width = 1.2f))

            if (dist < 12f) {
              val meas = textMeasurer.measure(
                "💎 ${sample.name}",
                TextStyle(fontFamily = FredokaFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CosmicCyan),
              )
              drawText(meas, topLeft = Offset(screenPos.x - meas.size.width / 2f, screenPos.y - scale * 0.7f - 14f))
            }
          }
        }
      }

      // =========================================================================
      // 6. HUD VISIERA CASCO ASTRONAUTA (FIRST PERSON EVA HELMET HUD)
      // =========================================================================
      // Profilo curva visiera del casco
      val visorStroke = 4f
      drawRoundRect(
        color = Color(0x220284C7),
        topLeft = Offset(14f, 14f),
        size = Size(canvasW - 28f, canvasH - 28f),
        cornerRadius = CornerRadius(36f, 36f),
        style = Stroke(width = visorStroke),
      )

      // 6.1 Bussola Azimutale Superiore a 360° con indicatore Lander & POI
      val compassCenter = Offset(canvasW * 0.5f, 32f)
      val compassHalfW = 160f
      drawRoundRect(
        color = Color(0xCC040816),
        topLeft = Offset(compassCenter.x - compassHalfW - 8f, compassCenter.y - 14f),
        size = Size((compassHalfW + 8f) * 2f, 30f),
        cornerRadius = CornerRadius(6f),
      )
      drawLine(
        color = CosmicCyan.copy(alpha = 0.8f),
        start = Offset(compassCenter.x - compassHalfW, compassCenter.y),
        end = Offset(compassCenter.x + compassHalfW, compassCenter.y),
        strokeWidth = 2f,
      )

      for (offset in -4..4) {
        val tickDeg = ((playerYawDeg + offset * 15 + 360f) % 360f).roundToInt()
        val tickX = compassCenter.x + offset * 32f
        val isCard = tickDeg % 90 == 0
        val tickH = if (isCard) 12f else 6f
        drawLine(
          color = if (isCard) CosmicAmber else CosmicCyan,
          start = Offset(tickX, compassCenter.y - tickH),
          end = Offset(tickX, compassCenter.y),
          strokeWidth = if (isCard) 2.2f else 1.2f,
        )
        if (isCard) {
          val cardLabel = when (tickDeg) {
            0 -> "N"
            90 -> "E"
            180 -> "S"
            270 -> "W"
            else -> "$tickDeg°"
          }
          val txt = textMeasurer.measure(
            cardLabel,
            TextStyle(fontFamily = FredokaFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicAmber),
          )
          drawText(txt, topLeft = Offset(tickX - txt.size.width / 2f, compassCenter.y + 2f))
        }
      }
      // Freccia prua centrale
      drawCircle(color = CosmicAmber, radius = 4f, center = Offset(compassCenter.x, compassCenter.y - 2f))

      // Indicatore Direzione Lander sulla bussola
      val angleToLander = ((atan2(-playerX.toDouble(), -playerZ.toDouble()) * 180.0 / PI - playerYawDeg + 540.0) % 360.0) - 180.0
      if (abs(angleToLander) < 45.0) {
        val landerMarkerX = compassCenter.x + (angleToLander.toFloat() / 45f) * compassHalfW
        drawCircle(color = CosmicAmber, radius = 4f, center = Offset(landerMarkerX, compassCenter.y - 10f))
      }

      // Indicatori Direzione POI sulla bussola
      pois.forEach { poi ->
        val dx = poi.posX - playerX
        val dz = poi.posZ - playerZ
        val angleToPoi = ((atan2(dx.toDouble(), dz.toDouble()) * 180.0 / PI - playerYawDeg + 540.0) % 360.0) - 180.0
        if (abs(angleToPoi) < 45.0) {
          val poiMarkerX = compassCenter.x + (angleToPoi.toFloat() / 45f) * compassHalfW
          drawCircle(color = AuroraGreen, radius = 3.2f, center = Offset(poiMarkerX, compassCenter.y - 10f))
        }
      }

      // 6.2 Pannello Telemetria Ambientale Realistica (In alto a sinistra)
      val envPanelTopLeft = Offset(24f, 24f)
      val envPanelW = 190f
      val envPanelH = 92f
      drawRoundRect(
        color = Color(0xDD04091A),
        topLeft = envPanelTopLeft,
        size = Size(envPanelW, envPanelH),
        cornerRadius = CornerRadius(8f),
      )
      drawRoundRect(
        color = CosmicCyan.copy(alpha = 0.55f),
        topLeft = envPanelTopLeft,
        size = Size(envPanelW, envPanelH),
        cornerRadius = CornerRadius(8f),
        style = Stroke(width = 1.2f),
      )

      val envTitle = textMeasurer.measure(
        "🛰️ ${env.planetName.uppercase()}",
        TextStyle(fontFamily = FredokaFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = CosmicCyan),
      )
      drawText(envTitle, topLeft = Offset(envPanelTopLeft.x + 8f, envPanelTopLeft.y + 6f))

      val gravText = textMeasurer.measure(
        "GRAVITÀ: ${env.gravityMs2} m/s² (${(env.gravityRatio * 100).roundToInt()}%)",
        TextStyle(fontFamily = FredokaFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Medium, color = StarlightWhite),
      )
      drawText(gravText, topLeft = Offset(envPanelTopLeft.x + 8f, envPanelTopLeft.y + 24f))

      val tempText = textMeasurer.measure(
        "TEMP: ${env.avgTempCelsius}°C • PRESS: ${env.pressureAtm} ATM",
        TextStyle(fontFamily = FredokaFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Medium, color = CosmicAmber),
      )
      drawText(tempText, topLeft = Offset(envPanelTopLeft.x + 8f, envPanelTopLeft.y + 40f))

      val o2Text = textMeasurer.measure(
        "TUTA EVA O₂: ${oxygenLevel.roundToInt()}%",
        TextStyle(fontFamily = FredokaFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (oxygenLevel > 20f) AuroraGreen else Color.Red),
      )
      drawText(o2Text, topLeft = Offset(envPanelTopLeft.x + 8f, envPanelTopLeft.y + 56f))

      val quotaText = textMeasurer.measure(
        "QUOTA OCCHI: ${String.format("%.2f", currentEyeY)}m",
        TextStyle(fontFamily = FredokaFontFamily, fontSize = 7.5.sp, color = StarlightWhite.copy(alpha = 0.8f)),
      )
      drawText(quotaText, topLeft = Offset(envPanelTopLeft.x + 8f, envPanelTopLeft.y + 72f))

      // 6.3 Mini Radar Circolare 2D Tattico (In alto a destra)
      val radarRadius = 38f
      val radarCenter = Offset(canvasW - radarRadius - 26f, radarRadius + 26f)
      drawCircle(color = SpaceVoid.copy(alpha = 0.85f), radius = radarRadius, center = radarCenter)
      drawCircle(color = CosmicCyan.copy(alpha = 0.6f), radius = radarRadius, center = radarCenter, style = Stroke(width = 1.4f))
      drawCircle(color = CosmicCyan.copy(alpha = 0.25f), radius = radarRadius * 0.5f, center = radarCenter, style = Stroke(width = 0.8f))

      // Punto del giocatore e cono visivo
      drawCircle(color = Color.White, radius = 2.5f, center = radarCenter)
      val radarFwdX = sin(yawRad) * 8f
      val radarFwdZ = -cos(yawRad) * 8f
      drawLine(
        color = AuroraGreen,
        start = radarCenter,
        end = Offset(radarCenter.x + radarFwdX, radarCenter.y + radarFwdZ),
        strokeWidth = 2f,
      )

      // Punti del radar: Lander (Verde), POI (Giallo), Campioni (Ciano)
      val radarScaleMeters = radarRadius / 35f // 35 metri di portata
      // Lander sul radar
      val rLanderX = (-playerX) * radarScaleMeters
      val rLanderY = (playerZ) * radarScaleMeters // Y invertita
      if (sqrt(rLanderX * rLanderX + rLanderY * rLanderY) < radarRadius) {
        drawCircle(color = AuroraGreen, radius = 2.8f, center = Offset(radarCenter.x + rLanderX, radarCenter.y + rLanderY))
      }
      // POI sul radar
      pois.forEach { poi ->
        val rx = (poi.posX - playerX) * radarScaleMeters
        val ry = -(poi.posZ - playerZ) * radarScaleMeters
        if (sqrt(rx * rx + ry * ry) < radarRadius) {
          drawCircle(color = CosmicAmber, radius = 2.5f, center = Offset(radarCenter.x + rx, radarCenter.y + ry))
        }
      }
      // Campioni sul radar
      samples.forEach { s ->
        if (s.id !in collectedSampleIds) {
          val rx = (s.posX - playerX) * radarScaleMeters
          val ry = -(s.posZ - playerZ) * radarScaleMeters
          if (sqrt(rx * rx + ry * ry) < radarRadius) {
            drawCircle(color = CosmicCyan, radius = 2.0f, center = Offset(radarCenter.x + rx, radarCenter.y + ry))
          }
        }
      }

      // 6.4 Mirino di Scansione Centrale Tattico
      val crosshairCenter = Offset(canvasW * 0.5f, canvasH * 0.5f)
      val isLocked = activeTargetSample != null || activeTargetPoi != null
      val crosshairColor = if (isLocked) AuroraGreen else CosmicCyan.copy(alpha = 0.75f)

      drawCircle(color = crosshairColor, radius = 16f, center = crosshairCenter, style = Stroke(width = 1.6f))
      drawCircle(color = if (isLocked) AuroraGreen else CosmicAmber, radius = 3f, center = crosshairCenter)

      // Tacche di mira
      drawLine(color = crosshairColor, start = Offset(crosshairCenter.x - 22f, crosshairCenter.y), end = Offset(crosshairCenter.x - 17f, crosshairCenter.y), strokeWidth = 2f)
      drawLine(color = crosshairColor, start = Offset(crosshairCenter.x + 17f, crosshairCenter.y), end = Offset(crosshairCenter.x + 22f, crosshairCenter.y), strokeWidth = 2f)
      drawLine(color = crosshairColor, start = Offset(crosshairCenter.x, crosshairCenter.y - 22f), end = Offset(crosshairCenter.x, crosshairCenter.y - 17f), strokeWidth = 2f)
      drawLine(color = crosshairColor, start = Offset(crosshairCenter.x, crosshairCenter.y + 17f), end = Offset(crosshairCenter.x, crosshairCenter.y + 22f), strokeWidth = 2f)

      // Badge Olografico se c'è un bersaglio agganciato
      if (activeTargetSample != null) {
        val s = activeTargetSample!!
        val badgeText = "🎯 ${s.name.uppercase()} • ${String.format("%.1f", targetDistanceMeters)}m"
        val subText = "FORMULA: ${s.spectrometryFormula}"
        val bMeas = textMeasurer.measure(badgeText, TextStyle(fontFamily = FredokaFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AuroraGreen))
        val sMeas = textMeasurer.measure(subText, TextStyle(fontFamily = FredokaFontFamily, fontSize = 7.5.sp, color = StarlightWhite))
        val bW = maxOf(bMeas.size.width, sMeas.size.width) + 18f
        val bH = bMeas.size.height + sMeas.size.height + 8f
        val bTopLeft = Offset(crosshairCenter.x - bW / 2f, crosshairCenter.y + 26f)

        drawRoundRect(color = Color(0xEE031510), topLeft = bTopLeft, size = Size(bW, bH), cornerRadius = CornerRadius(6f))
        drawRoundRect(color = AuroraGreen, topLeft = bTopLeft, size = Size(bW, bH), cornerRadius = CornerRadius(6f), style = Stroke(width = 1.2f))
        drawText(bMeas, topLeft = Offset(bTopLeft.x + 9f, bTopLeft.y + 3f))
        drawText(sMeas, topLeft = Offset(bTopLeft.x + 9f, bTopLeft.y + bMeas.size.height + 3f))
      } else if (activeTargetPoi != null) {
        val poi = activeTargetPoi!!
        val badgeText = "🏛️ SITO: ${poi.name.uppercase()} • ${String.format("%.1f", targetDistanceMeters)}m"
        val subText = "CATEGORIA: ${poi.poiType}"
        val bMeas = textMeasurer.measure(badgeText, TextStyle(fontFamily = FredokaFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicAmber))
        val sMeas = textMeasurer.measure(subText, TextStyle(fontFamily = FredokaFontFamily, fontSize = 7.5.sp, color = StarlightWhite))
        val bW = maxOf(bMeas.size.width, sMeas.size.width) + 18f
        val bH = bMeas.size.height + sMeas.size.height + 8f
        val bTopLeft = Offset(crosshairCenter.x - bW / 2f, crosshairCenter.y + 26f)

        drawRoundRect(color = Color(0xEE1E1303), topLeft = bTopLeft, size = Size(bW, bH), cornerRadius = CornerRadius(6f))
        drawRoundRect(color = CosmicAmber, topLeft = bTopLeft, size = Size(bW, bH), cornerRadius = CornerRadius(6f), style = Stroke(width = 1.2f))
        drawText(bMeas, topLeft = Offset(bTopLeft.x + 9f, bTopLeft.y + 3f))
        drawText(sMeas, topLeft = Offset(bTopLeft.x + 9f, bTopLeft.y + bMeas.size.height + 3f))
      }

      // 6.5 Indicatori Direzionali Off-Screen sulla Visiera per Astronave e POI
      // Calcola l'orientamento relativo (-180°..+180°) rispetto alla linea dello sguardo
      var leftStackCount = 0
      var rightStackCount = 0

      // A) Indicatore Off-Screen dell'Astronave AstroBloom-I
      if (!isInsideShip && shipDist > 4.2f) {
        val angleToShip = ((atan2(-playerX.toDouble(), -playerZ.toDouble()) * 180.0 / PI - playerYawDeg + 540.0) % 360.0) - 180.0
        if (abs(angleToShip) > 36.0) {
          val isLeft = angleToShip < 0
          val text = if (isLeft) "◀ 🚀 NAVE • ${shipDist.roundToInt()}m" else "🚀 NAVE • ${shipDist.roundToInt()}m ▶"
          val meas = textMeasurer.measure(
            text,
            TextStyle(fontFamily = FredokaFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CosmicAmber),
          )
          val pillW = meas.size.width + 16f
          val pillH = meas.size.height + 8f
          val pillX = if (isLeft) 28f else canvasW - 28f - pillW
          val pillY = canvasH * 0.42f + (if (isLeft) leftStackCount++ else rightStackCount++) * (pillH + 8f)

          drawRoundRect(
            color = Color(0xEE1E1303),
            topLeft = Offset(pillX, pillY),
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(6f),
          )
          drawRoundRect(
            color = CosmicAmber,
            topLeft = Offset(pillX, pillY),
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(6f),
            style = Stroke(width = 1.4f),
          )
          drawText(meas, topLeft = Offset(pillX + 8f, pillY + 4f))
        }
      }

      // B) Indicatori Off-Screen dei POI
      pois.forEach { poi ->
        val dx = poi.posX - playerX
        val dz = poi.posZ - playerZ
        val dist = sqrt(dx * dx + dz * dz)
        val angleToPoi = ((atan2(dx.toDouble(), dz.toDouble()) * 180.0 / PI - playerYawDeg + 540.0) % 360.0) - 180.0
        if (abs(angleToPoi) > 36.0 && (leftStackCount < 4 || rightStackCount < 4)) {
          val isLeft = angleToPoi < 0
          val shortName = poi.name.split(" ").take(2).joinToString(" ").take(14)
          val text = if (isLeft) "◀ 🎯 $shortName • ${dist.roundToInt()}m" else "🎯 $shortName • ${dist.roundToInt()}m ▶"
          val meas = textMeasurer.measure(
            text,
            TextStyle(fontFamily = FredokaFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = AuroraGreen),
          )
          val pillW = meas.size.width + 14f
          val pillH = meas.size.height + 7f
          val pillX = if (isLeft) 28f else canvasW - 28f - pillW
          val pillY = canvasH * 0.42f + (if (isLeft) leftStackCount++ else rightStackCount++) * (pillH + 8f)

          drawRoundRect(
            color = Color(0xEE021008),
            topLeft = Offset(pillX, pillY),
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(6f),
          )
          drawRoundRect(
            color = AuroraGreen,
            topLeft = Offset(pillX, pillY),
            size = Size(pillW, pillH),
            cornerRadius = CornerRadius(6f),
            style = Stroke(width = 1.3f),
          )
          drawText(meas, topLeft = Offset(pillX + 7f, pillY + 3.5f))
        }
      }

      } // Fine blocco rotate(rollSway)

      // =========================================================================
      // 7. VISIERA CASCO ASTRONAUTA EVA E TELEMETRIA A 360° (HELMET VISOR & HUD)
      // =========================================================================
      // 7.1 Vignettatura interna antiriflesso della visiera
      val visorCornerR = 48f
      drawRoundRect(
        color = Color(0x44000000),
        topLeft = Offset(-10f, -10f),
        size = Size(canvasW + 20f, canvasH + 20f),
        cornerRadius = CornerRadius(visorCornerR, visorCornerR),
        style = Stroke(width = 16f),
      )

      // 7.2 Arco di riflesso dorato in cima alla visiera
      val reflectPath = Path().apply {
        moveTo(canvasW * 0.15f, 18f)
        cubicTo(canvasW * 0.35f, 6f, canvasW * 0.65f, 6f, canvasW * 0.85f, 18f)
      }
      drawPath(
        path = reflectPath,
        color = CosmicAmber.copy(alpha = 0.20f),
        style = Stroke(width = 3.0f, cap = StrokeCap.Round),
      )

      // 7.3 Nastro Bussola a 360° in Cima alla Visiera
      val compassY = 32f
      val compassW = min(360f, canvasW * 0.42f)
      val compassLeft = (canvasW - compassW) / 2f

      drawRoundRect(
        color = Color(0xCC070B16),
        topLeft = Offset(compassLeft, compassY - 14f),
        size = Size(compassW, 28f),
        cornerRadius = CornerRadius(8f, 8f),
      )
      drawRoundRect(
        color = CosmicCyan.copy(alpha = 0.55f),
        topLeft = Offset(compassLeft, compassY - 14f),
        size = Size(compassW, 28f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.2f),
      )

      val centerHeading = ((playerYawDeg % 360f) + 360f) % 360f
      for (h in -4..4) {
        val headingVal = (((centerHeading + h * 15f) % 360f) + 360f) % 360f
        val tickX = canvasW * 0.5f + (h * 15f) * (compassW / 90f)
        if (tickX in (compassLeft + 6f)..(compassLeft + compassW - 6f)) {
          val isCardinal = headingVal.roundToInt() % 90 == 0
          drawLine(
            color = if (isCardinal) CosmicAmber else CosmicCyan.copy(alpha = 0.6f),
            start = Offset(tickX, compassY - (if (isCardinal) 10f else 6f)),
            end = Offset(tickX, compassY),
            strokeWidth = if (isCardinal) 2.2f else 1.2f,
          )

          val label = when (headingVal.roundToInt()) {
            0, 360 -> "N"
            45 -> "NE"
            90 -> "E"
            135 -> "SE"
            180 -> "S"
            225 -> "SW"
            270 -> "W"
            315 -> "NW"
            else -> "${headingVal.roundToInt()}°"
          }
          val meas = textMeasurer.measure(
            label,
            TextStyle(
              fontFamily = FredokaFontFamily,
              fontSize = if (isCardinal) 8.5.sp else 7.sp,
              fontWeight = if (isCardinal) FontWeight.ExtraBold else FontWeight.Bold,
              color = if (isCardinal) CosmicAmber else StarlightWhite.copy(alpha = 0.85f),
            ),
          )
          drawText(meas, topLeft = Offset(tickX - meas.size.width / 2f, compassY + 2f))
        }
      }

      // Mirino centrale fisso bussola
      drawLine(
        color = CosmicAmber,
        start = Offset(canvasW * 0.5f, compassY - 12f),
        end = Offset(canvasW * 0.5f, compassY + 12f),
        strokeWidth = 2.4f,
      )

      // 7.4 Segnale Faro Astronave sulla Bussola
      val bearingToShipDeg = ((atan2(-playerX, -playerZ) * 180f / PI.toFloat() + 360f) % 360f)
      val relShipAngle = ((bearingToShipDeg - playerYawDeg + 540f) % 360f) - 180f
      val shipBeaconX = (canvasW * 0.5f + relShipAngle * (compassW / 90f)).coerceIn(compassLeft + 12f, compassLeft + compassW - 12f)
      drawCircle(
        color = CosmicAmber,
        radius = 4.5f,
        center = Offset(shipBeaconX, compassY - 8f),
      )
    }

    // Controller Pad per lo Sbarco e Movimento a Piedi (112dp)
    if (!isInsideShip) {
      SurfaceMovementControllerPad(
        stickDeltaX = stickDeltaX,
        stickDeltaY = stickDeltaY,
        onDeltaChange = { dx, dy, active ->
          stickDeltaX = dx
          stickDeltaY = dy
          stickActive = active
        },
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 28.dp, bottom = 24.dp),
      )
    }

    // Pulsanti interattivi in sovraimpressione (Salto, Scansione/Raccolta, Sbarco e Fototeca NASA)
    SurfaceActionOverlay(
      isInsideShip = isInsideShip,
      shipDistance = shipDist,
      isGrounded = isGrounded,
      activeTargetSample = activeTargetSample,
      activeTargetPoi = activeTargetPoi,
      targetDistance = targetDistanceMeters,
      planetName = env.planetName,
      onDisembark = onDisembark,
      onBoardShip = onBoardShip,
      onJump = onTriggerJump,
      onInspectTarget = {
        if (activeTargetSample != null) {
          val s = activeTargetSample!!
          collectedSampleIds.add(s.id)
          val photo = s.associatedPhotoId?.let { pid -> SurfaceExplorationRepository.officialPhotos.firstOrNull { it.id == pid } }
          onCollectSample(s, photo)
        } else if (activeTargetPoi != null) {
          val p = activeTargetPoi!!
          discoveredPoiIds.add(p.id)
          val photo = p.associatedPhotoId?.let { pid -> SurfaceExplorationRepository.officialPhotos.firstOrNull { it.id == pid } }
          onInspectPoi(p, photo)
        }
      },
      onOpenPhotoGallery = onOpenPhotoGallery,
    )
  }
}

@Composable
private fun SurfaceActionOverlay(
  isInsideShip: Boolean,
  shipDistance: Float,
  isGrounded: Boolean,
  activeTargetSample: SurfaceSample?,
  activeTargetPoi: SurfacePoi?,
  targetDistance: Float,
  planetName: String,
  onDisembark: () -> Unit,
  onBoardShip: () -> Unit,
  onJump: () -> Unit,
  onInspectTarget: () -> Unit,
  onOpenPhotoGallery: () -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize()) {
    // 1. STATO: A BORDO DELL'ASTRONAVE -> Card di Sbarco Centrale per scendere
    if (isInsideShip) {
      Surface(
        color = Color(0xF00B132B),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, AuroraGreen),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 36.dp)
          .padding(horizontal = 24.dp),
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "🛰️ PORTELLONE APERTO • ${planetName.uppercase()}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AuroraGreen,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Camera d'equilibrio depressurizzata. Tuta EVA attiva. Sei in cima alla scaletta di discesa.",
            style = MaterialTheme.typography.bodySmall,
            color = StarlightWhite,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = onDisembark,
            colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
          ) {
            Icon(Icons.Rounded.ArrowUpward, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "🥾 SCENDI SULLA SUPERFICIE",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              softWrap = false,
            )
          }
        }
      }
    }

    // 2. Pulsante Fototeca Ufficiale NASA/ESA in alto a destra sotto il radar
    Button(
      onClick = onOpenPhotoGallery,
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xDD0B1229),
        contentColor = CosmicCyan,
      ),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(1.5.dp, CosmicCyan),
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 80.dp, end = 20.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.CameraAlt,
        contentDescription = "Foto Reali",
        modifier = Modifier.size(16.dp),
        tint = CosmicCyan,
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "FOTO REALI",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = StarlightWhite,
        maxLines = 1,
        softWrap = false,
      )
    }

    // 3. STATO: SULLA SUPERFICIE ALL'ESTERNO (Controlli Salto, Scansione e Risalita a Bordo)
    if (!isInsideShip) {
      Row(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(bottom = 22.dp, end = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Tasto per Risalire a Bordo se sei vicino alla scaletta dell'astronave (< 4.2 metri)
        if (shipDistance < 4.2f) {
          Button(
            onClick = onBoardShip,
            colors = ButtonDefaults.buttonColors(
              containerColor = CosmicAmber,
              contentColor = SpaceVoid,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
          ) {
            Icon(Icons.Rounded.ArrowUpward, contentDescription = null, modifier = Modifier.size(20.dp), tint = SpaceVoid)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "🚪 RISALI A BORDO",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = SpaceVoid,
              maxLines = 1,
              softWrap = false,
            )
          }
        }

        // Tasto Azione Contestuale (Scansiona/Raccogli) se vicino al bersaglio (< 4 metri)
        if ((activeTargetSample != null || activeTargetPoi != null) && targetDistance < 4.5f) {
          Button(
            onClick = onInspectTarget,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (activeTargetSample != null) CosmicCyan else AuroraGreen,
              contentColor = SpaceVoid,
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
          ) {
            Icon(
              imageVector = if (activeTargetSample != null) Icons.Rounded.Science else Icons.Rounded.Search,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
              tint = SpaceVoid,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (activeTargetSample != null) "RACCOGLI CAMPIONE" else "ISPEZIONA SITO",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = SpaceVoid,
              maxLines = 1,
              softWrap = false,
            )
          }
        }

        // Tasto Salto Gravitazionale (Jetpack Jump)
        Button(
          onClick = onJump,
          enabled = isGrounded,
          colors = ButtonDefaults.buttonColors(
            containerColor = CosmicAmber,
            contentColor = SpaceVoid,
            disabledContainerColor = Color(0x66451A03),
            disabledContentColor = Color.Gray,
          ),
          shape = CircleShape,
          contentPadding = PaddingValues(12.dp),
          modifier = Modifier.size(64.dp),
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.ArrowUpward,
              contentDescription = "Salto",
              modifier = Modifier.size(22.dp),
            )
            Text(
              text = "SALTO",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              maxLines = 1,
              softWrap = false,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SurfaceMovementControllerPad(
  stickDeltaX: Float,
  stickDeltaY: Float,
  onDeltaChange: (Float, Float, Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var controlMode by remember { mutableIntStateOf(0) } // 0: CLOCHE 3D, 1: D-PAD
  val density = LocalDensity.current
  val padBoxSizeDp = 112.dp
  val padBoxPx = with(density) { padBoxSizeDp.toPx() }
  val centerPx = padBoxPx / 2f
  val maxRadiusPx = padBoxPx * 0.42f
  val deadzonePx = with(density) { 5.dp.toPx() }

  val isStickActive = abs(stickDeltaX) > 0.02f || abs(stickDeltaY) > 0.02f

  fun updatePos(touchPos: Offset) {
    val dx = touchPos.x - centerPx
    val dy = touchPos.y - centerPx
    val dist = hypot(dx, dy)
    if (dist > deadzonePx) {
      val norm = ((dist - deadzonePx) / (maxRadiusPx - deadzonePx)).coerceIn(0f, 1f)
      val curved = (norm * norm * 0.70f + norm * 0.30f).coerceIn(0f, 1f)
      val ang = atan2(dy, dx)
      val sx = (cos(ang) * curved).coerceIn(-1f, 1f)
      val sy = (sin(ang) * curved).coerceIn(-1f, 1f)
      onDeltaChange(sx, sy, true)
    } else {
      onDeltaChange(0f, 0f, true)
    }
  }

  fun resetPos() {
    onDeltaChange(0f, 0f, false)
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    // Selettore Rapido Modalità: [CLOCHE] vs [D-PAD]
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        onClick = { controlMode = 0 },
        color = if (controlMode == 0) CosmicCyan.copy(alpha = 0.35f) else SpaceVoid.copy(alpha = 0.75f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (controlMode == 0) CosmicCyan else Color.Transparent),
        modifier = Modifier.height(20.dp),
      ) {
        Text(
          text = "CLOCHE",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
          color = if (controlMode == 0) StarlightWhite else StarlightWhite.copy(alpha = 0.6f),
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          maxLines = 1,
          softWrap = false,
        )
      }
      Surface(
        onClick = { controlMode = 1 },
        color = if (controlMode == 1) AuroraGreen.copy(alpha = 0.35f) else SpaceVoid.copy(alpha = 0.75f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, if (controlMode == 1) AuroraGreen else Color.Transparent),
        modifier = Modifier.height(20.dp),
      ) {
        Text(
          text = "D-PAD",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
          color = if (controlMode == 1) StarlightWhite else StarlightWhite.copy(alpha = 0.6f),
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          maxLines = 1,
          softWrap = false,
        )
      }
    }

    if (controlMode == 0) {
      // Cloche 3D Analogica con Ritorno a Molla (112dp)
      Box(
        modifier = Modifier
          .size(padBoxSizeDp)
          .background(Color(0xD90A1128), CircleShape)
          .border(
            2.dp,
            Brush.radialGradient(
              listOf(
                if (isStickActive) CosmicAmber else CosmicCyan,
                Color(0xFF14203D),
              )
            ),
            CircleShape,
          )
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = { offset ->
                updatePos(offset)
                tryAwaitRelease()
                resetPos()
              }
            )
          }
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = { offset -> updatePos(offset) },
              onDragEnd = { resetPos() },
              onDragCancel = { resetPos() },
            ) { change, _ ->
              change.consume()
              updatePos(change.position)
            }
          }
          .testTag("surface_movement_stick"),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val c = Offset(size.width / 2f, size.height / 2f)

          // Cerchi concentrici al neon e reticolo
          drawCircle(
            color = CosmicCyan.copy(alpha = if (isStickActive) 0.65f else 0.25f),
            radius = size.minDimension * 0.46f,
            style = Stroke(width = if (isStickActive) 2.2f else 1.6f),
          )
          drawCircle(
            color = CosmicCyan.copy(alpha = 0.16f),
            radius = size.minDimension * 0.24f,
            style = Stroke(width = 1f),
          )
          drawLine(
            color = CosmicCyan.copy(alpha = 0.35f),
            start = Offset(c.x - 10f, c.y),
            end = Offset(c.x + 10f, c.y),
            strokeWidth = 1.2f,
          )
          drawLine(
            color = CosmicCyan.copy(alpha = 0.35f),
            start = Offset(c.x, c.y - 10f),
            end = Offset(c.x, c.y + 10f),
            strokeWidth = 1.2f,
          )

          // Indicatori direzionali luminosi
          val upColor = if (stickDeltaY < -0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.45f)
          val downColor = if (stickDeltaY > 0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.45f)
          val leftColor = if (stickDeltaX < -0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.45f)
          val rightColor = if (stickDeltaX > 0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.45f)

          drawLine(color = upColor, start = Offset(c.x, 6f), end = Offset(c.x, 16f), strokeWidth = if (stickDeltaY < -0.2f) 3f else 1.8f)
          drawLine(color = downColor, start = Offset(c.x, size.height - 6f), end = Offset(c.x, size.height - 16f), strokeWidth = if (stickDeltaY > 0.2f) 3f else 1.8f)
          drawLine(color = leftColor, start = Offset(6f, c.y), end = Offset(16f, c.y), strokeWidth = if (stickDeltaX < -0.2f) 3f else 1.8f)
          drawLine(color = rightColor, start = Offset(size.width - 6f, c.y), end = Offset(size.width - 16f, c.y), strokeWidth = if (stickDeltaX > 0.2f) 3f else 1.8f)

          // Raggio laser dinamico dal centro alla manopola
          val knob = Offset(c.x + stickDeltaX * maxRadiusPx, c.y + stickDeltaY * maxRadiusPx)
          if (isStickActive) {
            drawLine(
              color = CosmicCyan.copy(alpha = 0.70f),
              start = c,
              end = knob,
              strokeWidth = 2.6f,
            )
          }

          // Manopola 3D della cloche
          drawCircle(
            brush = Brush.radialGradient(
              listOf(
                if (isStickActive) CosmicAmber else Color(0xFFFFB703),
                Color(0xFFE76F51),
              )
            ),
            radius = 16.dp.toPx(),
            center = knob,
          )
          drawCircle(
            color = ComicInk,
            radius = 16.dp.toPx(),
            center = knob,
            style = Stroke(width = 2.2f),
          )
          drawCircle(
            color = StarlightWhite,
            radius = 3.5.dp.toPx(),
            center = Offset(knob.x - 3.5.dp.toPx(), knob.y - 3.5.dp.toPx()),
          )
        }
      }
    } else {
      // D-Pad Tattico (112dp)
      Box(
        modifier = Modifier
          .size(padBoxSizeDp)
          .background(Color(0xD90A1128), RoundedCornerShape(16.dp))
          .border(2.dp, Brush.radialGradient(listOf(AuroraGreen, Color(0xFF14203D))), RoundedCornerShape(16.dp))
          .testTag("surface_movement_stick"),
        contentAlignment = Alignment.Center,
      ) {
        // Tasto AVANTI
        Surface(
          color = if (stickDeltaY < -0.2f) CosmicAmber else SpaceVoid.copy(alpha = 0.8f),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.2.dp, if (stickDeltaY < -0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.6f)),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 4.dp)
            .size(width = 50.dp, height = 32.dp)
            .pointerInput(Unit) {
              detectTapGestures(
                onPress = {
                  onDeltaChange(0f, -0.85f, true)
                  tryAwaitRelease()
                  resetPos()
                }
              )
            },
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("▲ AVANTI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = StarlightWhite, maxLines = 1, softWrap = false)
          }
        }

        // Tasto INDIETRO
        Surface(
          color = if (stickDeltaY > 0.2f) CosmicAmber else SpaceVoid.copy(alpha = 0.8f),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.2.dp, if (stickDeltaY > 0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.6f)),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 4.dp)
            .size(width = 50.dp, height = 32.dp)
            .pointerInput(Unit) {
              detectTapGestures(
                onPress = {
                  onDeltaChange(0f, 0.85f, true)
                  tryAwaitRelease()
                  resetPos()
                }
              )
            },
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("▼ RETRO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = StarlightWhite, maxLines = 1, softWrap = false)
          }
        }

        // Tasto SX
        Surface(
          color = if (stickDeltaX < -0.2f) CosmicAmber else SpaceVoid.copy(alpha = 0.8f),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.2.dp, if (stickDeltaX < -0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.6f)),
          modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 4.dp)
            .size(width = 38.dp, height = 40.dp)
            .pointerInput(Unit) {
              detectTapGestures(
                onPress = {
                  onDeltaChange(-0.85f, 0f, true)
                  tryAwaitRelease()
                  resetPos()
                }
              )
            },
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("◀ SX", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = StarlightWhite, maxLines = 1, softWrap = false)
          }
        }

        // Tasto DX
        Surface(
          color = if (stickDeltaX > 0.2f) CosmicAmber else SpaceVoid.copy(alpha = 0.8f),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.2.dp, if (stickDeltaX > 0.2f) CosmicAmber else CosmicCyan.copy(alpha = 0.6f)),
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 4.dp)
            .size(width = 38.dp, height = 40.dp)
            .pointerInput(Unit) {
              detectTapGestures(
                onPress = {
                  onDeltaChange(0.85f, 0f, true)
                  tryAwaitRelease()
                  resetPos()
                }
              )
            },
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("DX ▶", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = StarlightWhite, maxLines = 1, softWrap = false)
          }
        }

        // Centro D-Pad
        Box(
          modifier = Modifier
            .size(20.dp)
            .background(Color(0xFF0F1A35), CircleShape)
            .border(1.dp, AuroraGreen.copy(alpha = 0.6f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Rounded.CenterFocusStrong, contentDescription = null, tint = AuroraGreen, modifier = Modifier.size(12.dp))
        }
      }
    }
  }
}
