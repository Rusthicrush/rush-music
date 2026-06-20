package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.data.database.SongEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.math.sin

object AmbientSynthPlayer {
    private const val TAG = "AmbientSynthPlayer"
    private const val SAMPLE_RATE = 22050
    private const val BUFFER_SIZE = SAMPLE_RATE * 2 // 2 seconds buffer total in blocks

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Player State
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _volume = MutableStateFlow(0.8f) // 0.0f to 1.0f
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // Real-time Waveform data for stunning UI visuals!
    private val _waveformData = MutableStateFlow<List<Float>>(List(40) { 0.1f })
    val waveformData: StateFlow<List<Float>> = _waveformData.asStateFlow()

    // Shuffle and Repeat Settings
    val isShuffle = MutableStateFlow(false)
    val isRepeat = MutableStateFlow(false)

    // Current Queue
    private val _playQueue = MutableStateFlow<List<SongEntity>>(emptyList())
    val playQueue: StateFlow<List<SongEntity>> = _playQueue.asStateFlow()
    private var queueIndex = 0

    // Sleep Timer (Ticks down in seconds, stops audio on expiration)
    private val _sleepTimeRemaining = MutableStateFlow(0) // Seconds
    val sleepTimeRemaining: StateFlow<Int> = _sleepTimeRemaining.asStateFlow()
    private var sleepTimerJob: Job? = null

    // Equalizer State (5 bands, value from -12dB to +12dB, stored as UI display)
    val eqBands = MutableStateFlow(listOf(0, 0, 0, 0, 0)) // Standard Eq bars representation

    private var appContext: Context? = null

    fun setQueue(songs: List<SongEntity>, startIndex: Int) {
        _playQueue.value = songs
        queueIndex = if (startIndex in songs.indices) startIndex else 0
        songs.getOrNull(queueIndex)?.let {
            selectSong(it)
        }
    }

    private fun selectSong(song: SongEntity) {
        stopCurrentEngine()
        _currentSong.value = song
        _durationMs.value = song.durationMs
        _currentTimeMs.value = 0L
    }

    fun play(context: Context) {
        appContext = context.applicationContext
        val song = currentSong.value ?: return
        if (_isPlaying.value) return

        _isPlaying.value = true
        if (song.isBuiltIn || song.path.isNullOrEmpty()) {
            startSynthesizerEngine(song.mood)
        } else {
            startMediaPlayerEngine(context, song.path)
        }
    }

    fun pause() {
        if (!_isPlaying.value) return
        _isPlaying.value = false
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        // Synth job is paused by stopping generation but retaining position
    }

    fun next(context: Context) {
        val queue = _playQueue.value
        if (queue.isEmpty()) return

        if (isRepeat.value) {
            _currentTimeMs.value = 0L
            play(context)
            return
        }

        if (isShuffle.value) {
            queueIndex = queue.indices.random()
        } else {
            queueIndex = (queueIndex + 1) % queue.size
        }

        queue.getOrNull(queueIndex)?.let {
            selectSong(it)
            play(context)
        }
    }

    fun prev(context: Context) {
        val queue = _playQueue.value
        if (queue.isEmpty()) return

        if (isShuffle.value) {
            queueIndex = queue.indices.random()
        } else {
            queueIndex = if (queueIndex - 1 < 0) queue.size - 1 else queueIndex - 1
        }

        queue.getOrNull(queueIndex)?.let {
            selectSong(it)
            play(context)
        }
    }

    fun togglePlayPause(context: Context) {
        appContext = context.applicationContext
        if (_isPlaying.value) {
            pause()
        } else {
            play(context)
        }
    }

