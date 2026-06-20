package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SongEntity
import com.example.ui.components.EqualizerDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.SleepTimerDialog
import com.example.ui.theme.*
import com.example.ui.theme.LocalThemePreset
import com.example.ui.viewmodel.RushMusicViewModel

@Composable
fun NowPlayingScreen(viewModel: RushMusicViewModel) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    val activeSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val playQueue by viewModel.playQueue.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isRepeat by viewModel.isRepeat.collectAsState()
    val waveformData by viewModel.waveformData.collectAsState()
    val sleepTimeRemaining by viewModel.sleepTimeRemaining.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()
    val songInterpretation by viewModel.songInterpretation.collectAsState()
    val isExplainingLyrics by viewModel.isExplainingLyrics.collectAsState()

    // Dialog trigger states
    var showSleepTimer by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    var showQueueDrawer by remember { mutableStateOf(false) }

    if (activeSong == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("now_playing_empty"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.NightsStay,
                    contentDescription = "No song",
                    tint = SparkBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No song is playing. Touch the night sky on Home.",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftGray,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val song = activeSong!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("now_playing_screen"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Sleek top navigation actions panel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo("home") }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = SoftWhite)
                }

                Text(
                    text = "NOW FEELING",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                    color = SoftGray,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showSleepTimer = true },
                        modifier = Modifier.testTag("timer_dialog_trigger")
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimeRemaining > 0) GlowAccent else SoftWhite
                        )
                    }
                    IconButton(
                        onClick = { showEqualizer = true },
                        modifier = Modifier.testTag("eq_dialog_trigger")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Equalizer", tint = SoftWhite)
                    }
                }
            }
        }

        // 2. Large Album Art Representing deep space stars
        item {
            GlassCard(
                modifier = Modifier
                    .size(280.dp)
                    .testTag("album_art_card"),
                cornerRadius = 28.dp,
                glowColor = SparkBlue
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing planetary glow base matching playback
                    CanvasWavesBase(isPlaying = isPlaying)

                    Icon(
                        Icons.Default.NightsStay,
                        contentDescription = "Celestial",
                        tint = SoftWhite.copy(alpha = 0.85f),
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
        }

        // 3. Song Title & Artist info with Favorite selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = SoftWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(song) },
                    modifier = Modifier.testTag("favorite_btn")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) Color.Red else SoftWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // 4. Live Waveforom Visualizer
        item {
            InteractiveWaveform(waveformData = waveformData)
        }

        // 5. Progress slider & time indicators
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                val formattedCurrentTime = formatTime(currentTimeMs)
                val formattedDuration = formatTime(durationMs)
                
                Slider(
                    value = if (durationMs > 0) currentTimeMs.toFloat() else 0f,
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..(if (durationMs > 0) durationMs.toFloat() else 100f),
                    colors = SliderDefaults.colors(
                        activeTrackColor = SparkBlue,
                        thumbColor = SparkBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_progress_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formattedCurrentTime, style = MaterialTheme.typography.labelSmall, color = SoftGray)
                    Text(text = formattedDuration, style = MaterialTheme.typography.labelSmall, color = SoftGray)
                }
            }
        }

        // 6. Floating Glass play controls row
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceSpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = { viewModel.toggleShuffle() },
                        modifier = Modifier.testTag("shuffle_btn")
                    ) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) GlowAccent else SoftWhite.copy(alpha = 0.5f)
                        )
                    }

                    // Prev
                    IconButton(
                        onClick = { viewModel.prev() },
                        modifier = Modifier.testTag("prev_btn")
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = SoftWhite, modifier = Modifier.size(36.dp))
                    }

                    // Play Pause FAB
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(64.dp)
                            .background(SparkBlue, CircleShape)
                            .testTag("play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = DefaultBlackForPlayAccent(isPlaying),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = { viewModel.next() },
                        modifier = Modifier.testTag("next_btn")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = SoftWhite, modifier = Modifier.size(36.dp))
                    }

                    // Repeat
                    IconButton(
                        onClick = { viewModel.toggleRepeat() },
                        modifier = Modifier.testTag("repeat_btn")
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = "Repeat",
                            tint = if (isRepeat) GlowAccent else SoftWhite.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // 7. Interactive Song Lyrics & Gemini Reflection Panel
        item {
            LyricsContentWidget(
                lyrics = song.lyrics ?: "This instrumental frequency flows silently through the dark. Pure waves.",
                songInterpretation = songInterpretation,
                isExplaining = isExplainingLyrics,
                onInterpret = { viewModel.interpretActiveSongLyrics() },
                onClearInterpret = { viewModel.clearLyricsInterpretation() }
            )
        }

        // 8. Queue Management Header Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showQueueDrawer = !showQueueDrawer }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QueueMusic, contentDescription = "Queue", tint = SparkBlue)
                    Text(
                        text = "Music Queue (${playQueue.size} songs)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftWhite
                    )
                }
                Icon(
                    imageVector = if (showQueueDrawer) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Queue",
                    tint = SoftGray
                )
            }
        }

        // Queue drawer lists
        if (showQueueDrawer) {
            itemsIndexed(playQueue) { index, item ->
                val isSelected = item.id == song.id
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playSong(item, playQueue) }
                        .padding(vertical = 4.dp),
                    cornerRadius = 12.dp,
                    borderStroke = if (isSelected) BorderStroke(1.dp, GlowAccent) else BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "%02d.".format(index + 1),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) GlowAccent else SoftGray
                            )
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) GlowAccent else SoftWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.artist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftGray
                                )
                            }
                        }

                        if (isSelected && isPlaying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = GlowAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = formatTime(item.durationMs),
                                style = MaterialTheme.typography.labelMedium,
                                color = SoftGray
                            )
                        }
                    }
                }
            }
        }

        // Space at bottom so queue isn't cut off by nav gesture card
        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // --- Active Overlay Dialog Box Renderers ---
    if (showSleepTimer) {
        SleepTimerDialog(
            currentMinutesRemaining = sleepTimeRemaining,
            onDismiss = { showSleepTimer = false },
            onStartTimer = { viewModel.startSleepTimer(it) },
            onCancelTimer = { viewModel.cancelSleepTimer() }
        )
    }

    if (showEqualizer) {
        EqualizerDialog(
            currentBands = eqBands,
            onDismiss = { showEqualizer = false },
            onBandsUpdated = { viewModel.updateEqBands(it) }
        )
    }
}

