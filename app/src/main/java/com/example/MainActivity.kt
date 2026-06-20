package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.StarBackground
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.RushMusicViewModel
import com.example.ui.viewmodel.RushMusicViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val app = context.applicationContext as android.app.Application
                val viewModel: RushMusicViewModel = viewModel(
                    factory = RushMusicViewModelFactory(app)
                )

                val currentScreen by viewModel.currentScreen.collectAsState()
                val currentSong by viewModel.currentSong.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val currentTimeMs by viewModel.currentTimeMs.collectAsState()
                val durationMs by viewModel.durationMs.collectAsState()
                val selectedTheme by viewModel.selectedTheme.collectAsState()

                CompositionLocalProvider(LocalThemePreset provides selectedTheme) {
                    // Star background covers everything globally for cohesive styling theme experience
                    StarBackground(theme = selectedTheme) {
                        Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent, // Transparent Scaffold reveals the stellar starry background!
                        bottomBar = {
                            // Only show bottom navigation if active screen is NOT the full panel Now Playing
                            if (currentScreen != "now_playing") {
                                CustomBottomNavBar(
                                    currentScreen = currentScreen,
                                    onTabSelected = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    bottom = if (currentScreen != "now_playing") innerPadding.calculateBottomPadding() else 0.dp
                                )
                        ) {
                            // Crossfade screen navigation transition
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350))
                                },
                                label = "screen_transition"
                            ) { screen ->
                                when (screen) {
                                    "home" -> HomeScreen(viewModel = viewModel)
                                    "library" -> LibraryScreen(viewModel = viewModel)
                                    "now_playing" -> NowPlayingScreen(viewModel = viewModel)
                                    else -> HomeScreen(viewModel = viewModel)
                                }
                            }

                            // iOS-styled Floating Glass Now Playing Bar!
                            // Rendered hovering above navigation tabs if there is an active playing song
                            if (currentSong != null && currentScreen != "now_playing") {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                                ) {
                                    MiniFloatingPlayerBar(
                                        title = currentSong!!.title,
                                        artist = currentSong!!.artist,
                                        isPlaying = isPlaying,
                                        progress = if (durationMs > 0) currentTimeMs.toFloat() / durationMs.toFloat() else 0f,
                                        onPlayPauseClick = { viewModel.togglePlayPause() },
                                        onNextClick = { viewModel.next() },
                                        onBarClick = { viewModel.navigateTo("now_playing") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun CustomBottomNavBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    // Glassmorphism bottom navigation bar
    // Uses window insets navigation bars padding to avoid system overlap safe criteria!
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("app_bottom_nav"),
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home tab
            BottomNavTabButton(
                icon = if (currentScreen == "home") Icons.Default.NightsStay else Icons.Outlined.NightsStay,
                label = "Feel tonight",
                isSelected = currentScreen == "home",
                onClick = { onTabSelected("home") },
                modifier = Modifier.testTag("nav_home_tab")
            )

            // Library tab
            BottomNavTabButton(
                icon = if (currentScreen == "library") Icons.Default.LibraryMusic else Icons.Outlined.LibraryMusic,
                label = "Library",
                isSelected = currentScreen == "library",
                onClick = { onTabSelected("library") },
                modifier = Modifier.testTag("nav_library_tab")
            )
        }
    }
}

@Composable
fun BottomNavTabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) GlowAccent else SoftGray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) SoftWhite else SoftGray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun MiniFloatingPlayerBar(
    title: String,
    artist: String,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onBarClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBarClick() }
            .testTag("mini_player_bar"),
        cornerRadius = 18.dp,
        borderStroke = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.2f)),
        glowColor = SparkBlue.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Song text
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SparkBlue.copy(alpha = 0.2f), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.NightsStay,
                            contentDescription = "Playing",
                            tint = GlowAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SoftWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Controls row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.size(40.dp).testTag("mini_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = SoftWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextClick,
                        modifier = Modifier.size(40.dp).testTag("mini_next")
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = SoftWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Elegant, micro progress-indicator line at very bottom edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(GlowAccent)
                )
            }
        }
    }
}