    fun seekTo(timeMs: Long) {
        _currentTimeMs.value = timeMs
        mediaPlayer?.let {
            if (it.isPlaying || _isPlaying.value) {
                try {
                    it.seekTo(timeMs.toInt())
                } catch (e: Exception) {
                    Log.e(TAG, "Seek error", e)
                }
            }
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        mediaPlayer?.let {
            try {
                it.setVolume(clamped, clamped)
            } catch (e: Exception) {
                Log.e(TAG, "SetVolume error", e)
            }
        }
        audioTrack?.let {
            try {
                it.setVolume(clamped)
            } catch (e: Exception) {
                Log.e(TAG, "AudioTrack SetVolume error", e)
            }
        }
    }

    // --- Sleep Timer ---
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimeRemaining.value = minutes * 60
        sleepTimerJob = scope.launch {
            while (_sleepTimeRemaining.value > 0) {
                delay(1000)
                _sleepTimeRemaining.value -= 1
            }
            // Sleep Timer Expired! Stop music
            withContext(Dispatchers.Main) {
                pause()
            }
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimeRemaining.value = 0
    }

    // --- Media Player Engine ---
    private fun startMediaPlayerEngine(context: Context, path: String) {
        _isPlaying.value = true
        scope.launch(Dispatchers.Main) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    
                    // Direct Uri/Path initialization or Uri if file is from picked content
                    if (path.startsWith("content://")) {
                        setDataSource(context, Uri.parse(path))
                    } else {
                        val file = File(path)
                        if (file.exists()) {
                            setDataSource(file.absolutePath)
                        } else {
                            // Fallback if missing
                            Log.e(TAG, "File does not exist: $path")
                            throw Exception("File not found")
                        }
                    }
                    
                    setVolume(_volume.value, _volume.value)
                    prepare()
                    seekTo(_currentTimeMs.value.toInt())
                    start()
                }

                // Completion listener
                mediaPlayer?.setOnCompletionListener {
                    scope.launch {
                        next(context)
                    }
                }

                // Track position loop
                launch {
                    val fallbackWaveform = DoubleArray(40) { 0.1 }
                    var counter = 0
                    while (_isPlaying.value && mediaPlayer != null) {
                        try {
                            _currentTimeMs.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                        } catch (e: Exception) {
                            break
                        }

                        // Simulate animated waveform amplitude mirroring realistic playback
                        val rawWaves = List(40) { index ->
                            val phase = (counter * 0.1) + (index * 0.2)
                            val ampMultiplier = if (index in 12..28) 0.8f else 0.3f
                            val base = sin(phase) * ampMultiplier
                            (base + sin(phase * 2.3) * 0.2f + 0.5f).toFloat().coerceIn(0.05f, 0.95f)
                        }
                        _waveformData.value = rawWaves
                        counter++
                        delay(100)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play local file $path", e)
                withContext(Dispatchers.Main) {
                    // Fail gracefully fallback to synthesized song of of Solitude
                    _currentSong.value?.let {
                        val fallbackSong = it.copy(isBuiltIn = true, path = null)
                        _currentSong.value = fallbackSong
                        startSynthesizerEngine(fallbackSong.mood)
                    }
                }
            }
        }
    }

    // --- Synthesizer Engine ---
    private fun startSynthesizerEngine(mood: String) {
        synthJob?.cancel()
        _isPlaying.value = true

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize.coerceAtLeast(BUFFER_SIZE))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(_volume.value)
        audioTrack?.play()

        // Synth frequencies based on Mood Chords:
        // C3 = 130.81, E3 = 164.81, G3 = 196.00, B3 = 246.94 -> Cmaj7 (Calm Night)
        // A2 = 110.00, C3 = 130.81, E3 = 164.81, G3 = 196.00 -> Am7 (Solitude)
        // E3 = 164.81, G3 = 196.00, B3 = 246.94, D4 = 293.66 -> Em7 (Strength)
        // F3 = 174.61, A3 = 220.00, C4 = 261.63, E4 = 329.63 -> Fmaj7 (Ethereal)
        val chordFreqs = when (mood) {
            "Solitude" -> doubleArrayOf(110.0, 130.81, 164.81, 196.0) // Am7
            "Strength" -> doubleArrayOf(146.83, 174.61, 220.0, 261.63, 293.66) // Dm7 and Em7 blend
            "Ethereal" -> doubleArrayOf(174.61, 220.0, 261.63, 329.63, 392.00) // Fmaj9 sweep
            else -> doubleArrayOf(130.81, 164.81, 196.00, 246.94) // Cmaj7 (Calm Night)
        }

