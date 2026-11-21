package com.ricrug.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ricrug.data.SolarSystemData
import com.ricrug.model.CelestialBody
import com.ricrug.model.ChibiExpression
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.components.ChibiComicBubble
import com.ricrug.ui.components.MiniPlanet3DOrb
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

@Composable
fun PlanetsHomeScreen(
  chibiProfile: ChibiProfile,
  exploredPlanetIds: Set<String>,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenFreeExplore: () -> Unit,
  onOpenChibiCustomizer: () -> Unit,
  onOpenAsteroidDodge: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val bodies = SolarSystemData.celestialBodies
  val progress = exploredPlanetIds.size.toFloat() / bodies.size.toFloat()

  Box(
    modifier = modifier.fillMaxSize().background(SpaceVoid),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 680.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Comic Chibi Welcome Header
      item {
        ChibiComicBubble(
          profile = chibiProfile,
          expression = ChibiExpression.HAPPY,
          badgeText = "GUIDA SPAZIALE A FUMETTI",
          headline = "Ciao esploratore! Parti con me?",
          dialogue =
            "Sono ${chibiProfile.name}! Tocca un pianeta qui sotto per farlo girare in 3D, oppure vola libero nello spazio!",
          onAvatarClick = onOpenChibiCustomizer,
          footerContent = {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedButton(
                onClick = onOpenChibiCustomizer,
                border = BorderStroke(1.5.dp, ComicInk),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("customize_chibi_button"),
              ) {
                Icon(
                  Icons.Rounded.Edit,
                  contentDescription = null,
                  tint = ComicInk,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Il Mio Personaggio", color = ComicInk, style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
              }
              Button(
                onClick = onOpenFreeExplore,
                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan, contentColor = ComicInk),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("launch_free_explore_button"),
              ) {
                Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Vola in 3D", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
              }
            }
          },
        )
      }

      // Explorer Progress Strip
      item {
        Surface(
          color = SpaceCard,
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.5.dp, SpaceOutline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Diario di Bordo",
                style = MaterialTheme.typography.titleMedium,
                color = StarlightWhite,
              )
              Text(
                text = "${exploredPlanetIds.size}/${bodies.size} mondi esplorati",
                style = MaterialTheme.typography.labelMedium,
                color = CosmicAmber,
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { progress },
              color = AuroraGreen,
              trackColor = SpaceNavy,
              modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Mini Solar System in Order Strip
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              bodies.forEach { body ->
                val explored = exploredPlanetIds.contains(body.id)
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier =
                    Modifier.clip(RoundedCornerShape(12.dp))
                      .clickable { onOpenPlanetDetail(body.id) }
                      .padding(4.dp)
                      .testTag("quick_strip_${body.id}"),
                ) {
                  Box(
                    modifier =
                      Modifier.size(44.dp)
                        .clip(CircleShape)
                        .background(
                          if (explored) AuroraGreen.copy(alpha = 0.16f) else SpaceNavy,
                        ),
                    contentAlignment = Alignment.Center,
                  ) {
                    MiniPlanet3DOrb(body = body, size = 38.dp)
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = body.name.removePrefix("Il ").removePrefix("La "),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (explored) AuroraGreen else StarlightMuted,
                    maxLines = 1,
                  )
                }
              }
            }
          }
        }
      }

      // Free Exploration Hero Banner
      item {
        Surface(
          onClick = onOpenFreeExplore,
          shape = RoundedCornerShape(22.dp),
          border = BorderStroke(2.dp, CosmicCyan.copy(alpha = 0.7f)),
          color = Color.Transparent,
          modifier = Modifier.fillMaxWidth().testTag("banner_free_explore"),
        ) {
          Box(
            modifier =
              Modifier.background(
                  Brush.horizontalGradient(
                    colors = listOf(Color(0xFF142861), Color(0xFF281652)),
                  )
                )
                .padding(16.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Surface(
                  color = CosmicCyan,
                  shape = RoundedCornerShape(8.dp),
                ) {
                  Text(
                    text = "MODALITÀ 3D LIBERA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ComicInk,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Naviga nello Spazio!",
                  style = MaterialTheme.typography.titleLarge,
                  color = StarlightWhite,
                )
                Text(
                  text = "Guida la navicella tra asteroidi, orbite 3D e pianeti in movimento!",
                  style = MaterialTheme.typography.bodyMedium,
                  color = StarlightMuted,
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Surface(
                color = CosmicAmber,
                shape = CircleShape,
                border = BorderStroke(2.dp, ComicInk),
                modifier = Modifier.size(52.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Rounded.Explore,
                    contentDescription = "Esplorazione libera",
                    tint = ComicInk,
                    modifier = Modifier.size(28.dp),
                  )
                }
              }
            }
          }
        }
      }

      // Minigioco Arcade Banner: Scansa Asteroidi
      item {
        Surface(
          onClick = onOpenAsteroidDodge,
          shape = RoundedCornerShape(22.dp),
          border = BorderStroke(2.dp, NebulaMagenta.copy(alpha = 0.8f)),
          color = Color.Transparent,
          modifier = Modifier.fillMaxWidth().testTag("banner_asteroid_dodge"),
        ) {
          Box(
            modifier =
              Modifier.background(
                  Brush.horizontalGradient(
                    colors = listOf(Color(0xFF3B1238), Color(0xFF1B1440)),
                  )
                )
                .padding(16.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Surface(
                  color = NebulaMagenta,
                  shape = RoundedCornerShape(8.dp),
                ) {
                  Text(
                    text = "NUOVO MINIGIOCO ARCADE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = StarlightWhite,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Corsa Spaziale: Scansa Asteroidi! 🚀",
                  style = MaterialTheme.typography.titleLarge,
                  color = StarlightWhite,
                )
                Text(
                  text = "Volo a scorrimento verticale! Schiva rocce spaziali, raccogli power-up e spara ai meteoriti!",
                  style = MaterialTheme.typography.bodyMedium,
                  color = StarlightMuted,
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Surface(
                color = NebulaMagenta,
                shape = CircleShape,
                border = BorderStroke(2.dp, ComicInk),
                modifier = Modifier.size(52.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Rounded.RocketLaunch,
                    contentDescription = "Scansa Asteroidi",
                    tint = StarlightWhite,
                    modifier = Modifier.size(28.dp),
                  )
                }
              }
            }
          }
        }
      }

      // Section Header
      item {
        Text(
          text = "I Mondi del Sistema Solare",
          style = MaterialTheme.typography.headlineSmall,
          color = StarlightWhite,
          modifier = Modifier.padding(top = 4.dp),
        )
      }

      // Planet Cards
      items(bodies, key = { it.id }) { body ->
        PlanetCatalogCard(
          body = body,
          isExplored = exploredPlanetIds.contains(body.id),
          onClick = { onOpenPlanetDetail(body.id) },
        )
      }

      item { Spacer(modifier = Modifier.height(12.dp)) }
    }
  }
}

