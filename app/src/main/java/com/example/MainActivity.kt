package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SalimClockViewModel
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.BedtimeScreen
import com.example.ui.screens.ClockFaceStudioScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.WorldClockScreen
import com.example.ui.theme.BedtimeRoseGold
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MyApplicationTheme

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.DARK) }
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            MyApplicationTheme(darkTheme = isDark) {
                SalimClockApp(
                    themeMode = themeMode,
                    onToggleTheme = {
                        themeMode = when (themeMode) {
                            ThemeMode.DARK -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.SYSTEM
                            ThemeMode.SYSTEM -> ThemeMode.DARK
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalimClockApp(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    viewModel: SalimClockViewModel = viewModel()
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val timerPresets by viewModel.timerPresets.collectAsStateWithLifecycle()
    val savedCities by viewModel.savedCities.collectAsStateWithLifecycle()
    val bedtimeSchedule by viewModel.bedtimeSchedule.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showFaceStudio by remember { mutableStateOf(false) }

    BackHandler(enabled = showFaceStudio) {
        showFaceStudio = false
    }

    val activeColor = when (selectedTab) {
        0 -> CoralPrimary
        1 -> CyanPrimary
        2 -> EmeraldPrimary
        3 -> IndigoPrimary
        else -> BedtimeRoseGold
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Salim Clock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                actions = {
                    // Face Studio button
                    IconButton(
                        onClick = { showFaceStudio = !showFaceStudio },
                        modifier = Modifier.testTag("face_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Clock Faces & Widgets",
                            tint = if (showFaceStudio) CoralPrimary else Color.LightGray
                        )
                    }

                    // Theme Mode Toggle (System, Dark, Light)
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.LIGHT -> Icons.Default.Brightness7
                                else -> Icons.Default.Brightness4
                            },
                            contentDescription = "Switch theme: $themeMode",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (!showFaceStudio) {
                NavigationBar(
                    containerColor = Color(0xFF101018),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val navItems = listOf(
                        Triple(0, "Alarms", Icons.Default.Alarm),
                        Triple(1, "Timer", Icons.Default.HourglassBottom),
                        Triple(2, "Stopwatch", Icons.Default.AvTimer),
                        Triple(3, "World", Icons.Default.Public),
                        Triple(4, "Bedtime", Icons.Default.Bedtime)
                    )

                    navItems.forEach { (index, title, icon) ->
                        val isSelected = selectedTab == index
                        val tabColor = when (index) {
                            0 -> CoralPrimary
                            1 -> CyanPrimary
                            2 -> EmeraldPrimary
                            3 -> IndigoPrimary
                            else -> BedtimeRoseGold
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = tabColor,
                                selectedTextColor = tabColor,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = tabColor.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_$title")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showFaceStudio) {
                ClockFaceStudioScreen()
            } else {
                when (selectedTab) {
                    0 -> AlarmScreen(
                        alarms = alarms,
                        onToggleAlarm = { alarm, isEnabled ->
                            viewModel.toggleAlarm(alarm, isEnabled)
                        },
                        onSaveAlarm = { alarm ->
                            viewModel.saveAlarm(alarm)
                        },
                        onDeleteAlarm = { id ->
                            viewModel.deleteAlarm(id)
                        }
                    )
                    1 -> TimerScreen(
                        presets = timerPresets,
                        onSavePreset = { preset ->
                            viewModel.saveTimerPreset(preset)
                        },
                        onDeletePreset = { preset ->
                            viewModel.deleteTimerPreset(preset)
                        }
                    )
                    2 -> StopwatchScreen()
                    3 -> WorldClockScreen(
                        cities = savedCities,
                        onAddCity = { city ->
                            viewModel.addCity(city)
                        },
                        onDeleteCity = { city ->
                            viewModel.deleteCity(city)
                        }
                    )
                    4 -> BedtimeScreen(
                        schedule = bedtimeSchedule,
                        onUpdateSchedule = { schedule ->
                            viewModel.updateBedtimeSchedule(schedule)
                        }
                    )
                }
            }
        }
    }
}
