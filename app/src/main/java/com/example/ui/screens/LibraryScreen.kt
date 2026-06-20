package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.database.PlaylistEntity
import com.example.data.database.SongEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.GlowAccent
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoftWhite
import com.example.ui.theme.SparkBlue
import com.example.ui.theme.LocalThemePreset
import com.example.ui.viewmodel.RushMusicViewModel

@Composable
fun LibraryScreen(viewModel: RushMusicViewModel) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    val allSongs by viewModel.allSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val activePlaylist by viewModel.activePlaylist.collectAsState()
    val playlistSongs by viewModel.songsInActivePlaylist.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistNameInput by remember { mutableStateOf("") }
    var playlistDescInput by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importTitleInput by remember { mutableStateOf("") }
    var importArtistInput by remember { mutableStateOf("") }
    var importDurationMinsInput by remember { mutableStateOf("3") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Active Sub-playlist viewer backplate
        if (activePlaylist != null) {
            val pl = activePlaylist!!
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable { viewModel.activePlaylist.value = null },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SoftWhite)
                        Text(
                            text = "Back to Library",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftGray
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deletePlaylist(pl.id) },
                        modifier = Modifier.testTag("delete_playlist_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = SoftGray)
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(
                        text = pl.name,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = SoftWhite
                    )
                    if (pl.description.isNotEmpty()) {
                        Text(
                            text = pl.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (playlistSongs.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Playlist empty.\nAdd songs from the library below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftGray
                            )
                        }
                    }
                }
            } else {
                items(playlistSongs) { song ->
                    SongListInteractiveRow(
                        song = song,
                        onPlayClick = { viewModel.playSong(song, playlistSongs) },
                        onActionClick = { viewModel.removeSongFromPlaylist(song.id, pl.id) },
                        actionIcon = Icons.Default.RemoveCircleOutline,
                        actionDescription = "Remove from Playlist"
                    )
                }
            }

            // Separator to select other library tracks to add!
            item {
                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = SparkBlue)
                    Text(
                        text = "Add Songs to '${pl.name}'",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftWhite
                    )
                }
            }

            val songsToAddToPlaylist = allSongs.filter { s1 -> playlistSongs.none { s2 -> s1.id == s2.id } }
            if (songsToAddToPlaylist.isEmpty()) {
                item {
                    Text("All library frequencies are already enrolled in this playlist.", style = MaterialTheme.typography.labelSmall, color = SoftGray)
                }
            } else {
                items(songsToAddToPlaylist) { song ->
                    SongListInteractiveRow(
                        song = song,
                        onPlayClick = { viewModel.playSong(song) },
                        onActionClick = { viewModel.addSongToPlaylist(song.id, pl.id) },
                        actionIcon = Icons.Default.Add,
                        actionDescription = "Add to Playlist"
                    )
                }
            }

        } else {
            // Main Library View
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = SoftWhite
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showCreatePlaylistDialog = true },
                            modifier = Modifier.testTag("create_playlist_btn")
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = "New Playlist", tint = SoftWhite)
                        }
                        IconButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.testTag("import_song_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Import Songs", tint = SoftWhite)
                        }
                    }
                }
            }

            // Search input field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search title, artist, key...", color = SoftGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SoftGray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = SoftGray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.04f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedIndicatorColor = SparkBlue,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Playlists horizontal category selection
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.QueueMusic, contentDescription = "Playlists", tint = SparkBlue)
                        Text(
                            text = "Playlists",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SoftWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (playlists.isEmpty()) {
                        Text(
                            text = "No custom playlists. Create one above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftGray
                        )
                    } else {
                        playlists.forEach { pl ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectPlaylist(pl) }
                                    .padding(vertical = 4.dp)
                                    .testTag("playlist_item_${pl.id}"),
                                cornerRadius = 16.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pl.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = SoftWhite
                                        )
                                        Text(
                                            text = pl.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SoftGray
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = SoftGray)
                                }
                            }
                        }
                    }
                }
            }

            // Local Songs Header
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = "Local Songs", tint = SparkBlue)
                    Text(
                        text = "All Device Frequencies",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SoftWhite
                    )
                }
            }

            // List of ALL local & built-in songs
            val filteredSongs = allSongs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true)
            }
            if (filteredSongs.isEmpty()) {
                item {
                    Text(
                        text = "No tracks match your query.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(filteredSongs) { song ->
                    SongListInteractiveRow(
                        song = song,
                        onPlayClick = { viewModel.playSong(song) },
                        onActionClick = { viewModel.toggleFavorite(song) },
                        actionIcon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        actionColor = if (song.isFavorite) Color.Red else SoftWhite,
                        actionDescription = "Toggle Favorite"
                    )
                }
            }
        }

        // Extra spacing at bottom for background floating now playing bar
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // CREATE PLAYLIST DIALOG
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = playlistNameInput,
                        onValueChange = { playlistNameInput = it },
                        label = { Text("Playlist Name") },
                        modifier = Modifier.fillMaxWidth().testTag("playlist_name_input")
                    )
                    OutlinedTextField(
                        value = playlistDescInput,
                        onValueChange = { playlistDescInput = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("playlist_desc_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistNameInput.isNotBlank()) {
                            viewModel.createPlaylist(playlistNameInput, playlistDescInput)
                            playlistNameInput = ""
                            playlistDescInput = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_playlist_confirm")
                ) {
                    Text("Create", color = SparkBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // IMPORT SONG DIALOG
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Offline Song") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Scan the virtual storage. Feel-centered custom songs can be simulated offline instantly.",
                        style = MaterialTheme.typography.bodySmall, color = SoftGray
                    )
                    OutlinedTextField(
                        value = importTitleInput,
                        onValueChange = { importTitleInput = it },
                        label = { Text("Song Title") },
                        modifier = Modifier.fillMaxWidth().testTag("import_title_input")
                    )
                    OutlinedTextField(
                        value = importArtistInput,
                        onValueChange = { importArtistInput = it },
                        label = { Text("Artist / Key") },
                        modifier = Modifier.fillMaxWidth().testTag("import_artist_input")
                    )
                    OutlinedTextField(
                        value = importDurationMinsInput,
                        onValueChange = { importDurationMinsInput = it },
                        label = { Text("Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth().testTag("import_duration_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importTitleInput.isNotBlank()) {
                            val durationDecimal = importDurationMinsInput.toDoubleOrNull() ?: 3.0
                            val durationMs = (durationDecimal * 60 * 1000).toLong()
                            // Simulate a path. The player will synthesize this custom song beautiful too!
                            viewModel.importLocalSong(
                                title = importTitleInput,
                                artist = importArtistInput.ifBlank { "Offline Soul" },
                                durationMs = durationMs,
                                path = "" // Empty path signals synthesizer playback or fake file path simulation
                            )
                            importTitleInput = ""
                            importArtistInput = ""
                            importDurationMinsInput = "3"
                            showImportDialog = false
                        }
                    },
                    modifier = Modifier.testTag("import_song_confirm")
                ) {
                    Text("Import", color = SparkBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SongListInteractiveRow(
    song: SongEntity,
    onPlayClick: () -> Unit,
    onActionClick: () -> Unit,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    actionColor: Color = LocalThemePreset.current.textPrimary,
    actionDescription: String
) {
    val currentThemePreset = LocalThemePreset.current
    val SparkBlue = currentThemePreset.primaryColor
    val GlowAccent = currentThemePreset.accentColor
    val SoftWhite = currentThemePreset.textPrimary
    val SoftGray = currentThemePreset.textSecondary

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayClick() }
            .padding(vertical = 4.dp)
            .testTag("lib_song_row_${song.id}"),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Miniature launcher icon representing active state
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = SparkBlue,
                    modifier = Modifier.size(20.dp)
                )

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

            IconButton(onClick = onActionClick) {
                Icon(actionIcon, contentDescription = actionDescription, tint = actionColor)
            }
        }
    }
}
