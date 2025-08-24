package com.playworld.aliveshow

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.playworld.aliveshow.audio.PlayerController
import com.playworld.aliveshow.ui.theme.*
import com.playworld.aliveshow.viewer.TeslaModelView   // 3D preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val player = remember(context) { PlayerController(context) }
    DisposableEffect(Unit) { onDispose { player.release() } }

    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) player.setSource(uri)
    }
    val reqMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    AliveTheme {
        GradientBackground(Modifier.fillMaxSize()) {
            val nav = rememberNavController()
            Scaffold(containerColor = Color.Transparent, bottomBar = { BottomBar(nav) }) { padding ->
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
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

/* ----------------------------- Navigation UI ----------------------------- */

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

/* --------------------------------- Home ---------------------------------- */

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
                        "Create cinematic Tesla light shows.\nSmart mapping, zero fuss.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                    )
                }
            }
        }

        EnterSection {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Start a new show", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "1) Pick audio  •  2) Choose style  •  3) Preview & export",
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
                            onClick = onGoPreview,
                            tonal = true
                        ) {
                            Icon(Icons.Outlined.SmartDisplay, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Open preview")
                        }
                    }

                    AssistChip(
                        onClick = onRecordPerm,
                        label = { Text("Enable mic for reactive mouth") },
                        leadingIcon = { Icon(Icons.Outlined.Mic, null) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModeChips(selected: AudioMode, onChange: (AudioMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == AudioMode.DialogueOnly, onClick = { onChange(AudioMode.DialogueOnly) }, label = { Text("Dialogue only") })
        FilterChip(selected = selected == AudioMode.DialogueMusic, onClick = { onChange(AudioMode.DialogueMusic) }, label = { Text("Dialogue + Music") })
        FilterChip(selected = selected == AudioMode.MusicOnly, onClick = { onChange(AudioMode.MusicOnly) }, label = { Text("Music only") })
    }
}

/* -------------------------------- Preview -------------------------------- */

@Composable
fun PreviewScreen(player: PlayerController) {
    val amp by player.amplitude.collectAsState(0f)

    var head by remember { mutableStateOf(true) }
    var drl  by remember { mutableStateOf(true) }
    var fog  by remember { mutableStateOf(false) }
    var tail by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EnterSection {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preview", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Rotate the car. Play your audio; lights react in real-time.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }
        }

        EnterSection {
            GlassCard(Modifier.fillMaxWidth().height(280.dp)) {
                TeslaModelView(
                    amplitude = amp,
                    headlight = head, drl = drl, fog = fog, tail = tail
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { player.togglePlay() }) { Text("Play / Pause") }
            OutlinedButton(onClick = { player.stop() }) { Text("Stop") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(head,  onClick = { head = !head },  label = { Text("Headlights") })
            FilterChip(drl,   onClick = { drl = !drl },    label = { Text("DRLs") })
            FilterChip(fog,   onClick = { fog = !fog },    label = { Text("Fogs") })
            FilterChip(tail,  onClick = { tail = !tail },  label = { Text("Taillights") })
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { /* TODO: export to USB */ },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(Icons.Outlined.Usb, null)
            Spacer(Modifier.width(8.dp))
            Text("Export to USB /LightShow")
        }
    }
}

/* -------------------------------- Settings ------------------------------- */

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