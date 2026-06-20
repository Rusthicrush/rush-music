package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SongEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.GlowAccent
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.SparkBlue
import com.example.ui.theme.LocalThemePreset
import com.example.ui.theme.AppThemePreset
import com.example.ui.viewmodel.RushMusicViewModel

@Composable
fun HomeScreen(viewModel: RushMusicViewModel) {
    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val mostListened by viewModel.mostListened.collectAsState()

    // Query active design configuration dynamically!
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary
    
    // Welcome message
    val welcomeMessage = "Some songs are not heard, they are felt."

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // 1. Header & Welcome message
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = currentThemePreset.appHeaderName,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = SoftWhite
                )
                Text(
                     text = currentThemePreset.appSlogan,
                     style = MaterialTheme.typography.titleMedium.copy(
                         fontStyle = FontStyle.Italic,
                         letterSpacing = 0.5.sp
                     ),
                     color = SoftGray,
                     modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // 1b. Atmosphere Designs Changer Carousel!
        item {
            ThemeAtmosphereSelector(viewModel)
        }

        // 2. Interactive Sound & Atmosphere Frequency Console
        item {
            AtmosphereVisualizerWidget(viewModel)
        }

        // 3. Tonight's Playlist Section
        item {
            val tonightSongs = allSongs.filter { it.category == "Tonight's Playlist" }
            HorizontalSongListSection(
                title = "Tonight's Playlist",
                icon = Icons.Default.NightsStay,
                songs = tonightSongs,
                onSongClick = { song -> viewModel.playSong(song, tonightSongs) }
            )
        }

        // 4. Mood Collections Grid Buttons
        item {
            MoodCollectionSection(viewModel)
        }

        // 5. Favorite Collection
        item {
            HorizontalSongListSection(
                title = "Favorite Songs",
                icon = Icons.Default.Favorite,
                songs = favoriteSongs,
                onSongClick = { song -> viewModel.playSong(song, favoriteSongs) },
                emptyText = "Click the heart on a song to group it here."
            )
        }

        // 6. Recently Played
        item {
            HorizontalSongListSection(
                title = "Recently Played",
                icon = Icons.Default.History,
                songs = recentlyPlayed,
                onSongClick = { song -> viewModel.playSong(song, recentlyPlayed) },
                emptyText = "Frequencies you listened to will rest here."
            )
        }

        // 7. Most Listened
        item {
            HorizontalSongListSection(
                title = "Most Listened",
                icon = Icons.Default.TrendingUp,
                songs = mostListened,
                onSongClick = { song -> viewModel.playSong(song, mostListened) },
                emptyText = "Songs of high playcount will settle here."
            )
        }

        // Extra spacing at bottom for the floating now playing bar
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun AtmosphereVisualizerWidget(viewModel: RushMusicViewModel) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    // Local state for customized sound deck
    var spatialFocus by remember { mutableStateOf(0.7f) }
    var bassDepth by remember { mutableStateOf(0.5f) }
    var trebleSpark by remember { mutableStateOf(0.6f) }
    var vocalGlow by remember { mutableStateOf(0.8f) }

    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    // Periodic animation values for 8 frequency bands
    val heights = listOf(0.4f, 0.8f, 0.5f, 0.9f, 0.6f, 0.85f, 0.45f, 0.7f).mapIndexed { index, baseValue ->
        infiniteTransition.animateFloat(
            initialValue = baseValue * 0.3f,
            targetValue = baseValue,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(
                    durationMillis = 600 + (index * 120),
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "band_$index"
        )
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("frequency_console_widget"),
        cornerRadius = 24.dp,
        glowColor = SparkBlue
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SparkBlue.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Console Icon",
                        tint = GlowAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Acoustic Frequency Console",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftWhite
                    )
                    Text(
                        text = "Customize the spatial soundstage environment of ${currentThemePreset.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftGray
                    )
                }

                // Mini reactive equalizer wave
                Row(
                    modifier = Modifier
                        .height(24.dp)
                        .width(42.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    heights.take(5).forEach { animHeight ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(animHeight.value)
                                .background(GlowAccent, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.08f))

            // Adjustable console sliders
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Slider 1: Spatial Surround Focus
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AllOut,
                                contentDescription = null,
                                tint = GlowAccent.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Spatial Stage Width",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SoftWhite
                            )
                        }
                        Text(
                            text = "${(spatialFocus * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlowAccent
                        )
                    }
                    Slider(
                        value = spatialFocus,
                        onValueChange = { spatialFocus = it },
                        colors = SliderDefaults.colors(
                            thumbColor = GlowAccent,
                            activeTrackColor = GlowAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(18.dp)
                    )
                }

                // Slider 2: Tactile Bass Depth
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = GlowAccent.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "LFE Sub Bass Core",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SoftWhite
                            )
                        }
                        Text(
                            text = "${(bassDepth * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlowAccent
                        )
                    }
                    Slider(
                        value = bassDepth,
                        onValueChange = { bassDepth = it },
                        colors = SliderDefaults.colors(
                            thumbColor = GlowAccent,
                            activeTrackColor = GlowAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(18.dp)
                    )
                }

                // Slider 3: Acoustic Vocals Clarity
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = GlowAccent.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Acoustic Midrange Vocals",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SoftWhite
                            )
                        }
                        Text(
                            text = "${(vocalGlow * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlowAccent
                        )
                    }
                    Slider(
                        value = vocalGlow,
                        onValueChange = { vocalGlow = it },
                        colors = SliderDefaults.colors(
                            thumbColor = GlowAccent,
                            activeTrackColor = GlowAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(18.dp)
                    )
                }
            }

            // Quick Atmospheric Modes presets row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Cinema", "Concert", "Studio", "Nature").forEach { modeName ->
                    val isSelected = (modeName == "Cinema" && spatialFocus > 0.8f && bassDepth > 0.7f) ||
                            (modeName == "Concert" && spatialFocus > 0.8f && bassDepth < 0.7f && vocalGlow > 0.7f) ||
                            (modeName == "Studio" && spatialFocus < 0.5f && bassDepth < 0.6f && vocalGlow < 0.9f) ||
                            (modeName == "Nature" && trebleSpark > 0.5f && vocalGlow > 0.5f) // random state flags
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) SparkBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                when (modeName) {
                                    "Cinema" -> {
                                        spatialFocus = 0.95f
                                        bassDepth = 0.85f
                                        vocalGlow = 0.65f
                                    }
                                    "Concert" -> {
                                        spatialFocus = 0.85f
                                        bassDepth = 0.50f
                                        vocalGlow = 0.90f
                                    }
                                    "Studio" -> {
                                        spatialFocus = 0.40f
                                        bassDepth = 0.40f
                                        vocalGlow = 0.50f
                                    }
                                    "Nature" -> {
                                        spatialFocus = 0.60f
                                        bassDepth = 0.30f
                                        vocalGlow = 0.75f
                                    }
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = modeName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) GlowAccent else SoftWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodCollectionSection(viewModel: RushMusicViewModel) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    val activeMoodFilter by viewModel.activeMoodFilter.collectAsState()
    val moods = listOf(
        Triple("Solitude", Icons.Default.FilterHdr, "Steel mind, soft heart"),
        Triple("Calm Night", Icons.Default.NightsStay, "Gently floating"),
        Triple("Ethereal", Icons.Default.BubbleChart, "Vast spatial sweep"),
        Triple("Strength", Icons.Default.Shield, "Silent endurance")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.CloudQueue, contentDescription = "Moods", tint = SparkBlue)
            Text(
                text = "Mood Collections",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SoftWhite
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(moods) { (moodName, icon, subText) ->
                val isSelected = activeMoodFilter == moodName
                GlassCard(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable {
                            if (isSelected) {
                                viewModel.activeMoodFilter.value = null
                            } else {
                                viewModel.activeMoodFilter.value = moodName
                            }
                        }
                        .testTag("mood_card_$moodName"),
                    cornerRadius = 18.dp,
                    borderStroke = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) GlowAccent else Color.White.copy(alpha = 0.12f)
                    ),
                    glowColor = if (isSelected) SparkBlue else Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = moodName,
                            tint = if (isSelected) GlowAccent else SoftWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = moodName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SoftWhite
                            )
                            Text(
                                text = subText,
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalSongListSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    songs: List<SongEntity>,
    onSongClick: (SongEntity) -> Unit,
    emptyText: String = "No tracks matching this resonance of waves."
) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = title, tint = SparkBlue, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SoftWhite
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (songs.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 16.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftGray,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(songs) { song ->
                    SongCompactCard(song = song, onClick = { onSongClick(song) })
                }
            }
        }
    }
}

