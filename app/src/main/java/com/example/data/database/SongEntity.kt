package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val path: String? = null, // null for built-in synthesized songs, otherwise file path
    val durationMs: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val recentTimestamp: Long = 0,
    val mood: String = "Calm Night", // "Calm Night", "Solitude", "Ethereal", "Strength"
    val lyrics: String? = null,
    val isBuiltIn: Boolean = true,
    val category: String = "Tonight's Playlist"
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)
