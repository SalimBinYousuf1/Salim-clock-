package com.example.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CoralPrimary
import com.example.vibration.VibrationManager

@Composable
fun VibrationEditorDialog(
    initialPatternName: String,
    initialCustomPattern: String,
    onSave: (patternName: String, customPattern: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedPattern by remember { mutableStateOf(initialPatternName) }
    var customPatternStr by remember { mutableStateOf(initialCustomPattern) }

    // Tap recorder state
    var isRecording by remember { mutableStateOf(false) }
    val recordedDurations = remember { mutableStateListOf<Long>() }
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13131E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = CoralPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vibration Pattern Editor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets list
                Text("Select Built-in Pattern", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val presets = listOf("Standard", "SOS", "Heartbeat", "Escalating", "Rapid Staccato", "Custom")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEach { name ->
                        val isSelected = selectedPattern == name
                        Surface(
                            onClick = {
                                selectedPattern = name
                                VibrationManager.vibrate(context, name, customPatternStr)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CoralPrimary.copy(alpha = 0.2f) else Color(0xFF1B1B2A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) CoralPrimary else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tap Recorder for Custom Pattern
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A28)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Interactive Tap Recorder",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRecording) "Tap the rhythm button to record pulses!" else "Tap 'Start Recording' then rhythmically tap the pad.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tap Pad
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) CoralPrimary else Color(0xFF2E2E44))
                                .clickable(enabled = isRecording) {
                                    val now = System.currentTimeMillis()
                                    if (lastTapTimestamp != 0L) {
                                        val delta = (now - lastTapTimestamp).coerceIn(50L, 1500L)
                                        recordedDurations.add(delta)
                                        customPatternStr = recordedDurations.joinToString(",")
                                        VibrationManager.vibrate(context, "Custom", customPatternStr)
                                    } else {
                                        recordedDurations.add(200L)
                                    }
                                    lastTapTimestamp = now
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = "Tap",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual timeline blocks of recorded pulses
                        if (recordedDurations.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(recordedDurations) { ms ->
                                    val blockWidth = (ms / 10).coerceIn(12L, 60L).toInt().dp
                                    Box(
                                        modifier = Modifier
                                            .height(18.dp)
                                            .width(blockWidth)
                                            .background(CoralPrimary, RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                            Text("${recordedDurations.size} pulses recorded ($customPatternStr ms)", color = Color.Gray, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (!isRecording) {
                                        recordedDurations.clear()
                                        lastTapTimestamp = 0L
                                        isRecording = true
                                    } else {
                                        isRecording = false
                                        selectedPattern = "Custom"
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isRecording) "Finish Recording" else "Start Recording", fontSize = 12.sp)
                            }

                            if (recordedDurations.isNotEmpty()) {
                                IconButton(onClick = {
                                    recordedDurations.clear()
                                    customPatternStr = "200,200,400,200"
                                }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSave(selectedPattern, customPatternStr)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Pattern", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
