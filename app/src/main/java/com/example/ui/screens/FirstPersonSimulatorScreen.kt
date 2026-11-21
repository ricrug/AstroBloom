package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.ChibiProfile
import com.example.ui.components.FirstPersonFlightSimulator3D

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }

@Composable
fun FirstPersonSimulatorScreen(
  chibiProfile: ChibiProfile,
  onOpenPlanetDetail: (String) -> Unit,
  onOpenLandingSimulator: (String) -> Unit,
  onExitSimulator: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val view = LocalView.current

  BackHandler { onExitSimulator() }

  DisposableEffect(Unit) {
    val activity = context.findActivity()
    val previousOrientation =
      activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val window = activity?.window
    val controller =
      if (window != null) {
        runCatching { WindowCompat.getInsetsController(window, view) }.getOrNull()
      } else {
        null
      }

    if (activity != null) {
      runCatching {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      }
    }
    controller?.let {
      runCatching {
        it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        it.hide(WindowInsetsCompat.Type.systemBars())
      }
    }

    onDispose {
      controller?.let {
        runCatching {
          it.show(WindowInsetsCompat.Type.systemBars())
        }
      }
      if (activity != null) {
        runCatching {
          activity.requestedOrientation = previousOrientation
        }
      }
    }
  }

  FirstPersonFlightSimulator3D(
    chibiProfile = chibiProfile,
    onOpenPlanetDetail = onOpenPlanetDetail,
    onOpenLandingSimulator = onOpenLandingSimulator,
    onExitSimulator = onExitSimulator,
    modifier = modifier.fillMaxSize(),
  )
}
