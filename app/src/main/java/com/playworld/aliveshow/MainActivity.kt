package com.playworld.aliveshow

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.playworld.aliveshow.audio.PlayerController
import com.playworld.aliveshow.viewer.TeslaModelView
import com.playworld.aliveshow.ui.theme.* // uses your GradientBackground/GlassCard if present

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val player = remember { PlayerController(LocalContext.current) }
    DisposableEffect(Unit) { onDispose { player.release() } }

    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) player.setSource(uri)
    }
    val reqMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    AliveTheme {
        GradientBackground(Modifier.fillMaxSize()) {
            val nav = rememberNavController()
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { BottomBar(nav) }
            ) { padding ->
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
                    composable("home") {
                        HomeScreen(
                            isPlayingProvider = { player.amplitude.value > 0.02f },
                            onCreate = { nav.navigate("preview") },
                            onPick = { pickAudio.launch(arrayOf("audio/*")) },
                            onRecord = { reqMic.launch(Manifest.permission.RECORD_AUDIO) },
                            onOpenLibrary = { nav.navigate("library") }
                        )
                    }
                    composable("preview") { PreviewScreen(player) }
                    composable("settings") { SettingsScreen() }
                    composable("library") { LibraryScreen() }
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
        NavItem("library", "Library", Icons.Outlined.FolderOpen),
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

/* --------------------------------- HOME ---------------------------------- */

@Composable
fun HomeScreen(
    isPlayingProvider: () -> Boolean,
    onCreate: () -> Unit,
    onPick: () -> Unit,
    onRecord: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val breathing by animateFloatAsState(
        targetValue = if (isPlayingProvider()) 0.0f else 1.0f,
        animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing), label = "breath"
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HERO: 3D car + purpose text + primary CTA
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0x22FFFFFF),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 3D model viewer hero
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    // Subtle gradient frame behind the viewer
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x22000000), Color.Transparent)
                                )
                            )
                    )
                    // Amplitude “breathing” when idle (just a hint of life)
                    val ampHint = 0.08f + 0.04f * breathing
                    TeslaModelView(
                        amplitude = ampHint, // real amplitude takes over in Preview
                        headlight = true, drl = true, fog = false, tail = true
                    )
                }

                Text(
                    "Create cinematic Tesla light shows",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "Import audio, pick a style, preview on a live 3D Tesla, then export to USB.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onCreate,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Outlined.Bolt, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Create light show")
                    }
                    OutlinedButton(
                        onClick = onPick,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Outlined.Audiotrack, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import audio")
                    }
                }
            }
        }

        // QUICK ACTIONS
        Text("Quick actions", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickAction(
                icon = Icons.Outlined.Mic,
                label = "Quick record",
                onClick = onRecord
            )
            QuickAction(
                icon = Icons.Outlined.FolderOpen,
                label = "Open library",
                onClick = onOpenLibrary
            )
            QuickAction(
                icon = Icons.Outlined.QuestionMark,
                label = "How it works",
                onClick = { /* TODO: help overlay */ }
            )
        }

        // RECENTS (placeholder)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0x14FFFFFF),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent projects", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Your saved shows will appear here.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null)
            Text(label, textAlign = TextAlign.Center)
        }
    }
}

/* ------------------------------- PREVIEW --------------------------------- */

@Composable
fun PreviewScreen(player: com.playworld.aliveshow.audio.PlayerController) {
    val amp by player.amplitude.collectAsState(0f)

    var head by remember { mutableStateOf(true) }
    var drl  by remember { mutableStateOf(true) }
    var fog  by remember { mutableStateOf(false) }
    var tail by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Color(0x22FFFFFF), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Preview", style = MaterialTheme.typography.titleLarge)
                Text("Rotate the car. Play your audio; lights react in real-time.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            color = Color(0x14FFFFFF), shape = RoundedCornerShape(24.dp)
        ) {
            TeslaModelView(amplitude = amp, headlight = head, drl = drl, fog = fog, tail = tail)
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
        Button(onClick = { /* TODO export */ }, modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)) {
            Icon(Icons.Outlined.Usb, null); Spacer(Modifier.width(8.dp)); Text("Export to USB /LightShow")
        }
    }
}

/* ------------------------------- SETTINGS -------------------------------- */

enum class HeadlightType { Projector, Reflector }

@Composable
fun SettingsScreen() {
    var hasFogs by remember { mutableStateOf(true) }
    var headlight by remember { mutableStateOf(HeadlightType.Projector) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Color(0x22FFFFFF), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge)
                Text("Tell the app what hardware you have so mappings use every light.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssistChip(onClick = { headlight = HeadlightType.Projector }, label = { Text("Headlights: Projector") })
                    AssistChip(onClick = { headlight = HeadlightType.Reflector }, label = { Text("Headlights: Reflector") })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = hasFogs, onCheckedChange = { hasFogs = it })
                    Spacer(Modifier.width(8.dp)); Text("Front fog lights present")
                }
                Button(onClick = { /* save */ }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
            }
        }
    }
}

/* -------------------------------- LIBRARY -------------------------------- */

@Composable
fun LibraryScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Color(0x22FFFFFF), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Library", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Saved shows will appear here with duration, style, and export status.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
    }
}