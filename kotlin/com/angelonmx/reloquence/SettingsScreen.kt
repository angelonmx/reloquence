package com.angelonmx.reloquence

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private data class Category(
	val id: String,
	val title: String,
	val subtitle: String,
	val icon: ImageVector
)

/**
 * The four settings that are really the engine's: how a voice sounds, how text
 * is read, the dictionaries it reads with, and what is inside the build.
 *
 * The old interface also offered equalisation, history, backups and updates.
 * None of those exist in OpenEVV and none of them are pretended here; an
 * engine has voices, text rules and dictionaries, and those are the screens.
 */
private val categories = listOf(
	Category("voice", "Voz y ritmo", "Voz, género, velocidad y tono", Icons.Filled.Mic),
	Category("reading", "Lectura", "Pausas, abreviaturas y calidad de audio", Icons.Filled.Tune),
	Category("dictionary", "Diccionario", "Pronunciaciones personales", Icons.AutoMirrored.Filled.MenuBook),
	Category("about", "Acerca de", "Versión, motor e idiomas", Icons.Filled.Info)
)

/** The names the settings screen uses for the eight presets. The engine's own
 *  names are in [Eci.PRESET_NAMES] and are what Android sees. */
private val VOICE_NAMES = listOf(
	"Reed", "Shelley", "Bobby", "Rocko", "Glen", "Sandy", "Abuela", "Abuelo"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsApp(model: SettingsModel) {
	var page by remember { mutableStateOf<String?>(null) }
	val category = categories.firstOrNull { it.id == page }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(category?.title ?: "ReloquenceTTS") },
				navigationIcon = {
					if (category != null) {
						IconButton(onClick = { page = null }) {
							Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
						}
					}
				}
			)
		}
	) { insets ->
		if (category == null) {
			HomeScreen(model, Modifier.padding(insets), onOpen = { page = it })
		} else {
			CategoryScreen(model, category, Modifier.padding(insets))
		}
	}
}

@Composable
private fun HomeScreen(model: SettingsModel, modifier: Modifier, onOpen: (String) -> Unit) {
	LazyColumn(
		modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		item {
			Text(
				"Configuración",
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.SemiBold,
				modifier = Modifier.padding(vertical = 8.dp)
			)
		}
		item {
			val ready = EvvNative.loaded
			Card(
				colors = CardDefaults.cardColors(
					containerColor = if (ready) {
						MaterialTheme.colorScheme.primaryContainer
					} else {
						MaterialTheme.colorScheme.errorContainer
					}
				)
			) {
				Column(Modifier.fillMaxWidth().padding(16.dp)) {
					Text(
						if (ready) "Motor listo" else "No se pudo cargar el motor",
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						if (ready) {
							"OpenEVV ${EvvEngine.version} · ${model.languageNames.size} idiomas"
						} else {
							EvvNative.loadError?.message ?: "Revisa la instalación de la biblioteca nativa."
						},
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(top = 2.dp)
					)
				}
			}
		}
		items(categories) { category ->
			Card(
				modifier = Modifier.fillMaxWidth().clickable { onOpen(category.id) },
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surfaceContainerLow
				)
			) {
				Row(
					modifier = Modifier.fillMaxWidth().padding(18.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(16.dp)
				) {
					Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
					Column {
						Text(category.title, style = MaterialTheme.typography.titleMedium)
						Text(category.subtitle, style = MaterialTheme.typography.bodyMedium)
					}
				}
			}
		}
		item {
			Text(
				"ReloquenceTTS funciona como motor de texto a voz del sistema. " +
					"Actívalo en Ajustes > Idiomas e introducción de texto > Salida de texto a voz.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(vertical = 10.dp)
			)
		}
	}
}

@Composable
private fun CategoryScreen(model: SettingsModel, category: Category, modifier: Modifier) {
	Column(
		modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		when (category.id) {
			"voice" -> VoiceSettings(model)
			"reading" -> ReadingSettings(model)
			"dictionary" -> DictionarySettings(model)
			"about" -> AboutSettings(model)
		}
		Spacer(Modifier.height(20.dp))
	}
}

