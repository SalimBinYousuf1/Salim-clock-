package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.TimerPresetEntity
import com.example.sound.SoundSynthesizer
import com.example.timer.ActiveTimer
import com.example.timer.IntervalStage
import com.example.timer.PomodoroStage
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TabularDisplayLarge
import com.example.ui.theme.TabularDisplayMedium
import com.example.ui.theme.TabularHeadline
import com.example.ui.theme.TabularTitle
import com.example.ui.theme.TimerGradient
import com.example.ui.theme.VioletSecondary
import com.example.vibration.VibrationManager
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    presets: List<TimerPresetEntity>,
    onSavePreset: (TimerPresetEntity) -> Unit,
    onDeletePreset: (TimerPresetEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Standard & Multi, 1: Pomodoro, 2: Interval

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0C192E), // Diffuse cyan-to-violet accent background
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

            // Screen Header
            Text(
                text = "Timers",
                style = TabularHeadline,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Multi-timer orchestration, Pomodoro & interval cycles",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF131320),
                contentColor = CyanPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanPrimary
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Standard", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Pomodoro", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Interval HIIT", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> StandardTimerSection(
                    presets = presets,
                    onSavePreset = onSavePreset,
                    onDeletePreset = onDeletePreset
                )
                1 -> PomodoroSection()
                2 -> IntervalTimerSection()
            }
        }
    }
}