@Composable
fun SongCompactCard(song: SongEntity, onClick: () -> Unit) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    GlassCard(
        modifier = Modifier
            .width(148.dp)
            .clickable { onClick() }
            .testTag("song_card_${song.id}"),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Album art representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                // Drawing dynamic stars inside album cover or music notation
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Sound Wave",
                    tint = SparkBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(44.dp)
                )
            }
            
            Column {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SoftWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ThemeAtmosphereSelector(viewModel: RushMusicViewModel) {
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val theme = LocalThemePreset.current
    val SparkBlue = theme.primaryColor
    val GlowAccent = theme.accentColor
    val SoftWhite = theme.textPrimary
    val SoftGray = theme.textSecondary

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Atmosphere Preset Selector",
                tint = SparkBlue,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Atmosphere Designs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SoftWhite
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(AppThemePreset.values()) { preset ->
                val isSelected = selectedTheme == preset
                GlassCard(
                    modifier = Modifier
                        .clickable { viewModel.selectedTheme.value = preset }
                        .testTag("theme_preset_${preset.name}"),
                    cornerRadius = 16.dp,
                    borderStroke = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) GlowAccent else Color.White.copy(alpha = 0.12f)
                    ),
                    glowColor = if (isSelected) SparkBlue else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // mini colors preview circle dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(preset.primaryColor, CircleShape)
                        )
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) SoftWhite else SoftWhite.copy(alpha = 0.70f)
                        )
                    }
                }
            }
        }
    }
}
