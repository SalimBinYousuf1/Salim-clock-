package com.example.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.AlarmEntity
import com.example.sound.SoundSynthesizer
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.ClockShape
import com.example.ui.components.FillMode
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.AlarmGradient
import com.example.ui.theme.CoralPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditSheet(
    initialAlarm: AlarmEntity?,
    onSave: (AlarmEntity) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isNew = initialAlarm == null
    val alarm = initialAlarm ?: AlarmEntity(hour = 7, minute = 0)

    val timePickerState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = false
    )

    var label by remember { mutableStateOf(alarm.label) }
    var repeatDays by remember { mutableIntStateOf(alarm.repeatDays) }
    var isRecurring by remember { mutableStateOf(alarm.isRecurring) }
    var skipNextOccurrence by remember { mutableStateOf(alarm.skipNextOccurrence) }

    // Clock Face Customization
    var selectedFaceStyle by remember { mutableStateOf(alarm.clockFaceStyle) }
    var selectedShape by remember { mutableStateOf(alarm.clockShape) }
    var selectedFillMode by remember { mutableStateOf(alarm.fillMode) }

    // Dismissal challenge
    var dismissChallenge by remember { mutableStateOf(alarm.dismissChallenge) }
    var mathDifficulty by remember { mutableStateOf(alarm.mathDifficulty) }
    var phraseText by remember { mutableStateOf(alarm.phraseText) }
    var qrTargetCode by remember { mutableStateOf(alarm.qrTargetCode) }
    var shakeTargetCount by remember { mutableIntStateOf(alarm.shakeTargetCount) }

    // Sunrise alarm
    var sunriseEnabled by remember { mutableStateOf(alarm.sunriseEnabled) }
    var sunriseRampMinutes by remember { mutableIntStateOf(alarm.sunriseRampMinutes) }
    var sunriseStartBrightness by remember { mutableFloatStateOf(alarm.sunriseStartBrightness) }
    var sunriseEndBrightness by remember { mutableFloatStateOf(alarm.sunriseEndBrightness) }
    var sunriseStartColorTemp by remember { mutableIntStateOf(alarm.sunriseStartColorTemp) }
    var sunriseEndColorTemp by remember { mutableIntStateOf(alarm.sunriseEndColorTemp) }
    var showSunrisePreview by remember { mutableStateOf(false) }

    // Volume & Sound
    var volumeRampMinutes by remember { mutableIntStateOf(alarm.volumeRampMinutes) }
    var soundName by remember { mutableStateOf(alarm.soundName) }
    var isSoundAuditioning by remember { mutableStateOf(false) }

    // Vibration
    var vibrationPattern by remember { mutableStateOf(alarm.vibrationPattern) }
    var customVibePattern by remember { mutableStateOf(alarm.customVibePattern) }
    var showVibrationEditor by remember { mutableStateOf(false) }

    // Snooze & Escalation
    var maxSnoozeCount by remember { mutableIntStateOf(alarm.maxSnoozeCount) }
    var snoozeDurationMinutes by remember { mutableIntStateOf(alarm.snoozeDurationMinutes) }
    var escalatingSnooze by remember { mutableStateOf(alarm.escalatingSnooze) }
    var escalationEnabled by remember { mutableStateOf(alarm.escalationEnabled) }
    var escalationIntervalMinutes by remember { mutableIntStateOf(alarm.escalationIntervalMinutes) }
    var escalationSecondarySound by remember { mutableStateOf(alarm.escalationSecondarySound) }
    var emergencyContactName by remember { mutableStateOf(alarm.emergencyContactName) }
    var emergencyContactPhone by remember { mutableStateOf(alarm.emergencyContactPhone) }

    // Geofence & Smart wake
    var geofenceAutoSkip by remember { mutableStateOf(alarm.geofenceAutoSkip) }
    var smartWakeEnabled by remember { mutableStateOf(alarm.smartWakeEnabled) }
    var smartWakeWindowMinutes by remember { mutableIntStateOf(alarm.smartWakeWindowMinutes) }

    Dialog(
        onDismissRequest = {
            SoundSynthesizer.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0F)),
            color = Color(0xFF0A0A0F)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        SoundSynthesizer.stop()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }

                    Text(
                        text = if (isNew) "New Alarm" else "Edit Alarm",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Button(
                        onClick = {
                            SoundSynthesizer.stop()
                            val updated = alarm.copy(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                label = label.ifBlank { "Alarm" },
                                repeatDays = repeatDays,
                                isRecurring = isRecurring,
                                skipNextOccurrence = skipNextOccurrence,
                                clockFaceStyle = selectedFaceStyle,
                                clockShape = selectedShape,
                                fillMode = selectedFillMode,
                                dismissChallenge = dismissChallenge,
                                mathDifficulty = mathDifficulty,
                                phraseText = phraseText,
                                qrTargetCode = qrTargetCode,
                                shakeTargetCount = shakeTargetCount,
                                sunriseEnabled = sunriseEnabled,
                                sunriseRampMinutes = sunriseRampMinutes,
                                sunriseStartBrightness = sunriseStartBrightness,
                                sunriseEndBrightness = sunriseEndBrightness,
                                sunriseStartColorTemp = sunriseStartColorTemp,
                                sunriseEndColorTemp = sunriseEndColorTemp,
                                volumeRampMinutes = volumeRampMinutes,
                                soundName = soundName,
                                vibrationPattern = vibrationPattern,
                                customVibePattern = customVibePattern,
                                maxSnoozeCount = maxSnoozeCount,
                                snoozeDurationMinutes = snoozeDurationMinutes,
                                escalatingSnooze = escalatingSnooze,
                                escalationEnabled = escalationEnabled,
                                escalationIntervalMinutes = escalationIntervalMinutes,
                                escalationSecondarySound = escalationSecondarySound,
                                emergencyContactName = emergencyContactName,
                                emergencyContactPhone = emergencyContactPhone,
                                geofenceAutoSkip = geofenceAutoSkip,
                                smartWakeEnabled = smartWakeEnabled,
                                smartWakeWindowMinutes = smartWakeWindowMinutes,
                                isEnabled = true
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Time Picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF13131E))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = Color(0xFF1A1A2A),
                                selectorColor = CoralPrimary,
                                containerColor = Color(0xFF13131E),
                                timeSelectorSelectedContainerColor = CoralPrimary,
                                timeSelectorUnselectedContainerColor = Color(0xFF1E1E2E),
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContentColor = Color.LightGray
                            )
                        )
                    }

                    // Label input
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Alarm Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = Color(0xFF28283C)
                        )
                    )

                    // Repeat Days
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Repeat Schedule", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = isRecurring,
                                    onCheckedChange = { isRecurring = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                                )
                            }

                            if (isRecurring) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    days.forEachIndexed { index, day ->
                                        val isSelected = (repeatDays and (1 shl index)) != 0
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) CoralPrimary else Color(0xFF222234))
                                                .clickable {
                                                    repeatDays = if (isSelected) {
                                                        repeatDays and (1 shl index).inv()
                                                    } else {
                                                        repeatDays or (1 shl index)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.take(1),
                                                color = if (isSelected) Color.White else Color.LightGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Skip Next Occurrence", color = Color.White, fontSize = 13.sp)
                                Switch(
                                    checked = skipNextOccurrence,
                                    onCheckedChange = { skipNextOccurrence = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                                )
                            }
                        }
                    }

                    // Clock Face & Customization
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Clock Face System (Huawei Design Language)", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Choose from 6 customizable animated faces", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Face Styles
                            val faces = ClockFaceStyle.values()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                faces.take(3).forEach { face ->
                                    FilterChip(
                                        selected = selectedFaceStyle == face.displayName,
                                        onClick = { selectedFaceStyle = face.displayName },
                                        label = { Text(face.displayName, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CoralPrimary.copy(alpha = 0.3f),
                                            selectedLabelColor = CoralPrimary
                                        )
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                faces.drop(3).forEach { face ->
                                    FilterChip(
                                        selected = selectedFaceStyle == face.displayName,
                                        onClick = { selectedFaceStyle = face.displayName },
                                        label = { Text(face.displayName, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CoralPrimary.copy(alpha = 0.3f),
                                            selectedLabelColor = CoralPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Shape & Fill Parameters", color = Color.LightGray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Circle", "RoundedSquare", "NotchedSquircle").forEach { shapeName ->
                                    FilterChip(
                                        selected = selectedShape == shapeName,
                                        onClick = { selectedShape = shapeName },
                                        label = { Text(shapeName, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Gradient", "Solid").forEach { fillName ->
                                    FilterChip(
                                        selected = selectedFillMode == fillName,
                                        onClick = { selectedFillMode = fillName },
                                        label = { Text(fillName, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Dismissal Challenge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Dismissal Challenge", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Requires proof of wakefulness to silence", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            val challenges = listOf("None", "Math", "Shake", "QrCode", "Phrase")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                challenges.forEach { ch ->
                                    FilterChip(
                                        selected = dismissChallenge == ch,
                                        onClick = { dismissChallenge = ch },
                                        label = { Text(ch, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CoralPrimary.copy(alpha = 0.3f),
                                            selectedLabelColor = CoralPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (dismissChallenge) {
                                "Math" -> {
                                    Text("Difficulty Level:", color = Color.LightGray, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Easy", "Medium", "Hard").forEach { diff ->
                                            FilterChip(
                                                selected = mathDifficulty == diff,
                                                onClick = { mathDifficulty = diff },
                                                label = { Text(diff, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                                "Shake" -> {
                                    Text("Shakes Required: $shakeTargetCount", color = Color.LightGray, fontSize = 12.sp)
                                    Slider(
                                        value = shakeTargetCount.toFloat(),
                                        onValueChange = { shakeTargetCount = it.toInt() },
                                        valueRange = 10f..50f,
                                        colors = SliderDefaults.colors(thumbColor = CoralPrimary, activeTrackColor = CoralPrimary)
                                    )
                                }
                                "QrCode" -> {
                                    OutlinedTextField(
                                        value = qrTargetCode,
                                        onValueChange = { qrTargetCode = it },
                                        label = { Text("Target Code Text") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                "Phrase" -> {
                                    OutlinedTextField(
                                        value = phraseText,
                                        onValueChange = { phraseText = it },
                                        label = { Text("Challenge Statement") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // Sunrise Alarm
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD166))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sunrise Lighting Ramp", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                                Switch(
                                    checked = sunriseEnabled,
                                    onCheckedChange = { sunriseEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD166))
                                )
                            }

                            if (sunriseEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ramp Duration: $sunriseRampMinutes min", color = Color.LightGray, fontSize = 13.sp)
                                    Text("${sunriseStartColorTemp}K -> ${sunriseEndColorTemp}K", color = Color(0xFFFFD166), fontSize = 12.sp)
                                }

                                Slider(
                                    value = sunriseRampMinutes.toFloat(),
                                    onValueChange = { sunriseRampMinutes = it.toInt() },
                                    valueRange = 1f..30f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFFFFD166),
                                        activeTrackColor = Color(0xFFFFD166)
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { showSunrisePreview = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26263A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD166))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Preview Sunrise Ramp in App", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Sound & Volume Ramp-up
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Alarm Tone & Volume Ramp-up", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Tone Library:", color = Color.LightGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            SoundSynthesizer.ALARM_SOUNDS.chunked(2).forEach { rowSounds ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowSounds.forEach { sound ->
                                        FilterChip(
                                            selected = soundName == sound,
                                            onClick = {
                                                soundName = sound
                                                if (isSoundAuditioning) {
                                                    SoundSynthesizer.playSound(sound, isLooping = true)
                                                }
                                            },
                                            label = { Text(sound, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (isSoundAuditioning) {
                                            SoundSynthesizer.stop()
                                            isSoundAuditioning = false
                                        } else {
                                            SoundSynthesizer.playSound(soundName, isLooping = true)
                                            isSoundAuditioning = true
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(if (isSoundAuditioning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isSoundAuditioning) "Stop Audition" else "Audition Tone")
                                }

                                Text("Ramp: ${volumeRampMinutes}m", color = Color.LightGray, fontSize = 12.sp)
                            }

                            Slider(
                                value = volumeRampMinutes.toFloat(),
                                onValueChange = { volumeRampMinutes = it.toInt() },
                                valueRange = 0f..10f,
                                colors = SliderDefaults.colors(thumbColor = CoralPrimary, activeTrackColor = CoralPrimary)
                            )
                        }
                    }

                    // Vibration Pattern
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Vibration, contentDescription = null, tint = CoralPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Vibration Pattern", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                                Text(vibrationPattern, color = CoralPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showVibrationEditor = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26263A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = CoralPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Vibration Pattern Editor & Tap Recorder", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Snooze & Escalation Sequence
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Snooze & Escalation Sequence", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Max Snooze Count: $maxSnoozeCount", color = Color.LightGray, fontSize = 12.sp)
                                Text("Duration: ${snoozeDurationMinutes}m", color = Color.LightGray, fontSize = 12.sp)
                            }

                            Slider(
                                value = maxSnoozeCount.toFloat(),
                                onValueChange = { maxSnoozeCount = it.toInt() },
                                valueRange = 1f..10f,
                                colors = SliderDefaults.colors(thumbColor = CoralPrimary, activeTrackColor = CoralPrimary)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Escalating Snooze Duration (5m, 10m...)", color = Color.White, fontSize = 12.sp)
                                Switch(
                                    checked = escalatingSnooze,
                                    onCheckedChange = { escalatingSnooze = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Full Volume Escalation Sequence", color = Color.White, fontSize = 12.sp)
                                Switch(
                                    checked = escalationEnabled,
                                    onCheckedChange = { escalationEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                                )
                            }

                            if (escalationEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Auto full-volume re-trigger after $escalationIntervalMinutes minutes", color = Color.Gray, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = emergencyContactName,
                                    onValueChange = { emergencyContactName = it },
                                    label = { Text("Designated Contact (Opt-in)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Smart Wake Window & Geofence
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Smart Wake Window", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = smartWakeEnabled,
                                    onCheckedChange = { smartWakeEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                                )
                            }
                            Text("Best-effort heuristic monitoring motion & light within ${smartWakeWindowMinutes}m window", color = Color.Gray, fontSize = 11.sp)

                            if (smartWakeEnabled) {
                                Slider(
                                    value = smartWakeWindowMinutes.toFloat(),
                                    onValueChange = { smartWakeWindowMinutes = it.toInt() },
                                    valueRange = 10f..45f,
                                    colors = SliderDefaults.colors(thumbColor = CoralPrimary, activeTrackColor = CoralPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Geofence Auto-Skip (Away from home)", color = Color.White, fontSize = 12.sp)
                                Switch(
                                    checked = geofenceAutoSkip,
                                    onCheckedChange = { geofenceAutoSkip = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                                )
                            }
                        }
                    }

                    // Delete button if editing
                    if (!isNew && onDelete != null) {
                        OutlinedButton(
                            onClick = {
                                SoundSynthesizer.stop()
                                onDelete(alarm.id)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Alarm")
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    if (showSunrisePreview) {
        SunrisePreviewDialog(
            rampMinutes = sunriseRampMinutes,
            startBrightness = sunriseStartBrightness,
            endBrightness = sunriseEndBrightness,
            startKelvin = sunriseStartColorTemp,
            endKelvin = sunriseEndColorTemp,
            onDismiss = { showSunrisePreview = false }
        )
    }

    if (showVibrationEditor) {
        VibrationEditorDialog(
            initialPatternName = vibrationPattern,
            initialCustomPattern = customVibePattern,
            onSave = { patternName, customPattern ->
                vibrationPattern = patternName
                customVibePattern = customPattern
            },
            onDismiss = { showVibrationEditor = false }
        )
    }
}