// ---- voices --------------------------------------------------------------

@Composable
private fun VoiceSettings(model: SettingsModel) {
	SectionTitle("Vista previa")
	ChoiceRow(
		label = stringResource(R.string.preview_language_label),
		current = model.languageNames.getOrElse(model.language) { "—" },
		options = model.languageNames,
		chosen = model.language,
		onChoose = model::chooseLanguage
	)
	Text(
		"Este idioma solo afecta a la muestra que se escucha aquí. El servicio " +
			"responde siempre en el idioma que pida la aplicación que lo usa.",
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)

	SectionTitle("Voz")
	ChoiceRow(
		label = stringResource(R.string.voice_label),
		current = VOICE_NAMES.getOrElse(model.voice) { "—" },
		options = VOICE_NAMES,
		chosen = model.voice,
		onChoose = model::chooseVoice
	)
	ChoiceRow(
		label = stringResource(R.string.gender_label),
		current = listOf(
			stringResource(R.string.gender_male),
			stringResource(R.string.gender_female)
		).getOrElse(model.gender) { "—" },
		options = listOf(
			stringResource(R.string.gender_male),
			stringResource(R.string.gender_female)
		),
		chosen = model.gender,
		onChoose = { model.setShape(Eci.VOICE_GENDER, it) }
	)

	SectionTitle("Ajustes de voz")
	// Speed is the one scale that is not 0 to 100: the engine takes 0 to 250
	// and the old interface showed that number, so it is shown again.
	SliderRow(
		label = stringResource(R.string.speed_label),
		value = model.valueOf(Eci.VOICE_SPEED),
		range = Eci.voiceRange(Eci.VOICE_SPEED),
		display = model.valueOf(Eci.VOICE_SPEED).toString(),
		spoken = model.valueOf(Eci.VOICE_SPEED).toString(),
		onValue = { model.setValue(Eci.VOICE_SPEED, it) },
		onStep = { model.stepValue(Eci.VOICE_SPEED, it) }
	)
	for (param in listOf(
		Eci.VOICE_PITCH_BASELINE,
		Eci.VOICE_PITCH_FLUCTUATION,
		Eci.VOICE_HEAD_SIZE,
		Eci.VOICE_VOLUME,
		Eci.VOICE_ROUGHNESS,
		Eci.VOICE_BREATHINESS
	)) {
		PercentSlider(param, model)
	}

	Spacer(Modifier.height(6.dp))
	ActionRow(
		label = stringResource(R.string.speak_label),
		enabled = model.languages.isNotEmpty()
	) { model.say() }
	ConfirmRow(
		label = stringResource(R.string.reset_label),
		title = "Restablecer voz",
		message = "Se borrarán los ajustes personalizados de esta voz. Las demás voces no cambian.",
		confirmLabel = "Restablecer"
	) { model.resetVoice() }
}

@Composable
private fun PercentSlider(param: Int, model: SettingsModel) {
	val percent = model.percentOf(param)
	SliderRow(
		label = stringResource(labelOf(param)),
		value = percent,
		range = 0..Eci.PERCENT_MAX,
		display = stringResource(R.string.percent, percent),
		spoken = stringResource(R.string.percent_spoken, percent),
		onValue = { model.setPercent(param, it) },
		onStep = { model.stepPercent(param, it) }
	)
}

private fun labelOf(param: Int): Int = when (param) {
	Eci.VOICE_SPEED -> R.string.speed_label
	Eci.VOICE_PITCH_BASELINE -> R.string.pitch_label
	Eci.VOICE_PITCH_FLUCTUATION -> R.string.inflection_label
	Eci.VOICE_HEAD_SIZE -> R.string.head_size_label
	Eci.VOICE_ROUGHNESS -> R.string.roughness_label
	Eci.VOICE_BREATHINESS -> R.string.breathiness_label
	else -> R.string.volume_label
}

// ---- reading -------------------------------------------------------------

