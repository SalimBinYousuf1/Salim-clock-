package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.SystemClock
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StopwatchGradient
import com.example.ui.theme.TabularDisplayLarge
import com.example.ui.theme.TabularHeadline
import com.example.ui.theme.TealSecondary
import java.io.File

data class StopwatchLap(
    val lapIndex: Int,
    val lapDeltaMillis: Long,
    val totalSplitMillis: Long
)

@Composable
fun StopwatchScreen() {
    val context = LocalContext.current

    var isRunning by remember { mutableStateOf(false) }
    var baseStartTime by remember { mutableLongStateOf(0L) }
    var accumulatedTime by remember { mutableLongStateOf(0L) }
    var currentElapsedMillis by remember { mutableLongStateOf(0L) }

    val laps = remember { mutableStateListOf<StopwatchLap>() }
    var lastLapTotalMillis by remember { mutableLongStateOf(0L) }

    // High-precision frame callback for millisecond updates
    LaunchedEffect(isRunning) {
        if (isRunning) {
            baseStartTime = SystemClock.elapsedRealtime()
            while (isRunning) {
                withFrameMillis {
                    val now = SystemClock.elapsedRealtime()
                    currentElapsedMillis = accumulatedTime + (now - baseStartTime)
                }
            }
        }
    }

    // Identify fastest and slowest laps (only if 2 or more laps exist)
    val fastestLapTime = remember(laps.size) {
        if (laps.size >= 2) laps.minOfOrNull { it.lapDeltaMillis } else null
    }
    val slowestLapTime = remember(laps.size) {
        if (laps.size >= 2) laps.maxOfOrNull { it.lapDeltaMillis } else null
    }

    // Format current time: MM:SS.cc
    val minutes = (currentElapsedMillis / 60000)
    val seconds = (currentElapsedMillis % 60000) / 1000
    val centis = (currentElapsedMillis % 1000) / 10
    val timeFormatted = String.format("%02d:%02d.%02d", minutes, seconds, centis)

    // Second-hand progress (0.0 to 1.0 within 60 seconds)
    val secondHandProgress = ((currentElapsedMillis % 60000) / 60000f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF07241A), // Diffuse emerald-to-teal background
                        Color(0xFF0A0A0F),
                        Color(0xFF0A0A0F)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stopwatch",
                        style = TabularHeadline,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Millisecond precision & true delta lap split",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                if (laps.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            exportLapsCsv(context, laps)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = EmeraldPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Visual Motif Ring
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A1E)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SalimProgressRing(
                            progress = secondHandProgress,
                            colors = StopwatchGradient,
                            strokeWidth = 14.dp,
                            faceStyle = ClockFaceStyle.RingLike,
                            accessibilityText = "Stopwatch elapsed: $timeFormatted",
                            innerContent = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = timeFormatted,
                                        style = TabularDisplayLarge,
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isRunning) "Timing" else if (currentElapsedMillis > 0) "Paused" else "Ready",
                                        color = if (isRunning) EmeraldPrimary else Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons: Reset / Lap / Start-Pause
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                accumulatedTime = 0L
                                currentElapsedMillis = 0L
                                lastLapTotalMillis = 0L
                                laps.clear()
                            },
                            enabled = currentElapsedMillis > 0 && !isRunning,
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.LightGray)
                        }

                        // Play / Pause Button
                        Button(
                            onClick = {
                                if (isRunning) {
                                    accumulatedTime = currentElapsedMillis
                                    isRunning = false
                                } else {
                                    isRunning = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) CoralPrimary else EmeraldPrimary
                            ),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("stopwatch_toggle_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Start",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Lap Button
                        Button(
                            onClick = {
                                if (isRunning) {
                                    val lapDelta = currentElapsedMillis - lastLapTotalMillis
                                    val newLap = StopwatchLap(
                                        lapIndex = laps.size + 1,
                                        lapDeltaMillis = lapDelta,
                                        totalSplitMillis = currentElapsedMillis
                                    )
                                    laps.add(0, newLap)
                                    lastLapTotalMillis = currentElapsedMillis
                                }
                            },
                            enabled = isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2E2A)),
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = "Lap", tint = EmeraldPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lap List
            if (laps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lap", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Lap Delta (Δ)", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Overall Split", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(laps) { _, lap ->
                        val isFastest = fastestLapTime != null && lap.lapDeltaMillis == fastestLapTime
                        val isSlowest = slowestLapTime != null && lap.lapDeltaMillis == slowestLapTime

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isFastest -> Color(0xFF0F3024)
                                    isSlowest -> Color(0xFF30161A)
                                    else -> Color(0xFF141A1E)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format("#%02d", lap.lapIndex),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isFastest) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = EmeraldPrimary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "FASTEST",
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (isSlowest) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = CoralPrimary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "SLOWEST",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = formatLapTime(lap.lapDeltaMillis),
                                    color = if (isFastest) EmeraldPrimary else if (isSlowest) CoralPrimary else Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = formatLapTime(lap.totalSplitMillis),
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap the Flag button during timing to record split laps",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

fun formatLapTime(millis: Long): String {
    val m = millis / 60000
    val s = (millis % 60000) / 1000
    val c = (millis % 1000) / 10
    return String.format("%02d:%02d.%02d", m, s, c)
}

fun exportLapsCsv(context: Context, laps: List<StopwatchLap>) {
    try {
        val csvHeader = "Lap,Delta_Time,Split_Time\n"
        val csvBody = laps.sortedBy { it.lapIndex }.joinToString("\n") {
            "${it.lapIndex},${formatLapTime(it.lapDeltaMillis)},${formatLapTime(it.totalSplitMillis)}"
        }
        val file = File(context.cacheDir, "salim_stopwatch_laps.csv")
        file.writeText(csvHeader + csvBody)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Lap Times (CSV)"))
    } catch (_: Exception) {
        // Fallback simple text share if file provider not declared
        val plainText = laps.sortedBy { it.lapIndex }.joinToString("\n") {
            "Lap ${it.lapIndex}: Δ ${formatLapTime(it.lapDeltaMillis)} (Split: ${formatLapTime(it.totalSplitMillis)})"
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Salim Clock Laps:\n$plainText")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Lap Times"))
    }
}
