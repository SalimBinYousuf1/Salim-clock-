package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object SoundSynthesizer {
    private var activeJob: Job? = null
    private var activeTrack: AudioTrack? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    val ALARM_SOUNDS = listOf(
        "Radiant Horizon",
        "Zen Bell",
        "Chimes",
        "Energetic Pulse",
        "Digital Marimba",
        "Aurora Synth"
    )

    val SLEEP_SOUNDS = listOf(
        "Rain on Window",
        "Forest Night",
        "Ocean Waves",
        "White Noise",
        "Gentle Stream",
        "Cosmic Drone"
    )

    fun playSound(name: String, isLooping: Boolean = true, volume: Float = 1.0f) {
        stop()

        activeJob = scope.launch {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            activeTrack = track
            track.setVolume(volume.coerceIn(0f, 1f))
            track.play()

            val buffer = ShortArray(minBufferSize)
            var sampleIndex = 0L

            while (isActive) {
                fillBufferForSound(name, buffer, sampleIndex, sampleRate)
                try {
                    val written = track.write(buffer, 0, buffer.size)
                    if (written < 0) break
                } catch (_: Exception) {
                    break
                }
                sampleIndex += buffer.size
                if (!isLooping && sampleIndex > sampleRate * 3) {
                    break
                }
            }
        }
    }

    fun setVolume(volume: Float) {
        activeTrack?.setVolume(volume.coerceIn(0f, 1f))
    }

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        try {
            activeTrack?.stop()
            activeTrack?.release()
        } catch (_: Exception) {}
        activeTrack = null
    }

    private fun fillBufferForSound(name: String, buffer: ShortArray, startSample: Long, sampleRate: Int) {
        val rand = Random(42)
        for (i in buffer.indices) {
            val t = (startSample + i).toDouble() / sampleRate
            val sampleVal: Double = when (name) {
                "Zen Bell" -> {
                    // Fundamental + harmonics with slow decay pulsing
                    val cycle = t % 2.5
                    val decay = Math.exp(-cycle * 1.8)
                    (sin(2.0 * PI * 432.0 * t) * 0.6 +
                     sin(2.0 * PI * 864.0 * t) * 0.25 +
                     sin(2.0 * PI * 1296.0 * t) * 0.15) * decay
                }
                "Chimes" -> {
                    // Shimmering multi-tone frequencies
                    val cycle = t % 1.8
                    val decay = Math.exp(-cycle * 2.2)
                    (sin(2.0 * PI * 659.25 * t) * 0.35 +
                     sin(2.0 * PI * 880.0 * t) * 0.35 +
                     sin(2.0 * PI * 1318.51 * t) * 0.3) * decay
                }
                "Energetic Pulse" -> {
                    // Upbeat rhythmic synth arpeggio
                    val step = ((t * 4.0).toInt() % 4)
                    val freq = when (step) {
                        0 -> 440.0
                        1 -> 554.37
                        2 -> 659.25
                        else -> 880.0
                    }
                    val subCycle = (t * 4.0) % 1.0
                    val env = (1.0 - subCycle) * 0.8
                    sin(2.0 * PI * freq * t) * env
                }
                "Digital Marimba" -> {
                    // Wooden harmonic percussive tone
                    val cycle = (t * 3.0) % 1.0
                    val decay = Math.exp(-cycle * 6.0)
                    (sin(2.0 * PI * 523.25 * t) * 0.7 +
                     sin(2.0 * PI * 1569.75 * t) * 0.3) * decay
                }
                "Aurora Synth" -> {
                    // Gentle sweeping ambient chord
                    val lfo = (sin(2.0 * PI * 0.3 * t) + 1.0) * 0.5
                    (sin(2.0 * PI * 220.0 * t) * 0.4 +
                     sin(2.0 * PI * 277.18 * t) * 0.3 +
                     sin(2.0 * PI * 329.63 * t) * 0.3) * (0.6 + 0.4 * lfo)
                }
                "Rain on Window" -> {
                    // Filtered noise with rhythmic droplets
                    val noise = (rand.nextDouble() * 2.0 - 1.0) * 0.25
                    val drop = if (rand.nextDouble() < 0.0008) (sin(2.0 * PI * 1200.0 * t) * 0.3) else 0.0
                    noise + drop
                }
                "Forest Night" -> {
                    // Ambient wind + gentle high-frequency cricket chirps
                    val wind = sin(2.0 * PI * 0.15 * t) * 0.15
                    val cricket = if ((t % 1.2) < 0.18) (sin(2.0 * PI * 4600.0 * t) * 0.12) else 0.0
                    wind + cricket
                }
                "Ocean Waves" -> {
                    // Oscillating swell of filtered white noise
                    val swell = (sin(2.0 * PI * 0.1 * t) + 1.0) * 0.5
                    val noise = (rand.nextDouble() * 2.0 - 1.0)
                    noise * (0.1 + 0.35 * swell)
                }
                "White Noise" -> {
                    (rand.nextDouble() * 2.0 - 1.0) * 0.3
                }
                "Gentle Stream" -> {
                    val noise = (rand.nextDouble() * 2.0 - 1.0) * 0.2
                    val bubble = sin(2.0 * PI * (600.0 + sin(2.0 * PI * 3.0 * t) * 200.0) * t) * 0.1
                    noise + bubble
                }
                "Cosmic Drone" -> {
                    // Deep low frequency binaural drone
                    sin(2.0 * PI * 65.4 * t) * 0.4 +
                    sin(2.0 * PI * 98.0 * t) * 0.3 +
                    sin(2.0 * PI * 130.8 * t) * 0.2
                }
                else -> { // Radiant Horizon (default)
                    val cycle = t % 2.0
                    val decay = Math.exp(-cycle * 1.5)
                    (sin(2.0 * PI * 329.63 * t) * 0.4 +
                     sin(2.0 * PI * 493.88 * t) * 0.35 +
                     sin(2.0 * PI * 659.25 * t) * 0.25) * decay
                }
            }
            buffer[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
        }
    }
}
