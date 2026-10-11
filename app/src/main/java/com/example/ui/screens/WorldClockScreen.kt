package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.SavedCityEntity
import com.example.ui.components.ClockFaceStyle
import com.example.ui.components.SalimProgressRing
import com.example.ui.theme.ContinuityPalette
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MagentaSecondary
import com.example.ui.theme.TabularDisplayMedium
import com.example.ui.theme.TabularHeadline
import com.example.ui.theme.TabularTitle
import com.example.ui.theme.WorldClockGradient
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WorldClockScreen(
    cities: List<SavedCityEntity>,
    onAddCity: (SavedCityEntity) -> Unit,
    onDeleteCity: (SavedCityEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showMeetingPlanner by remember { mutableStateOf(false) }

    // Live frame clock
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val localZone = remember { ZoneId.systemDefault() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1B0B2B), // Diffuse indigo-to-magenta accent background
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "World Clock",
                            style = TabularHeadline,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Day-night cycle rings & global meeting planner",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showMeetingPlanner = !showMeetingPlanner },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MagentaSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showMeetingPlanner) "Hide Planner" else "Planner", color = MagentaSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Meeting-Time Planner Component (Expandable)
            if (showMeetingPlanner) {
                item {
                    MeetingTimePlannerCard(
                        localZone = localZone,
                        savedCities = cities
                    )
                }
            }

            // Saved Cities List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Global Cities (${cities.size})",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            items(cities, key = { it.id }) { city ->
                CityClockCard(
                    city = city,
                    localZone = localZone,
                    onDelete = { onDeleteCity(city) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add City
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = IndigoPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add City", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddDialog) {
        AddCityDialog(
            onDismiss = { showAddDialog = false },
            onSelect = { newCity ->
                onAddCity(newCity)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CityClockCard(
    city: SavedCityEntity,
    localZone: ZoneId,
    onDelete: () -> Unit
) {
    val cityZone = remember(city.timeZoneId) {
        try { ZoneId.of(city.timeZoneId) } catch (_: Exception) { ZoneId.of("UTC") }
    }

    val nowLocal = ZonedDateTime.now(localZone)
    val nowCity = ZonedDateTime.now(cityZone)

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm")
    val amPmFormatter = DateTimeFormatter.ofPattern("a")
    val timeFormatted = nowCity.format(timeFormatter)
    val amPm = nowCity.format(amPmFormatter)

    // Hour fraction in 24h day (0.0 to 1.0)
    val hourOfDay = nowCity.hour + (nowCity.minute / 60f)
    val dayProgress = (hourOfDay / 24f).coerceIn(0f, 1f)

    // Is daylight (between 6am and 6pm)
    val isDaylight = nowCity.hour in 6..18

    // Offset relative to local
    val diffSeconds = Duration.between(nowLocal.toOffsetDateTime(), nowCity.toOffsetDateTime()).seconds
    val diffHours = (diffSeconds / 3600).toInt()
    val diffMinutes = ((diffSeconds % 3600) / 60).toInt()
    val relativeText = when {
        diffHours == 0 && diffMinutes == 0 -> "Same time"
        diffHours > 0 -> "+$diffHours HRS"
        else -> "$diffHours HRS"
    }

    // Daylight saving time active check
    val isDst = cityZone.rules.isDaylightSavings(Instant.now())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141324)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // City details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.cityName,
                        style = TabularTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isDst) {
                        Surface(
                            color = IndigoPrimary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "DST",
                                color = IndigoPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${city.country} • $relativeText",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeFormatted,
                        style = TabularDisplayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = amPm,
                        color = MagentaSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // 24-hr Day/Night Cycle Continuity Ring
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SalimProgressRing(
                        progress = dayProgress,
                        colors = ContinuityPalette,
                        strokeWidth = 9.dp,
                        faceStyle = ClockFaceStyle.Continuity,
                        accessibilityText = "${city.cityName} is at $timeFormatted $amPm with 24h position ${(dayProgress * 100).toInt()}%",
                        innerContent = {
                            Icon(
                                imageVector = if (isDaylight) Icons.Default.WbSunny else Icons.Default.Nightlight,
                                contentDescription = if (isDaylight) "Daytime" else "Nighttime",
                                tint = if (isDaylight) Color(0xFFFFD166) else Color(0xFF818CF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun MeetingTimePlannerCard(
    localZone: ZoneId,
    savedCities: List<SavedCityEntity>
) {
    var selectedHour by remember { mutableFloatStateOf(14f) } // Default 2:00 PM local

    val selectedLocalTime = LocalTime.of(selectedHour.toInt(), 0)
    val nowLocalDate = ZonedDateTime.now(localZone).toLocalDate()
    val baseZoned = ZonedDateTime.of(nowLocalDate, selectedLocalTime, localZone)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16152B)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MagentaSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Global Meeting Planner",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = String.format("%02d:00 Local", selectedHour.toInt()),
                    color = MagentaSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Scrub local time to see working hours (08:00 - 18:00) across all cities",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Slider(
                value = selectedHour,
                onValueChange = { selectedHour = it },
                valueRange = 0f..23f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = MagentaSecondary,
                    activeTrackColor = MagentaSecondary,
                    inactiveTrackColor = Color(0xFF28283E)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cities converted time breakdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                savedCities.forEach { city ->
                    val cZone = try { ZoneId.of(city.timeZoneId) } catch (_: Exception) { ZoneId.of("UTC") }
                    val converted = baseZoned.withZoneSameInstant(cZone)
                    val cHour = converted.hour
                    val isWorkingHours = cHour in 8..18
                    val isExtendedHours = cHour in 7..21 && !isWorkingHours

                    val statusColor = when {
                        isWorkingHours -> EmeraldPrimary
                        isExtendedHours -> Color(0xFFFFD166)
                        else -> CoralPrimary
                    }

                    val statusText = when {
                        isWorkingHours -> "Reasonable Hours"
                        isExtendedHours -> "Extended Hours"
                        else -> "Out of Hours (Sleep)"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C32), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(city.cityName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = String.format("%02d:%02d", converted.hour, converted.minute),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddCityDialog(
    onDismiss: () -> Unit,
    onSelect: (SavedCityEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val defaultGlobalCities = remember {
        listOf(
            SavedCityEntity(cityName = "Tokyo", country = "Japan", timeZoneId = "Asia/Tokyo"),
            SavedCityEntity(cityName = "London", country = "United Kingdom", timeZoneId = "Europe/London"),
            SavedCityEntity(cityName = "New York", country = "United States", timeZoneId = "America/New_York"),
            SavedCityEntity(cityName = "San Francisco", country = "United States", timeZoneId = "America/Los_Angeles"),
            SavedCityEntity(cityName = "Dubai", country = "United Arab Emirates", timeZoneId = "Asia/Dubai"),
            SavedCityEntity(cityName = "Sydney", country = "Australia", timeZoneId = "Australia/Sydney"),
            SavedCityEntity(cityName = "Paris", country = "France", timeZoneId = "Europe/Paris"),
            SavedCityEntity(cityName = "Berlin", country = "Germany", timeZoneId = "Europe/Berlin"),
            SavedCityEntity(cityName = "Singapore", country = "Singapore", timeZoneId = "Asia/Singapore"),
            SavedCityEntity(cityName = "Mumbai", country = "India", timeZoneId = "Asia/Kolkata"),
            SavedCityEntity(cityName = "Hong Kong", country = "China", timeZoneId = "Asia/Hong_Kong"),
            SavedCityEntity(cityName = "Seoul", country = "South Korea", timeZoneId = "Asia/Seoul"),
            SavedCityEntity(cityName = "Cairo", country = "Egypt", timeZoneId = "Africa/Cairo"),
            SavedCityEntity(cityName = "São Paulo", country = "Brazil", timeZoneId = "America/Sao_Paulo"),
            SavedCityEntity(cityName = "Auckland", country = "New Zealand", timeZoneId = "Pacific/Auckland"),
            SavedCityEntity(cityName = "Toronto", country = "Canada", timeZoneId = "America/Toronto")
        )
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) defaultGlobalCities
        else defaultGlobalCities.filter {
            it.cityName.contains(searchQuery, ignoreCase = true) ||
            it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16152B)),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Global City", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city or country...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IndigoPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { city ->
                        Surface(
                            onClick = { onSelect(city) },
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E1D34),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(city.cityName, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(city.country, color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(city.timeZoneId, color = MagentaSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
