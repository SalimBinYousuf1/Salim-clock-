package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.alarm.AlarmAlertActivity
import com.example.alarm.AlarmEditSheet
import com.example.alarm.AlarmScheduler
import com.example.data.local.entity.AlarmEntity
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.AlarmGradient
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.TabularDisplayMedium
import com.example.ui.theme.TabularHeadline
import com.example.ui.theme.TabularTitle
import kotlinx.coroutines.delay

@Composable
fun AlarmScreen(
    alarms: List<AlarmEntity>,
    onToggleAlarm: (AlarmEntity, Boolean) -> Unit,
    onSaveAlarm: (AlarmEntity) -> Unit,
    onDeleteAlarm: (Long) -> Unit
) {
    val context = LocalContext.current
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    // Live frame ticker for next alarm ring
    var currentClockTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentClockTime = System.currentTimeMillis()
        }
    }

    // Determine earliest active alarm
    val nextAlarm = remember(alarms, currentClockTime) {
        alarms.filter { it.isEnabled }
            .minByOrNull { AlarmScheduler.calculateNextTriggerMillis(it) }
    }

    val nextAlarmTrigger = nextAlarm?.let { AlarmScheduler.calculateNextTriggerMillis(it) } ?: 0L
    val remainingMillis = (nextAlarmTrigger - currentClockTime).coerceAtLeast(0L)
    // 24-hr fractional progress for ring
    val ringProgress = if (nextAlarm != null) {
        val totalDayMillis = 24 * 3600 * 1000L
        (1.0f - (remainingMillis.toFloat() / totalDayMillis.toFloat())).coerceIn(0.05f, 1.0f)
    } else {
        0.05f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2A1015), // Diffuse coral-to-amber background
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
                Spacer(modifier = Modifier.height(12.dp))
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Alarms",
                            style = TabularHeadline,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Escalating failsafes & sunrise illumination",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    // Test trigger button
                    OutlinedButton(
                        onClick = {
                            val targetId = nextAlarm?.id ?: alarms.firstOrNull()?.id ?: 1L
                            val intent = Intent(context, AlarmAlertActivity::class.java).apply {
                                putExtra(AlarmScheduler.EXTRA_ALARM_ID, targetId)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Alert", color = AmberSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Live Circular Progress Ring: Next Alarm
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NEXT ALARM",
                                color = AmberSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (nextAlarm != null) {
                                val remainingText = AlarmScheduler.formatRemainingTime(nextAlarmTrigger)
                                Text(
                                    text = remainingText,
                                    color = Color.White,
                                    style = TabularTitle,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format("%02d:%02d • %s", nextAlarm.hour, nextAlarm.minute, nextAlarm.label),
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                            } else {
                                Text(
                                    text = "No active alarms",
                                    color = Color.White,
                                    style = TabularTitle
                                )
                                Text(
                                    text = "Enable an alarm to schedule",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Circular Progress Ring
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val activeFace = nextAlarm?.let {
                                try { ClockFaceStyle.valueOf(it.clockFaceStyle) } catch (_: Exception) { ClockFaceStyle.RingLike }
                            } ?: ClockFaceStyle.RingLike

                            SalimProgressRing(
                                progress = ringProgress,
                                colors = AlarmGradient,
                                strokeWidth = 10.dp,
                                faceStyle = activeFace,
                                accessibilityText = "Next alarm countdown: ${if (nextAlarm != null) AlarmScheduler.formatRemainingTime(nextAlarmTrigger) else "No alarm"}",
                                innerContent = {
                                    Icon(
                                        Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Alarms List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scheduled (${alarms.size})",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            // Alarms Cards
            items(alarms, key = { it.id }) { alarmItem ->
                AlarmItemCard(
                    alarm = alarmItem,
                    onToggle = { isChecked ->
                        onToggleAlarm(alarmItem, isChecked)
                    },
                    onClick = {
                        editingAlarm = alarmItem
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add Alarm
        FloatingActionButton(
            onClick = { isCreatingNew = true },
            containerColor = CoralPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_alarm_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alarm", modifier = Modifier.size(28.dp))
        }
    }

    // Edit sheet
    if (editingAlarm != null || isCreatingNew) {
        AlarmEditSheet(
            initialAlarm = editingAlarm,
            onSave = { saved ->
                onSaveAlarm(saved)
                editingAlarm = null
                isCreatingNew = false
            },
            onDelete = { id ->
                onDeleteAlarm(id)
                editingAlarm = null
                isCreatingNew = false
            },
            onDismiss = {
                editingAlarm = null
                isCreatingNew = false
            }
        )
    }
}

@Composable
fun AlarmItemCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val amPm = if (alarm.hour >= 12) "PM" else "AM"
    val displayHour = when {
        alarm.hour == 0 -> 12
        alarm.hour > 12 -> alarm.hour - 12
        else -> alarm.hour
    }
    val timeFormatted = String.format("%02d:%02d", displayHour, alarm.minute)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("alarm_card_${alarm.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) Color(0xFF161624) else Color(0xFF111118)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time & Label
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeFormatted,
                            style = TabularDisplayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) Color.White else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = amPm,
                            color = if (alarm.isEnabled) CoralPrimary else Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = alarm.label,
                        color = if (alarm.isEnabled) Color.LightGray else Color.DarkGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Switch
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CoralPrimary,
                        checkedTrackColor = CoralPrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF222230)
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feature Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeat days
                val repeatLabel = if (!alarm.isRecurring) "Once" else if (alarm.repeatDays == 0x7F) "Everyday" else if (alarm.repeatDays == 0x3E) "Mon-Fri" else "Custom days"
                BadgePill(text = repeatLabel, isHighlight = alarm.isEnabled)

                // Face Style Badge
                BadgePill(text = alarm.clockFaceStyle, isHighlight = false)

                // Dismissal Challenge
                if (alarm.dismissChallenge != "None") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF241620), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = when (alarm.dismissChallenge) {
                                "Math" -> Icons.Default.Calculate
                                "Shake" -> Icons.Default.Vibration
                                "QrCode" -> Icons.Default.QrCode
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(alarm.dismissChallenge, color = CoralPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Sunrise badge
                if (alarm.sunriseEnabled) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = "Sunrise ramp",
                        tint = AmberSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgePill(text: String, isHighlight: Boolean) {
    Surface(
        color = if (isHighlight) Color(0xFF222234) else Color(0xFF191924),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = if (isHighlight) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