@Composable
private fun ReadingSettings(model: SettingsModel) {
	val pauseNames = listOf(
		stringResource(R.string.pauses_keep),
		stringResource(R.string.pauses_end),
		stringResource(R.string.pauses_all)
	)

	SectionTitle("Pausas")
	ChoiceRow(
		label = stringResource(R.string.pauses_label),
		current = pauseNames.getOrElse(model.pauses) { "—" },
		options = pauseNames,
		chosen = model.pauses,
		onChoose = model::choosePauses
	)
	Text(
		"OpenEVV deja pausas largas en la puntuación. Acortarlas hace que un " +
			"lector de pantalla responda antes entre elementos.",
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)

	SectionTitle("Pronunciación")
	SwitchRow(
		label = stringResource(R.string.phrase_prediction_label),
		checked = model.phrasePrediction,
		onChange = model::choosePhrasePrediction
	)
	Text(
		"El motor adivina dónde acaba una frase y ajusta la entonación. Suele " +
			"leer bien en prosa y mal en fragmentos de lector de pantalla, así que " +
			"está desactivado por defecto.",
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
	SwitchRow(
		label = stringResource(R.string.abbreviations_label),
		checked = model.abbreviations,
		onChange = model::chooseAbbreviations
	)
	Text(
		"Expande abreviaturas como «Sr.» o «p. ej.». Está desactivado porque " +
			"también cambia palabras que no eran abreviaturas.",
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)

	SectionTitle("Audio")
	val rates = SettingsModel.RATES
	ChoiceRow(
		label = stringResource(R.string.sample_rate_label),
		current = stringResource(R.string.hertz, model.sampleRateHz),
		options = rates.map { stringResource(R.string.hertz, it) },
		chosen = rates.indexOf(model.sampleRateHz).coerceAtLeast(0),
		onChoose = { model.chooseSampleRate(rates[it]) }
	)
	Text(
		"11025 Hz es el valor natural del motor. Las frecuencias más altas " +
			"suenan más nítidas y entregan más datos a Android.",
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}

// ---- dictionaries --------------------------------------------------------

@Composable
private fun DictionarySettings(model: SettingsModel) {
	var wanted by remember { mutableStateOf(Eci.DICT_MAIN) }
	val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		uri?.let { model.chooseDictionary(wanted, it) }
	}

	SectionTitle("Diccionarios del motor")
	Text(
		"El formato es una entrada por línea: la palabra, un tabulador y su " +
			"pronunciación (por ejemplo, «Reloquence» y «relo cuens»). El motor " +
			"también acepta su alfabeto fonético entre corchetes.",
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
	for (volume in Eci.DICT_VOLUMES) {
		ValueRow(
			label = stringResource(dictionaryLabelOf(volume)),
			value = model.dictionaryName(volume)
		) {
			wanted = volume
			pick.launch(arrayOf("*/*"))
		}
	}
	ConfirmRow(
		label = stringResource(R.string.dictionary_remove),
		title = "Quitar diccionarios",
		message = "Se quitarán los tres diccionarios y sus copias internas.",
		confirmLabel = "Quitar"
	) { model.removeDictionaries() }

	SectionTitle("Dónde se guardan")
	Text(
		"El archivo elegido se copia al almacenamiento interno de la app, para " +
			"que el servicio pueda leerlo aunque el teléfono esté bloqueado. No se " +
			"modifica el archivo original.",
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}

private fun dictionaryLabelOf(volume: Int): Int = when (volume) {
	Eci.DICT_MAIN -> R.string.dictionary_main
	Eci.DICT_ROOT -> R.string.dictionary_root
	else -> R.string.dictionary_abbreviation
}

// ---- about ---------------------------------------------------------------

@Composable
private fun AboutSettings(model: SettingsModel) {
	val context = LocalContext.current
	var license by remember { mutableStateOf<Pair<String, String>?>(null) }
	val ready = EvvNative.loaded

	SectionTitle("ReloquenceTTS")
	InfoRow("Versión", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
	InfoRow("Paquete", context.packageName)
	InfoRow(
		"Motor",
		if (ready) "Eloquence · OpenEVV ${EvvEngine.version}" else "No disponible"
	)
	InfoRow(
		"Idiomas",
		model.languageNames.ifEmpty { listOf("Ninguno") }.joinToString(", ")
	)
	InfoRow("Voces", "Ocho presets por idioma, más los ajustes propios de cada una")

	SectionTitle("Activación")
	Text(
		"En los ajustes del sistema, abre Idiomas e introducción de texto > " +
			"Salida de texto a voz y elige ReloquenceTTS. Después puedes usarlo " +
			"desde cualquier lector de pantalla o aplicación de lectura.",
		style = MaterialTheme.typography.bodyMedium
	)
	ActionRow("Abrir ajustes de voz del sistema") { openTtsSettings(context) }

	SectionTitle("Licencias")
	InfoRow(
		"Aviso",
		"El motor Eloquence/OpenEVV y sus módulos de idioma se distribuyen con " +
			"sus propias licencias, incluidas en el APK."
	)
	ActionRow("Licencia de OpenEVV") {
		license = "Licencia de OpenEVV" to "openevv/LICENSE"
	}
	ActionRow("Avisos de OpenEVV") {
		license = "Avisos de OpenEVV" to "openevv/NOTICE"
	}
	license?.let { (title, asset) ->
		LicenseDialog(title, asset) { license = null }
	}
}

private fun openTtsSettings(context: Context) {
	val candidates = listOf(
		Intent("com.android.settings.TTS_SETTINGS"),
		Intent(Settings.ACTION_SETTINGS)
	)
	for (intent in candidates) {
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		try {
			context.startActivity(intent)
			return
		} catch (_: Exception) {
			// Try the next one. Some builds hide the TTS page behind their own
			// settings activity.
		}
	}
	Toast.makeText(context, "No se pudieron abrir los ajustes del sistema.", Toast.LENGTH_LONG).show()
}

// ---- rows and dialogs ----------------------------------------------------

@Composable
private fun SectionTitle(title: String) {
	Text(
		title,
		style = MaterialTheme.typography.titleMedium,
		color = MaterialTheme.colorScheme.primary,
		fontWeight = FontWeight.SemiBold,
		modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp)
	)
}

/**
 * A label, the value it is at, and a bar, as one thing.
 *
 * clearAndSetSemantics throws away what the label and the bar would each have
 * said and puts one control there instead: the name, the value, the range it
 * moves in, and how to move it. Without it a reader finds the text and the bar
 * separately and neither says what the other is.
 */
@Composable
private fun SliderRow(
	label: String,
	value: Int,
	range: IntRange,
	display: String,
	spoken: String,
	onValue: (Int) -> Unit,
	onStep: (Int) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 6.dp)
			.onKeyEvent { press ->
				if (press.type != KeyEventType.KeyDown) return@onKeyEvent false
				when (press.key) {
					Key.DirectionLeft, Key.DirectionDown -> onStep(-1)
					Key.DirectionRight, Key.DirectionUp -> onStep(1)
					Key.MoveHome -> onValue(range.first)
					Key.MoveEnd -> onValue(range.last)
					else -> return@onKeyEvent false
				}
				true
			}
			.focusable()
			.clearAndSetSemantics {
				contentDescription = label
				stateDescription = spoken
				progressBarRangeInfo = ProgressBarRangeInfo(
					value.toFloat(),
					range.first.toFloat()..range.last.toFloat()
				)
				setProgress { want ->
					onValue(want.roundToInt().coerceIn(range.first, range.last))
					true
				}
			}
	) {
		Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
			Text(label, style = MaterialTheme.typography.bodyLarge)
			Text(display, color = MaterialTheme.colorScheme.primary)
		}
		Slider(
			modifier = Modifier.focusProperties { canFocus = false },
			value = value.toFloat(),
			onValueChange = { onValue(it.roundToInt()) },
			valueRange = range.first.toFloat()..range.last.toFloat(),
			steps = 0
		)
	}
}