@Composable
fun CanvasWavesBase(isPlaying: Boolean) {
    // Drawn base with a solid glowing night core
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.02f))
    )
}

@Composable
fun InteractiveWaveform(waveformData: List<Float>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        waveformData.forEach { ampl ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(ampl)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GlowAccent, SparkBlue)
                        )
                    )
            )
        }
    }
}

@Composable
fun LyricsContentWidget(
    lyrics: String,
    songInterpretation: String?,
    isExplaining: Boolean,
    onInterpret: () -> Unit,
    onClearInterpret: () -> Unit
) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lyrics_card"),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ShortText, contentDescription = "Lyrics", tint = SparkBlue)
                    Text(
                        text = "Song Lyrics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftWhite
                    )
                }
            }

            // Lyrics Box
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = lyrics,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    color = SoftWhite.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Utility: Format Milliseconds into standard m:ss notation
fun formatTime(ms: Long): String {
    val totalSecs = ms / 1000
    val mm = totalSecs / 60
    val ss = totalSecs % 60
    return "%d:%02d".format(mm, ss)
}

fun DefaultBlackForPlayAccent(isPlaying: Boolean): Color {
    return Color(0xFF000411) // Soft premium rich contrast
}

// Arrangments spacing mapping fix
private val Arrangement.SpaceSpaceBetween: Arrangement.Horizontal
    get() = Arrangement.SpaceBetween