@Composable
private fun PlanetCatalogCard(
  body: CelestialBody,
  isExplored: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    color = SpaceCard,
    shape = RoundedCornerShape(22.dp),
    border =
      BorderStroke(
        width = if (isExplored) 2.dp else 1.5.dp,
        color = if (isExplored) AuroraGreen.copy(alpha = 0.7f) else SpaceOutline,
      ),
    shadowElevation = 4.dp,
    modifier = Modifier.fillMaxWidth().testTag("planet_card_${body.id}"),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(74.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SpaceNavy),
        contentAlignment = Alignment.Center,
      ) {
        MiniPlanet3DOrb(body = body, size = 66.dp)
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            color = body.primaryColor.copy(alpha = 0.22f),
            shape = RoundedCornerShape(6.dp),
          ) {
            Text(
              text = body.category.uppercase(),
              style = MaterialTheme.typography.labelSmall,
              color = body.accentColor,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            )
          }
          if (isExplored) {
            Icon(
              imageVector = Icons.Rounded.CheckCircle,
              contentDescription = "Esplorato",
              tint = AuroraGreen,
              modifier = Modifier.size(18.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = body.name,
          style = MaterialTheme.typography.titleLarge,
          color = StarlightWhite,
        )
        Text(
          text = body.nickname,
          style = MaterialTheme.typography.labelLarge,
          color = CosmicAmber,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = body.quickHook,
          style = MaterialTheme.typography.bodyMedium,
          color = StarlightMuted,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Surface(
        color = SpaceCardElevated,
        shape = CircleShape,
        modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = "Apri ${body.name}",
            tint = CosmicCyan,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}