@Composable
private fun ChoiceRow(
	label: String,
	current: String,
	options: List<String>,
	chosen: Int,
	onChoose: (Int) -> Unit
) {
	var open by remember { mutableStateOf(false) }
	Text(
		text = stringResource(R.string.labelled_value, label, current),
		style = MaterialTheme.typography.bodyLarge,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(enabled = options.isNotEmpty()) { open = true }
			.padding(vertical = 12.dp)
			.clearAndSetSemantics {
				contentDescription = label
				stateDescription = current
				role = Role.Button
			}
	)
	if (open && options.isNotEmpty()) {
		AlertDialog(
			onDismissRequest = { open = false },
			title = { Text(label) },
			text = {
				// Scrollable because the language row offers ten and a phone
				// held large does not fit them.
				Column(
					modifier = Modifier
						.heightIn(max = 400.dp)
						.selectableGroup()
						.verticalScroll(rememberScrollState())
				) {
					options.forEachIndexed { at, option ->
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.selectable(
									selected = at == chosen,
									onClick = {
										onChoose(at)
										open = false
									},
									role = Role.RadioButton
								)
								.padding(vertical = 12.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(selected = at == chosen, onClick = null)
							Text(
								text = option,
								modifier = Modifier.padding(start = 12.dp),
								style = MaterialTheme.typography.bodyLarge
							)
						}
					}
				}
			},
			confirmButton = {
				TextButton(onClick = { open = false }) {
					Text(stringResource(R.string.cancel))
				}
			}
		)
	}
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
	val on = stringResource(if (checked) R.string.switch_on else R.string.switch_off)
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onChange(!checked) }
			.padding(vertical = 10.dp)
			.clearAndSetSemantics {
				contentDescription = label
				stateDescription = on
				role = Role.Switch
			},
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f)
		)
		Switch(checked = checked, onCheckedChange = null)
	}
}

