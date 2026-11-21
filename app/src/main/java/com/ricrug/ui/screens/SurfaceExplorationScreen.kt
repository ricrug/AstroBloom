package com.ricrug.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.SubcomposeAsyncImage
import com.ricrug.data.OfficialPhotoRecord
import com.ricrug.data.SurfaceExplorationRepository
import com.ricrug.data.SurfacePoi
import com.ricrug.data.SurfaceSample
import com.ricrug.model.ChibiExpression
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.components.ChibiAvatar
import com.ricrug.ui.components.ChibiComicBubble
import com.ricrug.ui.components.SurfaceFirstPersonViewer3D
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.FredokaFontFamily
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceCard
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }

@Composable
fun SurfaceExplorationScreen(
  planetId: String,
  chibiProfile: ChibiProfile,
  savedTotalXp: Int = 0,
  initialDiscoveredPoiIds: Set<String> = emptySet(),
  initialCollectedSampleIds: Set<String> = emptySet(),
  onRecordPoiDiscovered: (String, Int) -> Unit = { _, _ -> },
  onRecordSampleCollected: (String, Int) -> Unit = { _, _ -> },
  onExitExploration: () -> Unit,
  onMarkPlanetExplored: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val view = LocalView.current

  BackHandler { onExitExploration() }

  // Modalità orizzontale immersiva a schermo intero (Landscape)
  DisposableEffect(Unit) {
    val activity = context.findActivity()
    val previousOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val window = activity?.window
    val controller = if (window != null) {
      runCatching { WindowCompat.getInsetsController(window, view) }.getOrNull()
    } else {
      null
    }

    if (activity != null) {
      runCatching { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE }
    }
    controller?.let {
      runCatching {
        it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        it.hide(WindowInsetsCompat.Type.systemBars())
      }
    }

    onDispose {
      controller?.let { runCatching { it.show(WindowInsetsCompat.Type.systemBars()) } }
      if (activity != null) {
        runCatching { activity.requestedOrientation = previousOrientation }
      }
    }
  }

  // Dati dell'ambiente e foto ufficiali
  val env = remember(planetId) { SurfaceExplorationRepository.getEnvironmentConfig(planetId) }
  val photos = remember(planetId) { SurfaceExplorationRepository.getPhotosForPlanet(planetId) }

  // Stato modali
  var inspectedPoi by remember { mutableStateOf<SurfacePoi?>(null) }
  var inspectedPoiPhoto by remember { mutableStateOf<OfficialPhotoRecord?>(null) }

  var collectedSample by remember { mutableStateOf<SurfaceSample?>(null) }
  var collectedSamplePhoto by remember { mutableStateOf<OfficialPhotoRecord?>(null) }

  var showPhotoGallery by remember { mutableStateOf(false) }
  var selectedGalleryPhoto by remember { mutableStateOf<OfficialPhotoRecord?>(photos.firstOrNull()) }

  var totalXp by remember(savedTotalXp) { mutableIntStateOf(savedTotalXp) }
  val discoveredPoiIds = remember(initialDiscoveredPoiIds) {
    mutableStateListOf<String>().apply { addAll(initialDiscoveredPoiIds) }
  }
  val collectedSampleIds = remember(initialCollectedSampleIds) {
    mutableStateListOf<String>().apply { addAll(initialCollectedSampleIds) }
  }

  Box(modifier = modifier.fillMaxSize().background(SpaceVoid)) {
    // 1. Viewer 3D in Prima Persona della Superficie
    SurfaceFirstPersonViewer3D(
      planetId = planetId,
      chibiProfile = chibiProfile,
      initialCollectedSampleIds = initialCollectedSampleIds,
      initialDiscoveredPoiIds = initialDiscoveredPoiIds,
      onInspectPoi = { poi, photo ->
        inspectedPoi = poi
        inspectedPoiPhoto = photo
        if (poi.id !in discoveredPoiIds) {
          discoveredPoiIds.add(poi.id)
          totalXp += poi.xpValue
          onRecordPoiDiscovered(poi.id, poi.xpValue)
        }
        onMarkPlanetExplored(planetId)
      },
      onCollectSample = { sample, photo ->
        collectedSample = sample
        collectedSamplePhoto = photo
        if (sample.id !in collectedSampleIds) {
          collectedSampleIds.add(sample.id)
          totalXp += sample.xpValue
          onRecordSampleCollected(sample.id, sample.xpValue)
        }
        onMarkPlanetExplored(planetId)
      },
      onOpenPhotoGallery = {
        selectedGalleryPhoto = photos.firstOrNull()
        showPhotoGallery = true
      },
      modifier = Modifier.fillMaxSize(),
    )

    // 2. Barra Superiore: Pulsante Rientra al Lander + Score XP
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, start = 18.dp, end = 18.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Tasto Ritorna al Modulo di Discesa
      Button(
        onClick = onExitExploration,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xDD0B1229),
          contentColor = StarlightWhite,
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, CosmicCyan.copy(alpha = 0.7f)),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
      ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Rientro", modifier = Modifier.size(16.dp), tint = CosmicCyan)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "RIENTRA AL LANDER",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          softWrap = false,
        )
      }

      // Badge XP Esploratore Planetario
      Surface(
        color = Color(0xDD0B1229),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, CosmicAmber.copy(alpha = 0.8f)),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Rounded.Star, contentDescription = null, tint = CosmicAmber, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "XP SCIENTIFICI: $totalXp",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = CosmicAmber,
          )
        }
      }
    }

    // =========================================================================
    // MODALE 1: ISPEZIONE PUNTO DI INTERESSE STORICO / SCIENTIFICO (POI)
    // =========================================================================
    if (inspectedPoi != null) {
      Dialog(
        onDismissRequest = { inspectedPoi = null },
        properties = DialogProperties(usePlatformDefaultWidth = false),
      ) {
        Surface(
          color = SpaceNavy.copy(alpha = 0.96f),
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(2.dp, AuroraGreen),
          modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.88f)
            .padding(12.dp),
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
          ) {
            // Intestazione con pulsante di chiusura
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  color = AuroraGreen.copy(alpha = 0.2f),
                  shape = CircleShape,
                  border = BorderStroke(1.5.dp, AuroraGreen),
                  modifier = Modifier.size(36.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Public, contentDescription = null, tint = AuroraGreen, modifier = Modifier.size(20.dp))
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = inspectedPoi!!.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StarlightWhite,
                  )
                  Text(
                    text = "CATEGORIA: ${inspectedPoi!!.poiType.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraGreen,
                  )
                }
              }

              IconButton(onClick = { inspectedPoi = null }) {
                Icon(Icons.Rounded.Close, contentDescription = "Chiudi", tint = StarlightMuted)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Corpo: Descrizione e Foto Reale Ufficiale della Missione
            Row(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              // Colonna Sinistra: Foto Reale Documentata con Badge Agenzia Spaziale
              if (inspectedPoiPhoto != null) {
                Card(
                  shape = RoundedCornerShape(14.dp),
                  border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.7f)),
                  colors = CardDefaults.cardColors(containerColor = Color.Black),
                  modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                ) {
                  Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    ) {
                      val poiDrawable = inspectedPoiPhoto!!.drawableRes
                      if (poiDrawable != null && poiDrawable != 0) {
                        Image(
                          painter = painterResource(id = poiDrawable),
                          contentDescription = inspectedPoiPhoto!!.title,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize(),
                        )
                      } else {
                        SubcomposeAsyncImage(
                          model = inspectedPoiPhoto!!.imageUrl,
                          contentDescription = inspectedPoiPhoto!!.title,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize(),
                          loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                              CircularProgressIndicator(color = CosmicCyan, modifier = Modifier.size(32.dp))
                            }
                          },
                          error = {
                            // Fallback grafico scientifico elegante se offline
                            Box(
                              modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))),
                              contentAlignment = Alignment.Center,
                            ) {
                              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(inspectedPoiPhoto!!.mission, color = CosmicAmber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Archivio Storico Ufficiale", color = StarlightMuted, style = MaterialTheme.typography.bodySmall)
                              }
                            }
                          },
                        )
                      }

                      // Badge Missione in sovrimpressione
                      Surface(
                        color = Color(0xDD0B1229),
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart),
                      ) {
                        Text(
                          text = "📸 ${inspectedPoiPhoto!!.mission} (${inspectedPoiPhoto!!.agency})",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = CosmicAmber,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                      }
                    }

                    // Didascalia foto
                    Surface(
                      color = Color(0xEE0B1229),
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                          text = inspectedPoiPhoto!!.title,
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = FontWeight.Bold,
                          color = StarlightWhite,
                        )
                        Text(
                          text = "Data: ${inspectedPoiPhoto!!.captureDate} • ${inspectedPoiPhoto!!.credit}",
                          style = MaterialTheme.typography.bodySmall,
                          fontSize = 8.sp,
                          color = StarlightMuted,
                        )
                      }
                    }
                  }
                }
              }

              // Colonna Destra: Informazioni Scientifiche e Suggerimento Ufficiale
              LazyColumn(
                modifier = Modifier
                  .weight(1.2f)
                  .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                item {
                  Text(
                    text = inspectedPoi!!.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarlightWhite,
                  )
                }

                if (inspectedPoiPhoto != null) {
                  item {
                    // Box Suggerimento Scientifico Ufficiale
                    Surface(
                      color = Color(0xDD031B15),
                      shape = RoundedCornerShape(12.dp),
                      border = BorderStroke(1.2.dp, AuroraGreen),
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = AuroraGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                          Text(
                            text = "💡 SUGGERIMENTO DELLA MISSIONE:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AuroraGreen,
                          )
                          Spacer(modifier = Modifier.height(2.dp))
                          Text(
                            text = inspectedPoiPhoto!!.scientificTip,
                            style = MaterialTheme.typography.bodySmall,
                            color = StarlightWhite,
                          )
                        }
                      }
                    }
                  }

                  item {
                    Text(
                      text = "DOCUMENTAZIONE SCIENTIFICA:\n${inspectedPoiPhoto!!.caption}",
                      style = MaterialTheme.typography.bodySmall,
                      color = StarlightMuted,
                    )
                  }
                }

                item {
                  // Commento del copilota Chibi
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    ChibiAvatar(profile = chibiProfile, size = 48.dp, expression = ChibiExpression.AMAZED)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "«Comandante, abbiamo documentato questo sito storico! +${inspectedPoi!!.xpValue} XP scientifici registrati nel diario!»",
                      style = MaterialTheme.typography.bodySmall,
                      fontFamily = FredokaFontFamily,
                      color = CosmicCyan,
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer con pulsanti
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
            ) {
              Button(
                onClick = { inspectedPoi = null },
                colors = ButtonDefaults.buttonColors(containerColor = AuroraGreen, contentColor = ComicInk),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text("CONTINUA ESPLORAZIONE", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
              }
            }
          }
        }
      }
    }

    // =========================================================================
    // MODALE 2: RACCOLTA CAMPIONE GEOLOGICO & ANALISI SPETTROMETRICA
    // =========================================================================
    if (collectedSample != null) {
      Dialog(
        onDismissRequest = { collectedSample = null },
        properties = DialogProperties(usePlatformDefaultWidth = false),
      ) {
        Surface(
          color = SpaceNavy.copy(alpha = 0.96f),
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(2.dp, CosmicCyan),
          modifier = Modifier
            .fillMaxWidth(0.88f)
            .fillMaxHeight(0.85f)
            .padding(12.dp),
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
          ) {
            // Titolo Raccolta Campione
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  color = CosmicCyan.copy(alpha = 0.2f),
                  shape = CircleShape,
                  border = BorderStroke(1.5.dp, CosmicCyan),
                  modifier = Modifier.size(36.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Science, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(20.dp))
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "CAMPIONE RACCOLTO: ${collectedSample!!.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StarlightWhite,
                  )
                  Text(
                    text = "CLASSE: ${collectedSample!!.mineralClass} • RARITÀ: ${collectedSample!!.rarity.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicCyan,
                  )
                }
              }

              IconButton(onClick = { collectedSample = null }) {
                Icon(Icons.Rounded.Close, contentDescription = "Chiudi", tint = StarlightMuted)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dettagli Spettrometrici e Foto Reale collegata
            Row(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              // Se c'è una foto reale della missione associata
              if (collectedSamplePhoto != null) {
                Card(
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.2.dp, CosmicAmber.copy(alpha = 0.7f)),
                  colors = CardDefaults.cardColors(containerColor = Color.Black),
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                ) {
                  Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                      val sampleDrawable = collectedSamplePhoto!!.drawableRes
                      if (sampleDrawable != null && sampleDrawable != 0) {
                        Image(
                          painter = painterResource(id = sampleDrawable),
                          contentDescription = collectedSamplePhoto!!.title,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize(),
                        )
                      } else {
                        SubcomposeAsyncImage(
                          model = collectedSamplePhoto!!.imageUrl,
                          contentDescription = collectedSamplePhoto!!.title,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize(),
                          loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                              CircularProgressIndicator(color = CosmicAmber, modifier = Modifier.size(28.dp))
                            }
                          },
                        )
                      }
                      Surface(
                        color = Color(0xDD0B1229),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart),
                      ) {
                        Text(
                          text = "🔬 Foto Reale: ${collectedSamplePhoto!!.mission}",
                          style = MaterialTheme.typography.labelSmall,
                          color = CosmicAmber,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                      }
                    }
                    Text(
                      text = collectedSamplePhoto!!.title,
                      style = MaterialTheme.typography.labelSmall,
                      color = StarlightWhite,
                      modifier = Modifier.padding(6.dp),
                    )
                  }
                }
              }

              // Pannello Risultati Laboratorio Spettrometrico
              LazyColumn(
                modifier = Modifier
                  .weight(1.3f)
                  .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                item {
                  Surface(
                    color = Color(0xDD081427),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                      Text("ANALISI SPETTROMETRICA (XRF & ChemCam):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CosmicCyan)
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(collectedSample!!.spectrometryFormula, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CosmicAmber)
                    }
                  }
                }

                item {
                  Text(
                    text = "SCOPERTA SCIENTIFICA:\n${collectedSample!!.scientificDiscovery}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarlightWhite,
                  )
                }

                if (collectedSamplePhoto != null) {
                  item {
                    Surface(
                      color = Color(0xDD031B15),
                      shape = RoundedCornerShape(10.dp),
                      border = BorderStroke(1.dp, AuroraGreen),
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Column(modifier = Modifier.padding(8.dp)) {
                        Text("💡 SUGGERIMENTO SCIENTIFICO:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AuroraGreen)
                        Text(collectedSamplePhoto!!.scientificTip, style = MaterialTheme.typography.bodySmall, color = StarlightWhite)
                      }
                    }
                  }
                }

                item {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    ChibiAvatar(profile = chibiProfile, size = 44.dp, expression = ChibiExpression.CHEERING)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "«Fantastico reperto! +${collectedSample!!.xpValue} XP scientifici aggiunti al diario di bordo!»",
                      style = MaterialTheme.typography.bodySmall,
                      fontFamily = FredokaFontFamily,
                      color = AuroraGreen,
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
            ) {
              Button(
                onClick = { collectedSample = null },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = SpaceVoid),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text("ARCHIVIA CAMPIONE", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
              }
            }
          }
        }
      }
    }

    // =========================================================================
    // MODALE 3: FOTOTECA UFFICIALE & SUGGERIMENTI DELLA MISSIONE (NASA/ESA)
    // =========================================================================
    if (showPhotoGallery) {
      Dialog(
        onDismissRequest = { showPhotoGallery = false },
        properties = DialogProperties(usePlatformDefaultWidth = false),
      ) {
        Surface(
          color = SpaceNavy.copy(alpha = 0.98f),
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(2.dp, CosmicCyan),
          modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.92f)
            .padding(10.dp),
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(14.dp),
          ) {
            // Intestazione Fototeca
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "ARCHIVIO FOTOGRAFICO UFFICIALE NASA / ESA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = StarlightWhite,
                  )
                  Text(
                    text = "PIANETA: ${env.planetName.uppercase()} • FOTO REALI DOCUMENTATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicCyan,
                  )
                }
              }

              IconButton(onClick = { showPhotoGallery = false }) {
                Icon(Icons.Rounded.Close, contentDescription = "Chiudi", tint = StarlightMuted)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Corpo della Fototeca: Visualizzatore Centrale + Elenco Foto Selezionabili
            Row(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              // Selettore Foto a Sinistra
              LazyColumn(
                modifier = Modifier
                  .width(180.dp)
                  .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                items(photos) { photo ->
                  val isSelected = selectedGalleryPhoto?.id == photo.id
                  Card(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) CosmicAmber else CosmicCyan.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B1229)),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { selectedGalleryPhoto = photo },
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Text(
                        text = photo.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CosmicAmber else StarlightWhite,
                        maxLines = 2,
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = "${photo.mission} (${photo.agency})",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 8.sp,
                        color = StarlightMuted,
                      )
                    }
                  }
                }
              }

              // Visualizzatore Grande della Foto Selezionata a Destra
              if (selectedGalleryPhoto != null) {
                val p = selectedGalleryPhoto!!
                Row(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  // Immagine Ufficiale Grande
                  Card(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.6f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                      .weight(1.2f)
                      .fillMaxHeight(),
                  ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                      val galleryDrawable = p.drawableRes
                      if (galleryDrawable != null && galleryDrawable != 0) {
                        Image(
                          painter = painterResource(id = galleryDrawable),
                          contentDescription = p.title,
                          contentScale = ContentScale.Fit,
                          modifier = Modifier.fillMaxSize(),
                        )
                      } else {
                        SubcomposeAsyncImage(
                          model = p.imageUrl,
                          contentDescription = p.title,
                          contentScale = ContentScale.Fit,
                          modifier = Modifier.fillMaxSize(),
                          loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                              CircularProgressIndicator(color = CosmicCyan, modifier = Modifier.size(36.dp))
                            }
                          },
                          error = {
                            Box(
                              modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A)),
                              contentAlignment = Alignment.Center,
                            ) {
                              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(p.mission, color = CosmicAmber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("Archivio Documentale Ufficiale", color = StarlightMuted, style = MaterialTheme.typography.bodySmall)
                              }
                            }
                          },
                        )
                      }

                      Surface(
                        color = Color(0xDD0B1229),
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart),
                      ) {
                        Text(
                          text = "🛰️ ${p.mission} • Data: ${p.captureDate}",
                          style = MaterialTheme.typography.labelSmall,
                          color = CosmicAmber,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                      }
                    }
                  }

                  // Descrizione e Suggerimento dell'Esperto
                  LazyColumn(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    item {
                      Text(
                        text = p.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StarlightWhite,
                      )
                      Text(
                        text = "Credit: ${p.credit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 8.sp,
                        color = StarlightMuted,
                      )
                    }

                    item {
                      Surface(
                        color = Color(0xDD031B15),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, AuroraGreen),
                        modifier = Modifier.fillMaxWidth(),
                      ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                          Icon(Icons.Rounded.Info, contentDescription = null, tint = AuroraGreen, modifier = Modifier.size(18.dp))
                          Spacer(modifier = Modifier.width(6.dp))
                          Column {
                            Text(
                              text = "💡 SUGGERIMENTO SCIENTIFICO:",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.Bold,
                              color = AuroraGreen,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                              text = p.scientificTip,
                              style = MaterialTheme.typography.bodySmall,
                              color = StarlightWhite,
                            )
                          }
                        }
                      }
                    }

                    item {
                      Text(
                        text = "ANALISI UFFICIALE:\n${p.caption}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarlightMuted,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