@Composable
fun StandardTimerSection(
    presets: List<TimerPresetEntity>,
    onSavePreset: (TimerPresetEntity) -> Unit,
    onDeletePreset: (TimerPresetEntity) -> Unit
) {
    val context = LocalContext.current
    var inputMinutes by remember { mutableIntStateOf(5) }
    var inputSeconds by remember { mutableIntStateOf(0) }
    var showAddPresetDialog by remember { mutableStateOf(false) }

    val activeTimers = remember {
        mutableStateListOf(
            ActiveTimer(name = "Main Countdown", totalSeconds = 300, remainingSeconds = 300, isRunning = false)
        )
    }

    // Ticker coroutine for all running timers
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            activeTimers.forEachIndexed { index, timer ->
                if (timer.isRunning && timer.remainingSeconds > 0) {
                    val updatedSecs = timer.remainingSeconds - 1
                    activeTimers[index] = timer.copy(remainingSeconds = updatedSecs)
                    if (updatedSecs == 0L) {
                        // Timer completed!
                        activeTimers[index] = timer.copy(remainingSeconds = 0, isRunning = false)
                        SoundSynthesizer.playSound("Digital Marimba", isLooping = false)
                        VibrationManager.vibrate(context, "Standard")
                    }
                }
            }
        }
    }

    val primaryTimer = activeTimers.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Circular Progress Ring for the Primary Timer
        if (primaryTimer != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = primaryTimer.name.uppercase(),
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.size(210.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SalimProgressRing(
                                progress = primaryTimer.progress,
                                colors = TimerGradient,
                                strokeWidth = 14.dp,
                                faceStyle = ClockFaceStyle.RingLike,
                                accessibilityText = "Timer remaining: ${primaryTimer.formattedRemaining}",
                                innerContent = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = primaryTimer.formattedRemaining,
                                            style = TabularDisplayLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (primaryTimer.isRunning) "Running" else if (primaryTimer.remainingSeconds == 0L) "Complete!" else "Paused",
                                            color = if (primaryTimer.remainingSeconds == 0L) EmeraldPrimary else Color.Gray,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Controls: +1m, Play/Pause, Reset
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    activeTimers[0] = primaryTimer.copy(
                                        totalSeconds = primaryTimer.totalSeconds + 60,
                                        remainingSeconds = primaryTimer.remainingSeconds + 60
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+1 min", color = CyanPrimary, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (primaryTimer.remainingSeconds == 0L) {
                                        activeTimers[0] = primaryTimer.copy(remainingSeconds = primaryTimer.totalSeconds, isRunning = true)
                                    } else {
                                        activeTimers[0] = primaryTimer.copy(isRunning = !primaryTimer.isRunning)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = CircleShape,
                                modifier = Modifier.size(60.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (primaryTimer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (primaryTimer.isRunning) "Pause" else "Start",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    activeTimers[0] = primaryTimer.copy(
                                        remainingSeconds = primaryTimer.totalSeconds,
                                        isRunning = false
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }

        // Quick-Start Duration Presets
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick-Start Presets", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    IconButton(onClick = { showAddPresetDialog = true }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Custom Preset", tint = CyanPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val quickDurations = listOf(
                    Pair("1m", 60L),
                    Pair("3m", 180L),
                    Pair("5m", 300L),
                    Pair("10m", 600L),
                    Pair("15m", 900L),
                    Pair("25m", 1500L),
                    Pair("45m", 2700L),
                    Pair("1h", 3600L)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickDurations) { pair ->
                        Surface(
                            onClick = {
                                activeTimers[0] = ActiveTimer(
                                    name = "${pair.first} Timer",
                                    totalSeconds = pair.second,
                                    remainingSeconds = pair.second,
                                    isRunning = true
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1E2E)
                        ) {
                            Text(
                                text = pair.first,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Saved Custom Presets from Room DB
        if (presets.isNotEmpty()) {
            item {
                Text("Saved Presets (${presets.size})", color = Color.LightGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presets) { p ->
                        Surface(
                            onClick = {
                                activeTimers.add(
                                    ActiveTimer(
                                        name = p.name,
                                        totalSeconds = p.durationSeconds,
                                        remainingSeconds = p.durationSeconds,
                                        isRunning = true,
                                        colorHex = p.colorHex
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF171728)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(p.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${p.durationSeconds / 60}m", color = CyanPrimary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onDeletePreset(p) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Multi-Timer: Simultaneous Active Running Timers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Simultaneous Timers (${activeTimers.size})", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Button(
                    onClick = {
                        val count = activeTimers.size + 1
                        activeTimers.add(
                            ActiveTimer(
                                name = "Timer $count",
                                totalSeconds = 180,
                                remainingSeconds = 180,
                                isRunning = true
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF202034)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Running Timer", color = CyanPrimary, fontSize = 11.sp)
                }
            }
        }

        items(activeTimers.drop(1)) { timer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161626)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(timer.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(timer.formattedRemaining, style = TabularDisplayMedium, color = CyanPrimary, fontSize = 22.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val idx = activeTimers.indexOf(timer)
                            if (idx != -1) {
                                activeTimers[idx] = timer.copy(isRunning = !timer.isRunning)
                            }
                        }) {
                            Icon(
                                if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = CyanPrimary
                            )
                        }

                        IconButton(onClick = { activeTimers.remove(timer) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    if (showAddPresetDialog) {
        var presetName by remember { mutableStateOf("") }
        var presetMinutes by remember { mutableIntStateOf(10) }

        Dialog(onDismissRequest = { showAddPresetDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161626)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Save Custom Timer Preset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset Name (e.g. Pasta, Nap)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Duration: $presetMinutes minutes", color = Color.LightGray, fontSize = 13.sp)
                    Slider(
                        value = presetMinutes.toFloat(),
                        onValueChange = { presetMinutes = it.toInt() },
                        valueRange = 1f..120f,
                        colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddPresetDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (presetName.isNotBlank()) {
                                    onSavePreset(
                                        TimerPresetEntity(
                                            name = presetName,
                                            durationSeconds = (presetMinutes * 60).toLong(),
                                            category = "Custom"
                                        )
                                    )
                                    showAddPresetDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroSection() {
    val context = LocalContext.current
    var currentStage by remember { mutableStateOf(PomodoroStage.WORK) }
    var currentRound by remember { mutableIntStateOf(1) }
    val totalRounds = 4
    var autoProgress by remember { mutableStateOf(true) }

    var remainingSeconds by remember { mutableIntStateOf(PomodoroStage.WORK.defaultMinutes * 60) }
    var totalSeconds by remember { mutableIntStateOf(PomodoroStage.WORK.defaultMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }

    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
            if (remainingSeconds == 0) {
                // Stage complete
                SoundSynthesizer.playSound("Zen Bell", isLooping = false)
                VibrationManager.vibrate(context, "SOS")

                if (autoProgress) {
                    when (currentStage) {
                        PomodoroStage.WORK -> {
                            if (currentRound < totalRounds) {
                                currentStage = PomodoroStage.SHORT_BREAK
                                totalSeconds = PomodoroStage.SHORT_BREAK.defaultMinutes * 60
                                remainingSeconds = totalSeconds
                            } else {
                                currentStage = PomodoroStage.LONG_BREAK
                                totalSeconds = PomodoroStage.LONG_BREAK.defaultMinutes * 60
                                remainingSeconds = totalSeconds
                            }
                        }
                        PomodoroStage.SHORT_BREAK -> {
                            currentRound++
                            currentStage = PomodoroStage.WORK
                            totalSeconds = PomodoroStage.WORK.defaultMinutes * 60
                            remainingSeconds = totalSeconds
                        }
                        PomodoroStage.LONG_BREAK -> {
                            currentRound = 1
                            currentStage = PomodoroStage.WORK
                            totalSeconds = PomodoroStage.WORK.defaultMinutes * 60
                            remainingSeconds = totalSeconds
                        }
                    }
                } else {
                    isRunning = false
                }
            }
        }
    }

    val stageColors = when (currentStage) {
        PomodoroStage.WORK -> listOf(Color(0xFFFF5E62), Color(0xFFFF9966))
        PomodoroStage.SHORT_BREAK -> listOf(Color(0xFF00E5FF), Color(0xFF00F5A0))
        PomodoroStage.LONG_BREAK -> listOf(Color(0xFF7C4DFF), Color(0xFF6366F1))
    }

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formatted = String.format("%02d:%02d", mins, secs)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "POMODORO FOCUS SESSION",
                        color = stageColors.first(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${currentStage.displayName} • Round $currentRound of $totalRounds",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SalimProgressRing(
                            progress = progress,
                            colors = stageColors,
                            strokeWidth = 14.dp,
                            faceStyle = ClockFaceStyle.RingLike,
                            accessibilityText = "Pomodoro remaining: $formatted",
                            innerContent = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatted,
                                        style = TabularDisplayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = currentStage.name,
                                        color = stageColors.first(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                totalSeconds = currentStage.defaultMinutes * 60
                                remainingSeconds = totalSeconds
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset", color = Color.LightGray)
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            colors = ButtonDefaults.buttonColors(containerColor = stageColors.first()),
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                // Skip to next stage
                                isRunning = false
                                if (currentStage == PomodoroStage.WORK) {
                                    currentStage = PomodoroStage.SHORT_BREAK
                                    totalSeconds = 5 * 60
                                } else {
                                    currentStage = PomodoroStage.WORK
                                    totalSeconds = 25 * 60
                                }
                                remainingSeconds = totalSeconds
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Skip", color = stageColors.first())
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto Progression Between Stages", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Automatically starts breaks & resumes work", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoProgress,
                        onCheckedChange = { autoProgress = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun IntervalTimerSection() {
    val context = LocalContext.current
    val stages = remember {
        listOf(
            IntervalStage("Warm Up", 60, 0xFFFFD166),
            IntervalStage("High Intensity Work", 45, 0xFFFF5E62),
            IntervalStage("Rest Recovery", 20, 0xFF00E5FF),
            IntervalStage("Cool Down", 60, 0xFF00F5A0)
        )
    }

    var currentStageIndex by remember { mutableIntStateOf(0) }
    var currentCycle by remember { mutableIntStateOf(1) }
    val totalCycles = 6

    var stageRemaining by remember { mutableIntStateOf(stages[0].durationSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    val activeStage = stages[currentStageIndex]
    val progress = (stageRemaining.toFloat() / activeStage.durationSeconds.toFloat()).coerceIn(0f, 1f)

    LaunchedEffect(isRunning) {
        while (isRunning && stageRemaining > 0) {
            delay(1000)
            stageRemaining--
            if (stageRemaining == 0) {
                // Next stage transition
                SoundSynthesizer.playSound("Digital Marimba", isLooping = false)
                VibrationManager.vibrate(context, "Rapid Staccato")

                if (currentStageIndex < stages.size - 1) {
                    currentStageIndex++
                    stageRemaining = stages[currentStageIndex].durationSeconds
                } else {
                    // Loop or finish
                    if (currentCycle < totalCycles) {
                        currentCycle++
                        currentStageIndex = 1 // Loop back to work
                        stageRemaining = stages[1].durationSeconds
                    } else {
                        isRunning = false
                    }
                }
            }
        }
    }

    val stageColor = Color(activeStage.colorHex)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131320)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INTERVAL HIIT TRAINING",
                        color = stageColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cycle $currentCycle of $totalCycles • Stage ${currentStageIndex + 1}/${stages.size}",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier.size(210.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SalimProgressRing(
                            progress = progress,
                            colors = listOf(stageColor, stageColor.copy(alpha = 0.7f)),
                            strokeWidth = 14.dp,
                            faceStyle = ClockFaceStyle.RingLike,
                            accessibilityText = "Interval remaining: $stageRemaining seconds",
                            innerContent = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$stageRemaining",
                                        style = TabularDisplayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = activeStage.name,
                                        color = stageColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                currentStageIndex = 0
                                stageRemaining = stages[0].durationSeconds
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset", color = Color.LightGray)
                        }

                        Button(
                            onClick = { isRunning = !isRunning },
                            colors = ButtonDefaults.buttonColors(containerColor = stageColor),
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(
                                if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // Stage sequence breakdown
        item {
            Text("Stage Flow", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        items(stages) { stage ->
            val isCurrent = stage == activeStage
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(stage.colorHex).copy(alpha = 0.2f) else Color(0xFF13131F)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(stage.colorHex))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(stage.name, color = Color.White, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                    }
                    Text("${stage.durationSeconds}s", color = Color.LightGray, fontSize = 13.sp)
                }
            }
        }
    }
}
