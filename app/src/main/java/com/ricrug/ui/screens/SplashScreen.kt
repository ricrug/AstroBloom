package com.ricrug.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ricrug.R
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceCard
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.SpaceVoid
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val introAlpha = remember { Animatable(0f) }
  val introScale = remember { Animatable(0.9f) }
  val progressAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    launch { introAlpha.animateTo(1f, tween(durationMillis = 550, easing = FastOutSlowInEasing)) }
    launch { introScale.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing)) }
    launch { progressAnim.animateTo(1f, tween(durationMillis = 3400, easing = LinearEasing)) }
    delay(3500L)
    onSplashFinished()
  }

  val starSeeds = remember {
    List(75) { i ->
      val sx = ((i * 73 + 17) % 100) / 100f
      val sy = ((i * 139 + 31) % 100) / 100f
      val sr = 1f + (i % 3) * 0.9f
      val sa = 0.25f + (i % 5) * 0.14f
      Triple(sx, sy, sr to sa)
    }
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(SpaceVoid)
        .safeDrawingPadding()
        .testTag("splash_screen"),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawRect(
        brush =
          Brush.radialGradient(
            colors = listOf(Color(0xFF1A2352), Color(0xFF0A0F29), SpaceVoid),
            center = Offset(size.width * 0.5f, size.height * 0.42f),
            radius = size.maxDimension * 0.72f,
          ),
      )
      starSeeds.forEach { (sx, sy, sizeAlpha) ->
        drawCircle(
          color = Color.White.copy(alpha = sizeAlpha.second),
          radius = sizeAlpha.first,
          center = Offset(sx * size.width, sy * size.height),
        )
      }
    }

    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 540.dp)
          .padding(horizontal = 24.dp, vertical = 20.dp)
          .alpha(introAlpha.value)
          .scale(introScale.value),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top School Badge
      Surface(
        color = SpaceNavy.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, CosmicCyan.copy(alpha = 0.65f)),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Rounded.School, contentDescription = null, tint = CosmicAmber, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Scuola Re David • Bari",
            style = MaterialTheme.typography.titleMedium,
            color = StarlightWhite,
            modifier = Modifier.testTag("splash_school_name"),
          )
        }
      }

      // Center School Logo + App Title + Credits
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Surface(
          color = Color.White,
          shape = RoundedCornerShape(28.dp),
          border = BorderStroke(3.dp, CosmicAmber),
          shadowElevation = 10.dp,
          modifier = Modifier.fillMaxWidth(0.84f),
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_school_logo),
            contentDescription = "Logo della Scuola Re David di Bari",
            contentScale = ContentScale.FillWidth,
            modifier =
              Modifier.fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(18.dp))
                .testTag("splash_school_logo"),
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = CosmicAmber, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "AstroViaggio",
              style = MaterialTheme.typography.displaySmall,
              color = StarlightWhite,
              modifier = Modifier.testTag("splash_app_name"),
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Esplora il Sistema Solare • Scuola Primaria Re David (Bari)",
            style = MaterialTheme.typography.bodyMedium,
            color = CosmicCyan,
            textAlign = TextAlign.Center,
          )
        }

        // Credits to AstroBloom
        Surface(
          color = SpaceCard,
          shape = RoundedCornerShape(18.dp),
          border = BorderStroke(1.5.dp, NebulaMagenta.copy(alpha = 0.7f)),
          modifier = Modifier.testTag("splash_credits_badge"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_chibi_portrait),
              contentDescription = "AstroBloom",
              contentScale = ContentScale.Crop,
              modifier = Modifier.size(34.dp).clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = CosmicAmber, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Credits: AstroBloom",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = CosmicAmber,
                )
              }
              Text(
                text = "Ideazione, guida spaziale & laboratori",
                style = MaterialTheme.typography.labelSmall,
                color = StarlightMuted,
              )
            }
          }
        }
      }

      // Launch Progress + Skip
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
      ) {
        LinearProgressIndicator(
          progress = { progressAnim.value },
          color = AuroraGreen,
          trackColor = SpaceNavy,
          modifier = Modifier.fillMaxWidth(0.7f).height(8.dp).clip(RoundedCornerShape(4.dp)),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Text(
            text = "Accensione motori...",
            style = MaterialTheme.typography.labelSmall,
            color = StarlightMuted,
          )
          Spacer(modifier = Modifier.width(8.dp))
          TextButton(
            onClick = onSplashFinished,
            colors = ButtonDefaults.textButtonColors(contentColor = CosmicCyan),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.testTag("splash_skip_button"),
          ) {
            Text("Entra subito", style = MaterialTheme.typography.labelLarge)
          }
        }
      }
    }
  }
}
