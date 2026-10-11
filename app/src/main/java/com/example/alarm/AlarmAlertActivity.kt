package com.example.alarm

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SalimClockApplication
import com.example.data.local.entity.AlarmEntity
import com.example.sound.SoundSynthesizer
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.AlarmGradient
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TabularDisplayLarge
import com.example.vibration.VibrationManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

class AlarmAlertActivity : ComponentActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime: Long = 0
    private var onShakeDetected: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Wake screen and turn on even over keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                AlarmAlertScreen(
                    alarmId = alarmId,
                    onRegisterShake = { callback ->
                        onShakeDetected = callback
                    },
                    onDismiss = {
                        stopAlerts()
                        finish()
                    },
                    onSnooze = {
                        stopAlerts()
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlerts()
    }

    private fun stopAlerts() {
        SoundSynthesizer.stop()
        VibrationManager.stop(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gForce = sqrt((x * x + y * y + z * z).toDouble()) / SensorManager.GRAVITY_EARTH

            val now = System.currentTimeMillis()
            if (gForce > 2.2 && (now - lastShakeTime > 250)) {
                lastShakeTime = now
                onShakeDetected?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun AlarmAlertScreen(
    alarmId: Long,
    onRegisterShake: ((() -> Unit) -> Unit),
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SalimClockApplication

    var alarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var currentVolume by remember { mutableFloatStateOf(0.1f) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isEscalated by remember { mutableStateOf(false) }

    // Load alarm from DB
    LaunchedEffect(alarmId) {
        val loaded = app.repository.getAlarmById(alarmId)
        alarm = loaded ?: AlarmEntity(
            hour = 7,
            minute = 0,
            label = "Alarm",
            dismissChallenge = "Math"
        )
    }

    val activeAlarm = alarm ?: return

    // Volume ramp-up & escalation sequence loop
    LaunchedEffect(activeAlarm) {
        SoundSynthesizer.playSound(activeAlarm.soundName, isLooping = true, volume = 0.2f)
        VibrationManager.vibrate(context, activeAlarm.vibrationPattern, activeAlarm.customVibePattern, repeatIndex = 0)

        val rampMinutes = activeAlarm.volumeRampMinutes.coerceAtLeast(1)
        val rampSeconds = rampMinutes * 60

        while (true) {
            delay(1000)
            elapsedSeconds++

            // Volume ramp
            if (elapsedSeconds <= rampSeconds) {
                currentVolume = 0.2f + (0.8f * (elapsedSeconds.toFloat() / rampSeconds))
                SoundSynthesizer.setVolume(currentVolume)
            }

            // Escalation sequence check
            if (activeAlarm.escalationEnabled &&
                elapsedSeconds >= activeAlarm.escalationIntervalMinutes * 60 &&
                !isEscalated
            ) {
                isEscalated = true
                SoundSynthesizer.playSound(activeAlarm.escalationSecondarySound, isLooping = true, volume = 1.0f)
            }
        }
    }

    // Sunrise background animation
    val sunriseColor by animateColorAsState(
        targetValue = if (activeAlarm.sunriseEnabled) {
            val progress = (elapsedSeconds.toFloat() / (activeAlarm.sunriseRampMinutes * 60f)).coerceIn(0f, 1f)
            when {
                progress < 0.3f -> Color(0xFF2B0A12)
                progress < 0.7f -> Color(0xFF6B2D18)
                else -> Color(0xFFFF8A50).copy(alpha = 0.85f)
            }
        } else {
            Color(0xFF0A0A0F)
        },
        animationSpec = tween(durationMillis = 2000),
        label = "sunrise_bg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        sunriseColor,
                        Color(0xFF0A0A0F)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Clock & Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                Text(
                    text = timeFormat.format(Date()),
                    style = TabularDisplayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeAlarm.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = CoralPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                if (isEscalated) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CoralPrimary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Escalation Sequence Active: Full Volume", color = CoralPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Challenge Interactive Zone
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (activeAlarm.dismissChallenge) {
                        "Math" -> {
                            MathDismissalChallenge(
                                difficulty = activeAlarm.mathDifficulty,
                                onSolved = onDismiss
                            )
                        }
                        "Shake" -> {
                            ShakeDismissalChallenge(
                                targetShakes = activeAlarm.shakeTargetCount,
                                onRegisterShake = onRegisterShake,
                                onCompleted = onDismiss
                            )
                        }
                        "QrCode" -> {
                            QrCodeDismissalChallenge(
                                targetCode = activeAlarm.qrTargetCode,
                                onScanned = onDismiss
                            )
                        }
                        "Phrase" -> {
                            PhraseDismissalChallenge(
                                targetPhrase = activeAlarm.phraseText,
                                onMatched = onDismiss
                            )
                        }
                        else -> {
                            // Simple button dismiss
                            StandardDismissal(onDismiss = onDismiss)
                        }
                    }
                }
            }

            // Snooze Bar
            val maxSnooze = activeAlarm.maxSnoozeCount
            val currentSnooze = activeAlarm.currentSnoozeCount
            val canSnooze = currentSnooze < maxSnooze

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                if (canSnooze) {
                    val snoozeMins = if (activeAlarm.escalatingSnooze) {
                        activeAlarm.snoozeDurationMinutes * (currentSnooze + 1)
                    } else {
                        activeAlarm.snoozeDurationMinutes
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Snoozed for $snoozeMins minutes", Toast.LENGTH_SHORT).show()
                            onSnooze()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(
                            text = "Snooze ($snoozeMins min) • ${maxSnooze - currentSnooze} left",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "Snooze limit reached. Dismiss challenge required.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StandardDismissal(onDismiss: () -> Unit) {
    Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(Icons.Default.Alarm, contentDescription = null)
        Spacer(modifier = Modifier.size(8.dp))
        Text("Dismiss Alarm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MathDismissalChallenge(
    difficulty: String,
    onSolved: () -> Unit
) {
    val mathProblem = remember(difficulty) {
        when (difficulty) {
            "Easy" -> {
                val a = Random.nextInt(12, 45)
                val b = Random.nextInt(15, 50)
                Pair("$a + $b", (a + b).toString())
            }
            "Hard" -> {
                val a = Random.nextInt(12, 25)
                val b = Random.nextInt(6, 14)
                val c = Random.nextInt(10, 40)
                Pair("($a × $b) - $c", ((a * b) - c).toString())
            }
            else -> { // Medium
                val a = Random.nextInt(7, 18)
                val b = Random.nextInt(6, 12)
                val c = Random.nextInt(15, 30)
                Pair("($a × $b) + $c", ((a * b) + c).toString())
            }
        }
    }

    var userInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Math Dismissal Challenge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Solve the equation to turn off the alarm", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${mathProblem.first} = ?",
            color = CoralPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userInput,
            onValueChange = {
                userInput = it
                isError = false
            },
            placeholder = { Text("Enter answer") },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CoralPrimary,
                unfocusedBorderColor = Color.DarkGray
            )
        )

        if (isError) {
            Text("Incorrect answer, try again!", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (userInput.trim() == mathProblem.second) {
                    onSolved()
                } else {
                    isError = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Verify & Dismiss", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShakeDismissalChallenge(
    targetShakes: Int,
    onRegisterShake: ((() -> Unit) -> Unit),
    onCompleted: () -> Unit
) {
    var shakesDone by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onRegisterShake {
            shakesDone++
            if (shakesDone >= targetShakes) {
                onCompleted()
            }
        }
        onDispose {}
    }

    val progress = (shakesDone.toFloat() / targetShakes.toFloat()).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Shake Challenge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Vigorously shake your phone to wake up!", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            SalimProgressRing(
                progress = progress,
                colors = AlarmGradient,
                faceStyle = ClockFaceStyle.RingLike,
                strokeWidth = 12.dp,
                innerContent = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(32.dp))
                        Text("${targetShakes - shakesDone}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("left", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("$shakesDone of $targetShakes shakes registered", color = Color.LightGray, fontSize = 13.sp)
    }
}

@Composable
fun QrCodeDismissalChallenge(
    targetCode: String,
    onScanned: () -> Unit
) {
    var simulatedScan by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("QR / Barcode Challenge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Scan your wake-up code placed outside bed", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(130.dp)
                .background(Color(0xFF222233), RoundedCornerShape(16.dp))
                .border(2.dp, CoralPrimary, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(targetCode.take(12) + "...", color = Color.LightGray, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                simulatedScan = true
                onScanned()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Scan Target Code", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PhraseDismissalChallenge(
    targetPhrase: String,
    onMatched: () -> Unit
) {
    var typedText by remember { mutableStateOf("") }
    val isMatch = typedText.trim().equals(targetPhrase.trim(), ignoreCase = true)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Typed-Phrase Challenge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Type the exact wakefulness statement", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222235)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "\"$targetPhrase\"",
                color = CoralPrimary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = typedText,
            onValueChange = { typedText = it },
            placeholder = { Text("Type phrase here...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CoralPrimary,
                unfocusedBorderColor = Color.DarkGray
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onMatched,
            enabled = isMatch,
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(if (isMatch) "Dismiss Alarm" else "Match Required", fontWeight = FontWeight.Bold)
        }
    }
}
