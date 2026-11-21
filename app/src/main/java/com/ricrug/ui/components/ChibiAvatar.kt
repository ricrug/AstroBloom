package com.ricrug.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ricrug.R
import com.ricrug.data.SolarSystemData
import com.ricrug.model.ChibiExpression
import com.ricrug.model.ChibiProfile
import com.ricrug.ui.theme.AuroraGreen
import com.ricrug.ui.theme.ComicBubbleCream
import com.ricrug.ui.theme.ComicInk
import com.ricrug.ui.theme.ComicYellow
import com.ricrug.ui.theme.CosmicAmber
import com.ricrug.ui.theme.CosmicCyan
import com.ricrug.ui.theme.NebulaMagenta
import com.ricrug.ui.theme.SpaceCardElevated
import com.ricrug.ui.theme.SpaceNavy
import com.ricrug.ui.theme.StarlightMuted
import com.ricrug.ui.theme.StarlightWhite
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ChibiAvatar(
  profile: ChibiProfile,
  expression: ChibiExpression = ChibiExpression.HAPPY,
  modifier: Modifier = Modifier,
  size: Dp = 92.dp,
  showBackdrop: Boolean = true,
  useFullBody: Boolean = false,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "chibiAnim")
  val floatPhase by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = (2 * PI).toFloat(),
      animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
      label = "float",
    )

  val bobPx = sin(floatPhase) * 4f
  val accentColor = SolarSystemData.suitColors.getOrElse(profile.suitColorIndex) { CosmicAmber }

  Box(
    modifier = modifier.size(width = if (useFullBody) size * 0.78f else size, height = size),
    contentAlignment = Alignment.Center,
  ) {
    val frameShape = if (useFullBody) RoundedCornerShape(22.dp) else CircleShape
    Surface(
      shape = frameShape,
      color = ComicBubbleCream,
      border =
        BorderStroke(
          width = if (showBackdrop) 3.dp else 2.dp,
          color = ComicInk,
        ),
      shadowElevation = 6.dp,
      modifier =
        Modifier.fillMaxSize()
          .padding(3.dp)
          .graphicsLayer { translationY = bobPx }
          .border(2.5.dp, accentColor, frameShape),
    ) {
      Image(
        painter =
          painterResource(
            id = if (useFullBody) R.drawable.img_chibi_character else R.drawable.img_chibi_portrait,
          ),
        contentDescription = "${profile.name} personaggio chibi",
        contentScale = if (useFullBody) ContentScale.Fit else ContentScale.Crop,
        modifier = Modifier.fillMaxSize().clip(frameShape),
      )
    }

    // Dynamic comic mood sticker in corner
    if (showBackdrop) {
      val (badgeColor, badgeLabel) =
        when (expression) {
          ChibiExpression.AMAZED -> CosmicAmber to "!"
          ChibiExpression.THINKING -> CosmicCyan to "?"
          ChibiExpression.CHEERING -> AuroraGreen to "★"
          ChibiExpression.ROCKET -> NebulaMagenta to "ROCKET"
          ChibiExpression.HAPPY -> CosmicAmber to "★"
        }

      Surface(
        color = badgeColor,
        shape = CircleShape,
        border = BorderStroke(1.5.dp, ComicInk),
        modifier = Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(24.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (badgeLabel == "ROCKET") {
            Icon(
              imageVector = Icons.Rounded.RocketLaunch,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(14.dp),
            )
          } else {
            Text(
              text = badgeLabel,
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              color = ComicInk,
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChibiComicBubble(
  profile: ChibiProfile,
  expression: ChibiExpression,
  dialogue: String,
  modifier: Modifier = Modifier,
  badgeText: String? = null,
  headline: String? = null,
  avatarSize: Dp = 86.dp,
  onAvatarClick: (() -> Unit)? = null,
  footerContent: (@Composable () -> Unit)? = null,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Bottom,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
        Modifier.then(
          if (onAvatarClick != null) {
            Modifier.clip(RoundedCornerShape(16.dp))
              .clickable(onClick = onAvatarClick)
              .testTag("chibi_avatar_button")
          } else {
            Modifier
          }
        ),
    ) {
      ChibiAvatar(
        profile = profile,
        expression = expression,
        size = avatarSize,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Surface(
        color = CosmicAmber,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, ComicInk),
      ) {
        Text(
          text = profile.name,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = ComicInk,
          maxLines = 1,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    Box(modifier = Modifier.weight(1f)) {
      Surface(
        color = ComicBubbleCream,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp),
        border = BorderStroke(2.5.dp, ComicInk),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          if (badgeText != null) {
            Surface(
              color = NebulaMagenta,
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.5.dp, ComicInk),
            ) {
              Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
          }
          if (headline != null) {
            Text(
              text = headline,
              style = MaterialTheme.typography.titleMedium,
              color = ComicInk,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
          }
          Text(
            text = dialogue,
            style = MaterialTheme.typography.bodyLarge,
            color = ComicInk,
          )
          if (footerContent != null) {
            Spacer(modifier = Modifier.height(10.dp))
            footerContent()
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChibiCustomizerSheet(
  profile: ChibiProfile,
  onDismiss: () -> Unit,
  onSave: (ChibiProfile) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var draft by remember(profile) { mutableStateOf(profile) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = SpaceNavy,
    contentColor = StarlightWhite,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .padding(bottom = 28.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = CosmicAmber,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Personalizza il Personaggio",
            style = MaterialTheme.typography.headlineSmall,
            color = StarlightWhite,
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Rounded.Close, contentDescription = "Chiudi", tint = StarlightMuted)
        }
      }

      Text(
        text = "Scegli il tuo nome da esploratore e il colore del badge fumetto!",
        style = MaterialTheme.typography.bodyMedium,
        color = StarlightMuted,
      )

      Spacer(modifier = Modifier.height(16.dp))

      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        ChibiAvatar(
          profile = draft,
          expression = ChibiExpression.CHEERING,
          size = 176.dp,
          useFullBody = true,
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = draft.name,
        onValueChange = { if (it.length <= 18) draft = draft.copy(name = it) },
        label = { Text("Nome dell'Esploratore") },
        singleLine = true,
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CosmicCyan,
            unfocusedBorderColor = SpaceCardElevated,
            focusedLabelColor = CosmicCyan,
            focusedTextColor = StarlightWhite,
            unfocusedTextColor = StarlightWhite,
          ),
        modifier = Modifier.fillMaxWidth().testTag("chibi_name_input"),
      )

      Spacer(modifier = Modifier.height(14.dp))
      Text("Colore Cornice Fumetto", style = MaterialTheme.typography.labelLarge, color = CosmicAmber)
      Spacer(modifier = Modifier.height(8.dp))
      ColorPickerRow(
        colors = SolarSystemData.suitColors,
        selectedIndex = draft.suitColorIndex,
        onSelect = { draft = draft.copy(suitColorIndex = it) },
      )

      Spacer(modifier = Modifier.height(20.dp))
      Button(
        onClick = {
          onSave(draft)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = CosmicAmber, contentColor = ComicInk),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_chibi_button"),
      ) {
        Icon(Icons.Rounded.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Salva Profilo!", style = MaterialTheme.typography.titleMedium, maxLines = 1, softWrap = false)
      }
    }
  }
}

@Composable
private fun ColorPickerRow(
  colors: List<Color>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    colors.forEachIndexed { index, color ->
      val selected = index == selectedIndex
      Box(
        modifier =
          Modifier.size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
              width = if (selected) 3.dp else 1.5.dp,
              color = if (selected) ComicYellow else SpaceCardElevated,
              shape = CircleShape,
            )
            .clickable { onSelect(index) },
        contentAlignment = Alignment.Center,
      ) {
        if (selected) {
          Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "Selezionato",
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}
