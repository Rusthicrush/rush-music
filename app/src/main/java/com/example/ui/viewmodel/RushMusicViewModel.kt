package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientSynthPlayer
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.PlaylistEntity
import com.example.data.database.SongEntity
import com.example.data.database.SongRepository
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RushMusicViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "RushMusicViewModel"
    private val repository: SongRepository

    // Global Visual Design Themes State
    val selectedTheme = MutableStateFlow(AppThemePreset.MIDNIGHT_SKY)

    // Screen State: "home", "now_playing", "library", "chat"
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Player State Redirection from AmbientSynthPlayer
    val isPlaying = AmbientSynthPlayer.isPlaying
    val currentSong = AmbientSynthPlayer.currentSong
    val currentTimeMs = AmbientSynthPlayer.currentTimeMs
    val durationMs = AmbientSynthPlayer.durationMs
    val volume = AmbientSynthPlayer.volume
    val waveformData = AmbientSynthPlayer.waveformData
    val isShuffle = AmbientSynthPlayer.isShuffle
    val isRepeat = AmbientSynthPlayer.isRepeat
    val playQueue = AmbientSynthPlayer.playQueue
    val sleepTimeRemaining = AmbientSynthPlayer.sleepTimeRemaining
    val eqBands = AmbientSynthPlayer.eqBands

    // Room Database Flow State
    val allSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val favoriteSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val recentlyPlayed = MutableStateFlow<List<SongEntity>>(emptyList())
    val mostListened = MutableStateFlow<List<SongEntity>>(emptyList())
    val playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())

    // Selection Flows
    val songsInActivePlaylist = MutableStateFlow<List<SongEntity>>(emptyList())
    val activePlaylist = MutableStateFlow<PlaylistEntity?>(null)

    // Filter/Search queries
    val searchQuery = MutableStateFlow("")
    val activeMoodFilter = MutableStateFlow<String?>(null)

    // Character chat logs
    private val _chatHistory = MutableStateFlow<List<Pair<String,String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<String,String>>> = _chatHistory.asStateFlow()
    
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Gemini interpretation for the active song's lyrics!
    private val _songInterpretation = MutableStateFlow<String?>(null)
    val songInterpretation: StateFlow<String?> = _songInterpretation.asStateFlow()
    
    private val _isExplainingLyrics = MutableStateFlow(false)
    val isExplainingLyrics: StateFlow<Boolean> = _isExplainingLyrics.asStateFlow()

    init {
        val songDao = AppDatabase.getDatabase(application).songDao()
        repository = SongRepository(songDao)

        // Initialize built-in tracks & playlists
        viewModelScope.launch {
            repository.enrollInitialSongsIfNeeded()
            observeDatabaseFlows()
        }
    }

    private fun observeDatabaseFlows() {
        viewModelScope.launch {
            repository.allSongs.collect { allSongs.value = it }
        }
        viewModelScope.launch {
            repository.favoriteSongs.collect { favoriteSongs.value = it }
        }
        viewModelScope.launch {
            repository.recentlyPlayed.collect { recentlyPlayed.value = it }
        }
        viewModelScope.launch {
            repository.mostListened.collect { mostListened.value = it }
        }
        viewModelScope.launch {
            repository.allPlaylists.collect { playlists.value = it }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // Playback Wrapper Functions
    fun playSong(song: SongEntity, queue: List<SongEntity> = allSongs.value) {
        val idx = queue.indexOfFirst { it.id == song.id }
        AmbientSynthPlayer.setQueue(queue, if (idx != -1) idx else 0)
        AmbientSynthPlayer.play(getApplication())
        
        // Track analytics
        updatePlayStats(song)
    }

    private fun updatePlayStats(song: SongEntity) {
        viewModelScope.launch {
            val updated = song.copy(
                playCount = song.playCount + 1,
                recentTimestamp = System.currentTimeMillis()
            )
            repository.updateSong(updated)
        }
    }

    fun togglePlayPause() {
        AmbientSynthPlayer.togglePlayPause(getApplication())
    }

    fun seekTo(timeMs: Long) {
        AmbientSynthPlayer.seekTo(timeMs)
    }

    fun next() {
        AmbientSynthPlayer.next(getApplication())
    }

    fun prev() {
        AmbientSynthPlayer.prev(getApplication())
    }

    fun toggleShuffle() {
        isShuffle.value = !isShuffle.value
    }

    fun toggleRepeat() {
        isRepeat.value = !isRepeat.value
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            val updated = song.copy(isFavorite = !song.isFavorite)
            repository.updateSong(updated)
        }
    }

    fun setVolume(vol: Float) {
        AmbientSynthPlayer.setVolume(vol)
    }

    // Timer and Equalizer controls
    fun startSleepTimer(minutes: Int) {
        AmbientSynthPlayer.startSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        AmbientSynthPlayer.stopSleepTimer()
    }

    fun updateEqBands(bands: List<Int>) {
        eqBands.value = bands
    }

    // Playlist Management
    fun createPlaylist(name: String, desc: String) {
        viewModelScope.launch {
            val playlist = PlaylistEntity(name = name, description = desc)
            repository.insertPlaylist(playlist)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (activePlaylist.value?.id == playlistId) {
                activePlaylist.value = null
                songsInActivePlaylist.value = emptyList()
            }
        }
    }

    fun addSongToPlaylist(songId: Long, playlistId: Long) {
        viewModelScope.launch {
            repository.insertPlaylistSong(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(songId: Long, playlistId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            if (activePlaylist.value?.id == playlistId) {
                loadPlaylistSongs(playlistId)
            }
        }
    }

    fun selectPlaylist(playlist: PlaylistEntity) {
        activePlaylist.value = playlist
        loadPlaylistSongs(playlist.id)
    }

    private fun loadPlaylistSongs(playlistId: Long) {
        viewModelScope.launch {
            repository.getSongsInPlaylist(playlistId).collect {
                songsInActivePlaylist.value = it
            }
        }
    }

    // Local song importer
    fun importLocalSong(title: String, artist: String, durationMs: Long, path: String) {
        viewModelScope.launch {
            val newSong = SongEntity(
                title = title,
                artist = artist,
                durationMs = durationMs,
                path = path,
                isBuiltIn = false,
                lyrics = "No local lyrics. Click 'Interpret' to consult Luna.",
                mood = "Solitude",
                category = "Imported"
            )
            repository.insertSong(newSong)
        }
    }

    // Interactive Character Luna Dialogue via Gemini
    fun sendMessageToLuna(message: String) {
        if (message.isBlank()) return
        
        val theme = selectedTheme.value
        val companionName = theme.companionName
        val systemPrompt = theme.systemPrompt

        // Append user chat bubble
        _chatHistory.value = _chatHistory.value + ("User" to message)
        _isChatLoading.value = true

        viewModelScope.launch {
            val response = GeminiClient.getCharacterResponse(message, systemPrompt)
            _isChatLoading.value = false
            _chatHistory.value = _chatHistory.value + (companionName to response)
            
            // Adjust song play or mood suggestion if character guides a mood!
            val lower = response.lowercase()
            val suggestedMood = when {
                lower.contains("solitude") || lower.contains("steel") || lower.contains("resilience") -> "Solitude"
                lower.contains("strength") || lower.contains("strong") || lower.contains("endure") || lower.contains("pulse") -> "Strength"
                lower.contains("ethereal") || lower.contains("cosmic") || lower.contains("sky") || lower.contains("starlight") || lower.contains("angel") -> "Ethereal"
                else -> "Calm Night"
            }
            activeMoodFilter.value = suggestedMood
        }
    }

    // Explain lyrics with Gemini API!
    fun interpretActiveSongLyrics() {
        val song = currentSong.value ?: return
        if (_isExplainingLyrics.value) return

        val theme = selectedTheme.value
        val companionName = theme.companionName

        _isExplainingLyrics.value = true
        _songInterpretation.value = "$companionName is examining the frequencies of '${song.title}'..."

        val prompt = "Interpret the meaning or aesthetic heart of the song '${song.title}' by '${song.artist}' which has these lyrics or themes: '${song.lyrics ?: "Serene music frequencies and silence."}'. Keep it brief, poetic, and mentally comforting."
        val systemPrompt = "You are $companionName, a ${theme.companionRole} who interprets music for deep mental wellness. Adhere strictly to your default style and persona: ${theme.systemPrompt}"

        viewModelScope.launch {
            val result = GeminiClient.getCharacterResponse(prompt, systemPrompt)
            _songInterpretation.value = result
            _isExplainingLyrics.value = false
        }
    }

    fun clearLyricsInterpretation() {
        _songInterpretation.value = null
    }

    override fun onCleared() {
        super.onCleared()
        AmbientSynthPlayer.release()
    }
}

class RushMusicViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RushMusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RushMusicViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
