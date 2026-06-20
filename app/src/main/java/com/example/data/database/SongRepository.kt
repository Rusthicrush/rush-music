package com.example.data.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SongRepository(private val songDao: SongDao) {
    val allSongs: Flow<List<SongEntity>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = songDao.getFavoriteSongs()
    val recentlyPlayed: Flow<List<SongEntity>> = songDao.getRecentlyPlayed()
    val mostListened: Flow<List<SongEntity>> = songDao.getMostListened()
    val allPlaylists: Flow<List<PlaylistEntity>> = songDao.getAllPlaylists()

    fun getSongsByMood(mood: String): Flow<List<SongEntity>> = songDao.getSongsByMood(mood)
    fun getSongsByCategory(category: String): Flow<List<SongEntity>> = songDao.getSongsByCategory(category)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>> = songDao.getSongsInPlaylist(playlistId)

    suspend fun insertSongs(songs: List<SongEntity>) = songDao.insertSongs(songs)
    suspend fun insertSong(song: SongEntity): Long = songDao.insertSong(song)
    suspend fun updateSong(song: SongEntity) = songDao.updateSong(song)
    suspend fun deleteSong(song: SongEntity) = songDao.deleteSong(song)

    suspend fun insertPlaylist(playlist: PlaylistEntity): Long = songDao.insertPlaylist(playlist)
    suspend fun deletePlaylist(playlistId: Long) = songDao.deletePlaylist(playlistId)
    suspend fun insertPlaylistSong(playlistId: Long, songId: Long) =
        songDao.insertPlaylistSong(PlaylistSongCrossRef(playlistId, songId))
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        songDao.removeSongFromPlaylist(playlistId, songId)

    suspend fun enrollInitialSongsIfNeeded() {
        val count = allSongs.first().size
        if (count == 0) {
            val initialSongs = listOf(
                SongEntity(
                    title = "Midnight Solitude",
                    artist = "Rush",
                    durationMs = 225000,
                    mood = "Solitude",
                    lyrics = "In the quiet of the dark, we find who we are.\nNo flashy lights, just the stars.\nA quiet mind holds an entire world inside.",
                    isBuiltIn = true,
                    category = "Tonight's Playlist"
                ),
                SongEntity(
                    title = "Tethered to Stars",
                    artist = "Luna",
                    durationMs = 252000,
                    mood = "Calm Night",
                    lyrics = "We are all just particles waiting to ignite.\nCalm outside, but a blazing fire inside.\nFloat on the deep blue tide.",
                    isBuiltIn = true,
                    category = "Tonight's Playlist"
                ),
                SongEntity(
                    title = "Mental Fortitude",
                    artist = "Rush",
                    durationMs = 198000,
                    mood = "Strength",
                    lyrics = "The storms will rage, the noise will cry,\nBut we stand steady, silent under the night.\nStrong inside, simple outside.",
                    isBuiltIn = true,
                    category = "Tonight's Playlist"
                ),
                SongEntity(
                    title = "Ethereal Silence",
                    artist = "Eclipse",
                    durationMs = 302000,
                    mood = "Ethereal",
                    lyrics = "Soft heart, titanium mind.\nSome songs are not heard, they are felt.\nLet the quiet wash over you.",
                    isBuiltIn = true,
                    category = "Tonight's Playlist"
                ),
                SongEntity(
                    title = "Whispering Nebula",
                    artist = "Stellar",
                    durationMs = 210000,
                    mood = "Calm Night",
                    lyrics = "Words are too loud, let the chords speak.\nDeep within the nebula, we find our peace.\nFloating, dreaming, living.",
                    isBuiltIn = true,
                    category = "Tonight's Playlist"
                )
            )
            songDao.insertSongs(initialSongs)

            // Dynamic default playlists
            val playlists = listOf(
                PlaylistEntity(name = "Midnight Coffee", description = "Steeped in silence and stars"),
                PlaylistEntity(name = "Mental Recovery", description = "Deep resonance of the inner self")
            )
            playlists.forEach {
                songDao.insertPlaylist(it)
            }
        }
    }
}
