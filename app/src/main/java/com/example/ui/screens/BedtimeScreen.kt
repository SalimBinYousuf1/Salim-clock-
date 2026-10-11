package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BedtimeScheduleEntity
import com.example.sound.SoundSynthesizer
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.BedtimeDeepNavy
import com.example.ui.theme.BedtimeGradient
import com.example.ui.theme.BedtimeRoseGold
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TabularDisplayLarge
import com.example.ui.theme.TabularHeadline
import com.example.ui.theme.TabularTitle
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun BedtimeScreen(
    schedule: BedtimeScheduleEntity?,
    onUpdateSchedule: (BedtimeScheduleEntity) -> Unit
) {
    val context = LocalContext.current
    val currentSchedule = schedule ?: BedtimeScheduleEntity()

    var isEnabled by remember { mutableStateOf(currentSchedule.isEnabled) }
    var bedHour by remember { mutableIntStateOf(currentSchedule.bedHour) }
    var bedMinute by remember { mutableIntStateOf(currentSchedule.bedMinute) }
    var wakeHour by remember { mutableIntStateOf(currentSchedule.wakeHour) }
    var wakeMinute by remember { mutableIntStateOf(currentSchedule.wakeMinute) }
    var windDownMins by remember { mutableIntStateOf(currentSchedule.windDownMinutes) }

    // Sleep Sound state
    var selectedSound by remember { mutableStateOf(currentSchedule.sleepSoundName) }
    var soundTimerMins by remember { mutableIntStateOf(currentSchedule.sleepSoundTimerMinutes) }
    var isSoundPlaying by remember { mutableStateOf(false) }

    // Smart wake sensor state
    var smartWakeWindow by remember { mutableIntStateOf(25) }
    var currentMotionVariance by remember { mutableFloatStateOf(0.12f) }
    var currentAmbientLux by remember { mutableFloatStateOf(8.5f) }

    // Register live sensor listener for motion & ambient light heuristic
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val g = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
                    currentMotionVariance = Math.abs(g).toFloat()
                } else if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    currentAmbientLux = event.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager?.unregisterListener(listener)
            SoundSynthesizer.stop()
        }
    }

    // Calculate sleep target duration
    val sleepDurationHours = remember(bedHour, bedMinute, wakeHour, wakeMinute) {
        var diffMinutes = (wakeHour * 60 + wakeMinute) - (bedHour * 60 + bedMinute)
        if (diffMinutes <= 0) diffMinutes += 24 * 60
        val hrs = diffMinutes / 60
        val mins = diffMinutes % 60
        Pair(hrs, mins)
    }

    // Sleep ring fraction (8 hrs ideal = 1.0)
    val sleepRingProgress = remember(sleepDurationHours) {
        val totalMins = sleepDurationHours.first * 60 + sleepDurationHours.second
        (totalMins.toFloat() / (8 * 60f)).coerceIn(0.1f, 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E1038), // Diffuse deep royal navy to rose gold accent
                        Color(0xFF0A0A0F),
                        Color(0xFF0A0A0F)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bedtime Routine",
                            style = TabularHeadline,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Smart wake window & calendar sleep protection",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            onUpdateSchedule(currentSchedule.copy(isEnabled = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = BedtimeRoseGold)
                    )
                }
            }

            // Calendar Early-Morning Commitment Suggestion Card
            if (currentSchedule.hasCalendarSuggestion) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF241530)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventNote, contentDescription = null, tint = BedtimeRoseGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Calendar Sleep Protection",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Early commitment detected: \"${currentSchedule.calendarEventTitle}\". Going to bed 45 min earlier (${String.format("%02d:%02d", (bedHour - 1).coerceAtLeast(0), 15)}) preserves your full 8-hour target.",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    bedHour = 22
                                    bedMinute = 15
                                    wakeHour = currentSchedule.lastSuggestedWakeHour
                                    wakeMinute = currentSchedule.lastSuggestedWakeMinute
                                    onUpdateSchedule(
                                        currentSchedule.copy(
                                            bedHour = 22,
                                            bedMinute = 15,
                                            wakeHour = wakeHour,
                                            wakeMinute = wakeMinute
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BedtimeRoseGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apply Earlier Bedtime (22:15)", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Hero Bedtime to Wake Circular Ring
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141026)),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SLEEP SCHEDULE",
                            color = BedtimeRoseGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.size(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SalimProgressRing(
                                progress = sleepRingProgress,
                                colors = BedtimeGradient,
                                strokeWidth = 14.dp,
                                faceStyle = ClockFaceStyle.Continuity,
                                accessibilityText = "Sleep target: ${sleepDurationHours.first} hours ${sleepDurationHours.second} minutes",
                                innerContent = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${sleepDurationHours.first}h ${sleepDurationHours.second}m",
                                            style = TabularDisplayLarge,
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Sleep Target",
                                            color = BedtimeRoseGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Time Interval Indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = BedtimeRoseGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bedtime", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    String.format("%02d:%02d", bedHour, bedMinute),
                                    style = TabularHeadline,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Wake Up", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    String.format("%02d:%02d", wakeHour, wakeMinute),
                                    style = TabularHeadline,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Wind-down Reminder
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141026)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wind-down Notification", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("${windDownMins}m before bed", color = BedtimeRoseGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Slider(
                            value = windDownMins.toFloat(),
                            onValueChange = {
                                windDownMins = it.toInt()
                                onUpdateSchedule(currentSchedule.copy(windDownMinutes = it.toInt()))
                            },
                            valueRange = 15f..60f,
                            colors = SliderDefaults.colors(thumbColor = BedtimeRoseGold, activeTrackColor = BedtimeRoseGold)
                        )
                    }
                }
            }

            // Clean Looping Sleep Sounds
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141026)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = BedtimeRoseGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Looping Sleep Sounds", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }

                            IconButton(
                                onClick = {
                                    if (isSoundPlaying) {
                                        SoundSynthesizer.stop()
                                        isSoundPlaying = false
                                    } else {
                                        SoundSynthesizer.playSound(selectedSound, isLooping = true)
                                        isSoundPlaying = true
                                    }
                                }
                            ) {
                                Icon(
                                    if (isSoundPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = BedtimeRoseGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sound selector pills
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SoundSynthesizer.SLEEP_SOUNDS) { sound ->
                                FilterChip(
                                    selected = selectedSound == sound,
                                    onClick = {
                                        selectedSound = sound
                                        if (isSoundPlaying) {
                                            SoundSynthesizer.playSound(sound, isLooping = true)
                                        }
                                    },
                                    label = { Text(sound, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BedtimeRoseGold.copy(alpha = 0.3f),
                                        selectedLabelColor = BedtimeRoseGold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Timer: ${soundTimerMins}m auto-stop", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            // Smart Wake Window & Heuristic Sensor Monitor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141026)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = BedtimeRoseGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Smart Wake Window Heuristic", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Monitors micro-movements and ambient light before alarm to gently wake you during lighter sleep phases.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Smart Window Duration", color = Color.LightGray, fontSize = 12.sp)
                            Text("${smartWakeWindow} minutes", color = BedtimeRoseGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Slider(
                            value = smartWakeWindow.toFloat(),
                            onValueChange = { smartWakeWindow = it.toInt() },
                            valueRange = 15f..45f,
                            colors = SliderDefaults.colors(thumbColor = BedtimeRoseGold, activeTrackColor = BedtimeRoseGold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Sensor Diagnostic Panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1834), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Motion Variance", color = Color.Gray, fontSize = 11.sp)
                                Text(String.format("%.2f m/s²", currentMotionVariance), color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Ambient Light", color = Color.Gray, fontSize = 11.sp)
                                Text(String.format("%.1f lux", currentAmbientLux), color = Color(0xFFFFD166), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Heuristic State", color = Color.Gray, fontSize = 11.sp)
                                Text(if (currentMotionVariance > 0.5f) "Restless" else "Resting", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "*Note: Heuristic based on device motion and light sensors. Salim does not represent clinical or medical sleep diagnosis.",
                            color = Color.DarkGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
