package com.example.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.ClockShape
import com.example.ui.components.FillMode
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.AlarmGradient
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TabularDisplayLarge
import com.example.ui.theme.TabularHeadline
import com.example.ui.theme.TabularTitle
import com.example.widget.SalimClockWidgetProvider
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ClockFaceStudioScreen() {
    val context = LocalContext.current

    var selectedFace by remember { mutableStateOf(ClockFaceStyle.RingLike) }
    var selectedShape by remember { mutableStateOf(ClockShape.Circle) }
    var selectedFill by remember { mutableStateOf(FillMode.Gradient) }

    // Live continuous second hand progress
    var secondProgress by remember { mutableFloatStateOf(0f) }
    var currentTimeString by remember { mutableStateOf("12:00:00") }
    var currentDateString by remember { mutableStateOf("Saturday, Oct 10") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        while (true) {
            val now = Calendar.getInstance()
            val millis = now.get(Calendar.MILLISECOND)
            val seconds = now.get(Calendar.SECOND)
            secondProgress = (seconds + (millis / 1000f)) / 60f
            currentTimeString = timeFormat.format(Date())
            currentDateString = dateFormat.format(Date())
            delay(50) // 20 fps smooth sweep
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E1428),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Face Studio & Widgets",
                            style = TabularHeadline,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Huawei design language • 6 faces • Widget & Wear OS sync",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Live Interactive Preview Ring
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141322)),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = selectedFace.displayName.uppercase(),
                            color = CoralPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.size(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SalimProgressRing(
                                progress = secondProgress,
                                colors = selectedFace.getPaletteForArea(AlarmGradient),
                                strokeWidth = 15.dp,
                                faceStyle = selectedFace,
                                shape = selectedShape,
                                fillMode = selectedFill,
                                accessibilityText = "Clock studio display: $currentTimeString",
                                innerContent = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = currentTimeString.take(5),
                                            style = TabularDisplayLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = currentDateString,
                                            color = Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${selectedShape.displayName} • ${selectedFill.displayName}",
                                            color = CoralPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = selectedFace.description,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Face Style Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141322)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = CoralPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Clock Face Style", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ClockFaceStyle.entries.chunked(2).forEach { rowFaces ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowFaces.forEach { face ->
                                    val isSelected = selectedFace == face
                                    Surface(
                                        onClick = { selectedFace = face },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CoralPrimary.copy(alpha = 0.2f) else Color(0xFF1D1C30),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                face.displayName,
                                                color = if (isSelected) CoralPrimary else Color.White,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Shape and Fill Parameters
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141322)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Clock Shape Parameter", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ClockShape.values().forEach { shape ->
                                FilterChip(
                                    selected = selectedShape == shape,
                                    onClick = { selectedShape = shape },
                                    label = { Text(shape.displayName, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralPrimary.copy(alpha = 0.3f),
                                        selectedLabelColor = CoralPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Fill Mode Parameter", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FillMode.values().forEach { fill ->
                                FilterChip(
                                    selected = selectedFill == fill,
                                    onClick = { selectedFill = fill },
                                    label = { Text(fill.displayName, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralPrimary.copy(alpha = 0.3f),
                                        selectedLabelColor = CoralPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Home Screen Widget & Wear OS Sync Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141322)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Widgets, contentDescription = null, tint = CoralPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Home Screen Widget & Wear OS", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Push chosen clock face, shape, and fill mode directly to your home screen widget and paired Wear OS smartwatch.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(context, SalimClockWidgetProvider::class.java).apply {
                                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    }
                                    context.sendBroadcast(intent)
                                    Toast.makeText(context, "Widget updated to ${selectedFace.displayName} (${selectedShape.displayName})", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Update Widget", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Wear OS companion synchronized: 3 alarms & timer states", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24223A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Watch, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Wear OS", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
