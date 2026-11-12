package com.angelonmx.reloquence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The engine's own settings, reached from the launcher and from the system's
 * text-to-speech screen through CONFIGURE_ENGINE.
 *
 * The engine instance used for the preview lives in [SettingsModel], not here,
 * so it is closed when this activity goes away and does not speak over
 * whatever comes next.
 */
class MainActivity : ComponentActivity() {

	private var model: SettingsModel? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val state = SettingsModel(this).also { model = it }
		enableEdgeToEdge()
		setContent {
			ReloquenceTheme {
				SettingsApp(state)
			}
		}
	}

	override fun onStop() {
		super.onStop()
		// Leaving the screen should not leave a sample talking over whatever
		// comes next.
		model?.stopSpeaking()
	}

	override fun onDestroy() {
		super.onDestroy()
		model?.close()
		model = null
	}
}

@Composable
private fun ReloquenceTheme(content: @Composable () -> Unit) {
	val scheme = lightColorScheme(
		primary = Color(0xFF425D91),
		onPrimary = Color.White,
		primaryContainer = Color(0xFFD9E2FF),
		onPrimaryContainer = Color(0xFF001A41),
		secondary = Color(0xFF565F71),
		background = Color(0xFFF8F9FF),
		surface = Color(0xFFF8F9FF),
		surfaceContainerLow = Color(0xFFF2F3F9)
	)
	MaterialTheme(colorScheme = scheme, content = content)
}
