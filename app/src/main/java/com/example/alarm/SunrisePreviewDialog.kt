package com.example.alarm

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CoralPrimary
import kotlinx.coroutines.delay

@Composable
fun SunrisePreviewDialog(
    rampMinutes: Int,
    startBrightness: Float,
    endBrightness: Float,
    startKelvin: Int,
    endKelvin: Int,
    onDismiss: () -> Unit
) {
    var previewProgress by remember { mutableFloatStateOf(0f) }
    var isSimulating by remember { mutableStateOf(false) }

    // Fast-forward simulation: 10 seconds simulates full ramp
    LaunchedEffect(isSimulating) {
        if (isSimulating) {
            val totalSteps = 100
            while (previewProgress < 1f && isSimulating) {
                delay(100)
                previewProgress += (1f / totalSteps)
            }
            if (previewProgress >= 1f) {
                isSimulating = false
            }
        }
    }

    // Color calculation from Kelvin and brightness
    val simulatedColor = remember(previewProgress, startBrightness, endBrightness) {
        val currentBrightness = startBrightness + (endBrightness - startBrightness) * previewProgress
        when {
            previewProgress < 0.25f -> Color(0xFF1E0A08).copy(alpha = currentBrightness.coerceIn(0.2f, 1f))
            previewProgress < 0.6f -> Color(0xFF8A3014).copy(alpha = currentBrightness.coerceIn(0.4f, 1f))
            previewProgress < 0.85f -> Color(0xFFFF7A33).copy(alpha = currentBrightness.coerceIn(0.6f, 1f))
            else -> Color(0xFFFFD166).copy(alpha = currentBrightness.coerceIn(0.8f, 1f))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                simulatedColor,
                                Color(0xFF0A0A0F)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sunrise Ramp Preview",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Simulation Center Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Simulating ${rampMinutes}-minute ramp",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val currentMinute = (previewProgress * rampMinutes).toInt()
                        Text(
                            text = "T + $currentMinute min",
                            color = Color(0xFFFFD166),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(previewProgress * 100).toInt()}% Illumination",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }

                    // Bottom scrub & control panel
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC161622)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Scrub Ramp Timeline", color = Color.White, fontSize = 13.sp)
                                Text("${(startKelvin + (endKelvin - startKelvin) * previewProgress).toInt()}K", color = Color(0xFFFFD166), fontSize = 13.sp)
                            }

                            Slider(
                                value = previewProgress,
                                onValueChange = {
                                    previewProgress = it
                                    isSimulating = false
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFFD166),
                                    activeTrackColor = Color(0xFFFF8A50),
                                    inactiveTrackColor = Color.DarkGray
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (previewProgress >= 1f) previewProgress = 0f
                                        isSimulating = !isSimulating
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(if (isSimulating) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isSimulating) "Pause Simulation" else "Auto Play")
                                }

                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A3C)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Done Preview")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