        synthJob = scope.launch(Dispatchers.Default) {
            val shortBuffer = ShortArray(1024)
            var phase = 0.0
            val sweepCycles = SAMPLE_RATE * 8 // 8 seconds sweep
            var sampleCounter = (_currentTimeMs.value * SAMPLE_RATE / 1000).toInt()

            while (isActive && _isPlaying.value) {
                // Synthesize sweet, slow pulsing spatial sound
                val pulseHz = 0.08 // Sub-1Hz panning pulse (Calming breath rhythm!)
                
                for (i in shortBuffer.indices) {
                    val t = sampleCounter / SAMPLE_RATE.toDouble()
                    
                    // Breathing pulse amplitude envelope for zen-meditative breathing (1 breath per 12 seconds)
                    val breathPhase = 2 * Math.PI * 0.083 * t
                    val breathAmp = 0.4 + 0.6 * (0.5 * (1.0 + sin(breathPhase)))

                    // Add chord notes (multi-sine waves with low-pass/warm characteristics)
                    var waveSum = 0.0
                    for (freqIdx in chordFreqs.indices) {
                        val freq = chordFreqs[freqIdx]
                        // Slow shimmering detune per wave to make it feel rich and organic!
                        val detune = 1.0 + 0.003 * sin(2.0 * Math.PI * (0.1 * (freqIdx + 1)) * t)
                        waveSum += sin(2.0 * Math.PI * freq * detune * t + (freqIdx * 0.5))
                    }
                    
                    // Normalize sum
                    waveSum /= chordFreqs.size
                    
                    // Add a tiny ethereal high-bell twinkle note periodically (every 5 seconds)
                    val twinklePeriod = SAMPLE_RATE * 5
                    val twinkleIdx = sampleCounter % twinklePeriod
                    if (twinkleIdx < SAMPLE_RATE * 1.5) { // Twinkle decays in 1.5s
                        val twT = twinkleIdx / SAMPLE_RATE.toDouble()
                        val twFreq = 880.0 // A5 high shimmering note
                        val twAmp = (1.0 - (twT / 1.5)) * 0.15 * sin(2.0 * Math.PI * twFreq * twT)
                        waveSum += twAmp
                    }

                    // Apply Master breathing amplitude & scaling
                    val sampleValue = (waveSum * breathAmp * 32767 * 0.25).toInt()
                    shortBuffer[i] = sampleValue.coerceIn(-32768, 32767).toShort()
                    sampleCounter++
                }

                // Write to AudioTrack
                audioTrack?.let { track ->
                    try {
                        track.write(shortBuffer, 0, shortBuffer.size)
                    } catch (e: Exception) {
                        Log.e(TAG, "AudioTrack write error", e)
                    }
                }

                // Calculate visualizer and advance timer in loop
                _currentTimeMs.value = (sampleCounter.toLong() * 1000) / SAMPLE_RATE
                
                // Track ends? Built-in looped default playback or proceed to next
                if (_currentTimeMs.value >= _durationMs.value) {
                    sampleCounter = 0
                    _currentTimeMs.value = 0L
                    if (!isRepeat.value) {
                        withContext(Dispatchers.Main) {
                            appContext?.let { next(it) } // Trigger next
                        }
                        break
                    }
                }

                // Update Waveform matching the synthesized audio blocks
                val visualWaves = List(40) { index ->
                    val blockIdx = (index * (shortBuffer.size / 40)).coerceIn(shortBuffer.indices)
                    val value = Math.abs(shortBuffer[blockIdx].toFloat() / 32768f)
                    (value * 2.8f + 0.08f).coerceIn(0.05f, 0.95f)
                }
                _waveformData.value = visualWaves
            }
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
    }

    private fun stopCurrentEngine() {
        _isPlaying.value = false
        synthJob?.cancel()
        synthJob = null
        
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Safe ignore
        }
        audioTrack = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Safe ignore
        }
        mediaPlayer = null
    }

    fun release() {
        stopCurrentEngine()
        scope.cancel()
    }
}