/** A label, what it is set to, and something to press to change it. */
@Composable
private fun ValueRow(label: String, value: String, onClick: () -> Unit) {
	Text(
		text = stringResource(R.string.labelled_value, label, value),
		style = MaterialTheme.typography.bodyLarge,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 12.dp)
			.clearAndSetSemantics {
				contentDescription = label
				stateDescription = value
				role = Role.Button
			}
	)
}

@Composable
private fun InfoRow(label: String, value: String) {
	Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
		Text(label, style = MaterialTheme.typography.bodyLarge)
		Text(
			value,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun ActionRow(label: String, enabled: Boolean = true, onClick: () -> Unit) {
	TextButton(
		onClick = onClick,
		enabled = enabled,
		modifier = Modifier.fillMaxWidth()
	) {
		Text(label, modifier = Modifier.fillMaxWidth())
	}
}

@Composable
private fun ConfirmRow(
	label: String,
	title: String,
	message: String,
	confirmLabel: String,
	onConfirm: () -> Unit
) {
	var open by remember { mutableStateOf(false) }
	ActionRow(label) { open = true }
	if (open) {
		AlertDialog(
			onDismissRequest = { open = false },
			title = { Text(title) },
			text = { Text(message) },
			confirmButton = {
				TextButton(onClick = {
					onConfirm()
					open = false
				}) {
					Text(confirmLabel)
				}
			},
			dismissButton = {
				TextButton(onClick = { open = false }) {
					Text(stringResource(R.string.cancel))
				}
			}
		)
	}
}

@Composable
private fun LicenseDialog(title: String, asset: String, onDismiss: () -> Unit) {
	val context = LocalContext.current
	val text = remember(asset) {
		runCatching {
			context.assets.open(asset).bufferedReader().use { it.readText() }
		}.getOrDefault("No se encontró el archivo de licencia.")
	}
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(title) },
		text = {
			Column(
				modifier = Modifier
					.heightIn(max = 420.dp)
					.verticalScroll(rememberScrollState())
			) {
				Text(text, style = MaterialTheme.typography.bodySmall)
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) { Text("Cerrar") }
		}
	)
}
