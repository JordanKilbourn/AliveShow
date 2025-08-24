package com.playworld.aliveshow

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.playworld.aliveshow.audio.PlayerController
import com.playworld.aliveshow.ui.theme.*

// Main activity with Android 12+ splash support
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    // ✅ Get context outside the remember{} calculation to avoid the Compose error you saw
    val context = LocalContext.current
    val player = remember(context) { PlayerController(context) }
    DisposableEffect(Unit) { onDispose { player.release() } }

    // Pick audio (Storage Access Framework – no storage permission needed)
    val pickAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) player.setSource(uri)
    }

    // Mic permission for Visualizer (safe no-op if denied)
    val reqMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled gracefully */ }

    AliveTheme {
        GradientBackground(Modifier.fillMaxSize()) {
            val nav = rememberNavController()
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { BottomBar(nav) }
            ) { padding ->
                NavHost(
                    navController = nav,
                    startDestination = "home",
                    modifier = Modifier.padding(padding)
                ) {
                    composable("home") {
                        HomeScreen(
                            onPick = { pickAudio.launch(arrayOf("audio/*")) },
                            onRecordPerm = { reqMic.launch(Manifest.permission.RECORD_AUDIO) },
                            onGoPreview = { nav.navigate("preview") }
                        )
                    }
                    composable("preview") { PreviewScreen(player) }
                    composable("settings") { SettingsScreen() }
                }
            }
        }
    }
}

/* ----------------------------- UI Components ----------------------------- */

@Composable
private fun BottomBar(nav: NavHostController) {
    val items = listOf(
        NavItem("home", "Home", Icons.Outlined.Home),
        NavItem("preview", "Preview", Icons.Outlined.SmartDisplay),
        NavItem("settings", "Settings", Icons.Outlined.Settings)
    )
    NavigationBar(containerColor = Color(0x1FFFFFFF)) {
        val backStack by nav.currentBackStackEntryAsState()
        val current = backStack?.destination
        items.forEach { item ->
            val selected = current?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) nav.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

enum class AudioMode { DialogueOnly, DialogueMusic, MusicOnly }

@Composable
fun HomeScreen(onPick: () -> Unit, onRecordPerm: () -> Unit, onGoPreview: () -> Unit) {
    var mode by remember { mutableStateOf(AudioMode.DialogueMusic) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EnterSection {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("AliveShow", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Create cinematic light shows. Smart mapping, zero fuss.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }
        }
        EnterSection {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Start a new show", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Pick your audio type. The app adapts options and mapping accordingly.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    ModeChips(mode) { mode = it }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PressScaleButton(
                            colors = ButtonDefaults.elevatedButtonColors(),
                            onClick = onPick
                        ) {
                            Icon(Icons.Outlined.Audiotrack, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pick audio")
                        }
                        PressScaleButton(
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            onClick = onRecordPerm,
                            tonal = true
                        ) {
                            Icon(Icons.Outlined.Mic, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Enable mic")
                        }
                    }
                    AssistChip(onClick = { /* TODO */ }, label = { Text("Personality: Jarvis") })
                }
            }
        }
        EnterSection {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("One-tap generate", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Preview Eyes, Mouth, and Gestures before export.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    Button(
                        onClick = onGoPreview,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate & Preview")
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Text(
            "Tip: export to a USB “/LightShow” folder when ready.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ModeChips(selected: AudioMode, onChange: (AudioMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == AudioMode.DialogueOnly,
            onClick = { onChange(AudioMode.DialogueOnly) },
            label = { Text("Dialogue only") }
        )
        FilterChip(
            selected = selected == AudioMode.DialogueMusic,
            onClick = { onChange(AudioMode.DialogueMusic) },
            label = { Text("Dialogue + Music") }
        )
        FilterChip(
            selected = selected == AudioMode.MusicOnly,
            onClick = { onChange(AudioMode.MusicOnly) },
            label = { Text("Music only") }
        )
    }
}

@Composable
fun PreviewScreen(player: PlayerController) {
    val amp by player.amplitude.collectAsState(0f)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EnterSection {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preview", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Pick audio on Home. Play here; mouth reacts to loudness.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }
        }
        EnterSection {
            GlassCard(Modifier.fillMaxWidth().height(260.dp)) {
                Box(Modifier.padding(12.dp)) { CarFaceMock(mouthOpen = amp) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { player.togglePlay() }) { Text("Play / Pause") }
            OutlinedButton(onClick = { player.stop() }) { Text("Stop") }
        }
        Button(
            onClick = { /* TODO: export */ },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(Icons.Outlined.Usb, null)
            Spacer(Modifier.width(8.dp))
            Text("Export to USB /LightShow")
        }
    }
}

@Composable
fun CarFaceMock(mouthOpen: Float = 0f) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawRoundRect(
            Color(0xFF1B2437),
            topLeft = Offset(w * 0.05f, h * 0.1f),
            size = androidx.compose.ui.geometry.Size(w * 0.9f, h * 0.8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f, 40f),
            style = Stroke(width = 3f)
        )
        // Eyes
        drawCircle(Color(0xFF9EC3FF), 18f, Offset(w * 0.22f, h * 0.47f))
        drawCircle(Color(0xFF9EC3FF), 18f, Offset(w * 0.78f, h * 0.47f))
        // DRL eyelids
        drawLine(Color(0xFFCCE0FF), Offset(w * 0.15f, h * 0.42f), Offset(w * 0.29f, h * 0.42f), 6f)
        drawLine(Color(0xFFCCE0FF), Offset(w * 0.71f, h * 0.42f), Offset(w * 0.85f, h * 0.42f), 6f)
        // Mouth (reacts to amplitude)
        val mouthH = 6f + (22f * mouthOpen.coerceIn(0f, 1f))
        drawLine(Color(0xFFFFCC99), Offset(w * 0.35f, h * 0.60f), Offset(w * 0.65f, h * 0.60f), mouthH)
        // Cheeks
        drawCircle(Color(0xFFFFE8B0), 8f, Offset(w * 0.12f, h * 0.52f))
        drawCircle(Color(0xFFFFE8B0), 8f, Offset(w * 0.88f, h * 0.52f))
        // Rear bar reference
        drawLine(Color(0xFFFF8080), Offset(w * 0.40f, h * 0.86f), Offset(w * 0.60f, h * 0.86f), 8f)
    }
}

enum class HeadlightType { Projector, Reflector }

@Composable
fun SettingsScreen() {
    var hasFogs by remember { mutableStateOf(true) }
    var headlight by remember { mutableStateOf(HeadlightType.Projector) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EnterSection {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Settings", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Tell the app what hardware you have so mappings use every light.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AssistChip(onClick = { headlight = HeadlightType.Projector }, label = { Text("Headlights: Projector") })
                        AssistChip(onClick = { headlight = HeadlightType.Reflector }, label = { Text("Headlights: Reflector") })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = hasFogs, onCheckedChange = { hasFogs = it })
                        Spacer(Modifier.width(8.dp)); Text("Front fog lights present")
                    }
                    Button(onClick = { /* TODO: persist */ }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save profile")
                    }
                }
            }
        }
    }
}
