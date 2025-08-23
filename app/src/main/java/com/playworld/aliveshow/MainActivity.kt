package com.playworld.aliveshow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val nav = rememberNavController()
    MaterialTheme(colorScheme = darkColors()) {
        Scaffold(bottomBar = { BottomBar(nav) }) { padding ->
            NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
                composable("home")    { HomeScreen { nav.navigate("preview") } }
                composable("preview") { PreviewScreen() }
                composable("settings"){ SettingsScreen() }
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val items = listOf(
        NavItem("home","Home", Icons.Outlined.Home),
        NavItem("preview","Preview", Icons.Outlined.PlayCircleOutline),
        NavItem("settings","Settings", Icons.Outlined.Settings)
    )
    NavigationBar {
        val backStack by nav.currentBackStackEntryAsState()
        val current = backStack?.destination
        items.forEach { item ->
            val selected = current?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) nav.navigate(item.route) },
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

enum class AudioMode { DialogueOnly, DialogueMusic, MusicOnly }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onGoPreview: () -> Unit) {
    var mode by remember { mutableStateOf(AudioMode.DialogueMusic) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CenterAlignedTopAppBar(title = { Text("AliveShow") })
        Card(elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Start a new show", style = MaterialTheme.typography.titleLarge)
                Text("Pick your audio type. The app adapts options and mapping accordingly.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ModeChips(mode) { mode = it }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedButton(onClick = { /* TODO: pick audio */ },
                        leadingIcon = { Icon(Icons.Outlined.AudioFile, null) }) { Text("Pick audio") }
                    FilledTonalButton(onClick = { /* TODO: record */ },
                        leadingIcon = { Icon(Icons.Outlined.Mic, null) }) { Text("Quick record") }
                }
                AssistChip(onClick = { /* TODO */ }, label = { Text("Personality: Jarvis") })
            }
        }
        Card(elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("One-tap generate", style = MaterialTheme.typography.titleLarge)
                Text("Preview Eyes, Mouth, and Gestures before export.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onGoPreview, modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)) {
                    Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Generate & Preview")
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("Tip: you’ll export to a USB /LightShow folder when ready.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ModeChips(selected: AudioMode, onChange: (AudioMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected==AudioMode.DialogueOnly, onClick = { onChange(AudioMode.DialogueOnly) }, label = { Text("Dialogue only") })
        FilterChip(selected = selected==AudioMode.DialogueMusic, onClick = { onChange(AudioMode.DialogueMusic) }, label = { Text("Dialogue + Music") })
        FilterChip(selected = selected==AudioMode.MusicOnly,  onClick = { onChange(AudioMode.MusicOnly)  }, label = { Text("Music only") })
    }
}

@Composable
fun PreviewScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Preview", style = MaterialTheme.typography.headlineSmall)
        Text("Live mock. The generator will drive these channels once wired in.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth().height(240.dp), elevation = CardDefaults.cardElevation(6.dp)) {
            Box(Modifier.padding(12.dp)) { CarFaceMock() }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { /* TODO */ }) { Text("Play") }
            OutlinedButton(onClick = { /* TODO */ }) { Text("Stop") }
        }
        Button(onClick = { /* TODO: export */ }, modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)) {
            Icon(Icons.Outlined.Usb, null); Spacer(Modifier.width(8.dp)); Text("Export to USB /LightShow")
        }
    }
}

@Composable
fun CarFaceMock() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawRoundRect(Color(0xFF1B2437), topLeft = Offset(w*0.05f, h*0.1f),
            size = androidx.compose.ui.geometry.Size(w*0.9f, h*0.8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f,40f),
            style = Stroke(width = 3f))
        drawCircle(Color(0xFF9EC3FF), 18f, Offset(w*0.22f, h*0.47f))
        drawCircle(Color(0xFF9EC3FF), 18f, Offset(w*0.78f, h*0.47f))
        drawLine(Color(0xFFCCE0FF), Offset(w*0.15f, h*0.42f), Offset(w*0.29f, h*0.42f), 6f)
        drawLine(Color(0xFFCCE0FF), Offset(w*0.71f, h*0.42f), Offset(w*0.85f, h*0.42f), 6f)
        drawLine(Color(0xFFFFCC99), Offset(w*0.35f, h*0.60f), Offset(w*0.65f, h*0.60f), 10f)
        drawCircle(Color(0xFFFFE8B0), 8f, Offset(w*0.12f, h*0.52f))
        drawCircle(Color(0xFFFFE8B0), 8f, Offset(w*0.88f, h*0.52f))
        drawLine(Color(0xFFFF8080), Offset(w*0.40f, h*0.86f), Offset(w*0.60f, h*0.86f), 8f)
    }
}

enum class HeadlightType { Projector, Reflector }

@Composable
fun SettingsScreen() {
    var hasFogs by remember { mutableStateOf(true) }
    var headlight by remember { mutableStateOf(HeadlightType.Projector) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Tell the app what hardware you have so mappings use every light.")
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vehicle profile", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssistChip(onClick = { headlight = HeadlightType.Projector }, label = { Text("Headlights: Projector") })
                    AssistChip(onClick = { headlight = HeadlightType.Reflector }, label = { Text("Headlights: Reflector") })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = hasFogs, onCheckedChange = { hasFogs = it })
                    Spacer(Modifier.width(8.dp)); Text("Front fog lights present")
                }
                Text("Projectors → outer mains as boolean; ramps on DRL/inner mains.")
            }
        }
        Button(onClick = { /* TODO: persist */ }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
    }
}

private fun darkColors(): ColorScheme = darkColorScheme(
    primary = Color(0xFFFF6600), secondary = Color(0xFFFF6600),
    background = Color(0xFF0F172A), surface = Color(0xFF0B1222),
    onBackground = Color(0xFFE5EAF2), onSurface = Color(0xFFE5EAF2)
)
