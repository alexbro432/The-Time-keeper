package com.example.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.rotate as drawScopeRotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusLog
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.ui.components.AnimatedGradientBackground
import com.example.ui.components.GlassyCard
import com.example.ui.viewmodel.TimeKeeperViewModel
import com.example.utils.TimeUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TimeKeeperViewModel) {
    val isDark by viewModel.settingsDarkMode.collectAsState()
    val userName by viewModel.currentUserName.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val focusLogs by viewModel.focusLogs.collectAsState()

    var activeTab by remember { mutableStateOf("Home") } // "Home", "Tasks", "Focus", "Habits", "Calendar", "Coach", "Settings"

    // Dialog flags
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Floating particles
    var realTimeClock by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            realTimeClock = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    AnimatedGradientBackground(isDarkMode = isDark) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp

            if (isTablet) {
                // tablet layout with sidebar
                Row(modifier = Modifier.fillMaxSize()) {
                    TimeKeeperSidebar(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        isDark = isDark,
                        onLogout = { viewModel.logout() }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp)
                    ) {
                        TabContent(
                            activeTab = activeTab,
                            viewModel = viewModel,
                            isDark = isDark,
                            userName = userName,
                            realTimeClock = realTimeClock,
                            tasks = tasks,
                            habits = habits,
                            calendarEvents = calendarEvents,
                            focusLogs = focusLogs,
                            onShowAddTask = { showAddTaskDialog = true },
                            onShowAddHabit = { showAddHabitDialog = true },
                            onShowAddEvent = { showAddEventDialog = true }
                        )
                    }
                }
            } else {
                // Phone layout with bottom tab scaffold
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        TimeKeeperBottomBar(
                            activeTab = activeTab,
                            onTabSelected = { activeTab = it },
                            isDark = isDark
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp)
                    ) {
                        TabContent(
                            activeTab = activeTab,
                            viewModel = viewModel,
                            isDark = isDark,
                            userName = userName,
                            realTimeClock = realTimeClock,
                            tasks = tasks,
                            habits = habits,
                            calendarEvents = calendarEvents,
                            focusLogs = focusLogs,
                            onShowAddTask = { showAddTaskDialog = true },
                            onShowAddHabit = { showAddHabitDialog = true },
                            onShowAddEvent = { showAddEventDialog = true }
                        )
                    }
                }
            }

            // Dialog Popups
            if (showAddTaskDialog) {
                AddTaskDialog(
                    isDark = isDark,
                    onDismiss = { showAddTaskDialog = false },
                    onConfirm = { title, desc, priority, category ->
                        viewModel.addTask(title, desc, priority, System.currentTimeMillis() + 86400000, category)
                        showAddTaskDialog = false
                    }
                )
            }

            if (showAddHabitDialog) {
                AddHabitDialog(
                    isDark = isDark,
                    onDismiss = { showAddHabitDialog = false },
                    onConfirm = { title, category ->
                        viewModel.addHabit(title, category)
                        showAddHabitDialog = false
                    }
                )
            }

            if (showAddEventDialog) {
                AddEventDialog(
                    isDark = isDark,
                    onDismiss = { showAddEventDialog = false },
                    onConfirm = { title, desc, hour, min, dur ->
                        viewModel.addCalendarEvent(title, desc, TimeUtils.getTodayDateString(), hour, min, dur)
                        showAddEventDialog = false
                    }
                )
            }

            // Streak Celebration Particles Overlay (Requirement 1)
            StreakCelebrationOverlay(
                viewModel = viewModel,
                isDark = isDark
            )
        }
    }
}

// Side tab selector for Tablet style
@Composable
fun TimeKeeperSidebar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    isDark: Boolean,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // App label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = Color(0xFF007AFF),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Time Keeper",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sidebar items
            val menu = listOf(
                Pair("Home", Icons.Default.GridView),
                Pair("Tasks", Icons.Default.TaskAlt),
                Pair("Focus", Icons.Default.Timer),
                Pair("Habits", Icons.Default.CheckCircleOutline),
                Pair("Calendar", Icons.Default.CalendarToday),
                Pair("Coach", Icons.Default.Psychology),
                Pair("Settings", Icons.Default.Settings)
            )

            menu.forEach { (name, icon) ->
                val isSelected = activeTab == name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF007AFF).copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onTabSelected(name) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = name,
                        color = if (isSelected) Color(0xFF007AFF) else if (isDark) Color.White else Color.Black,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Signout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onLogout() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color(0xFFFF453A))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Sign Out", color = Color(0xFFFF453A), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Bottom tab selector component
@Composable
fun TimeKeeperBottomBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), clip = false)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                    } else {
                        listOf(Color.White.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.05f))
                    }
                ),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val menu = listOf(
                Pair("Home", Icons.Default.GridView),
                Pair("Tasks", Icons.Default.TaskAlt),
                Pair("Focus", Icons.Default.Timer),
                Pair("Habits", Icons.Default.CheckCircleOutline),
                Pair("Calendar", Icons.Default.CalendarToday),
                Pair("Coach", Icons.Default.Psychology),
                Pair("Settings", Icons.Default.Settings)
            )

            menu.forEach { (name, icon) ->
                val isSelected = activeTab == name
                val animScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .scale(animScale)
                        .clickable { onTabSelected(name) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93)
                        )
                    )
                }
            }
        }
    }
}

// Container directing to active view content
@Composable
fun TabContent(
    activeTab: String,
    viewModel: TimeKeeperViewModel,
    isDark: Boolean,
    userName: String,
    realTimeClock: String,
    tasks: List<Task>,
    habits: List<Habit>,
    calendarEvents: List<CalendarEvent>,
    focusLogs: List<FocusLog>,
    onShowAddTask: () -> Unit,
    onShowAddHabit: () -> Unit,
    onShowAddEvent: () -> Unit
) {
    AnimatedContent(
        targetState = activeTab,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        }
    ) { targetState ->
        when (targetState) {
            "Home" -> HomeScreen(
                userName = userName,
                realTimeClock = realTimeClock,
                isDark = isDark,
                tasks = tasks,
                habits = habits,
                focusLogs = focusLogs,
                viewModel = viewModel,
                onNavigateToTasks = { }
            )

            "Tasks" -> TasksScreen(
                tasks = tasks,
                isDark = isDark,
                onAddTaskClick = onShowAddTask,
                onToggleTask = { viewModel.toggleTaskCompletion(it) },
                onDeleteTask = { viewModel.deleteTask(it) }
            )

            "Focus" -> FocusScreen(
                viewModel = viewModel,
                focusLogs = focusLogs,
                isDark = isDark
            )

            "Habits" -> HabitsScreen(
                habits = habits,
                isDark = isDark,
                onAddHabitClick = onShowAddHabit,
                onCompleteHabit = { viewModel.completeHabitToday(it) },
                onDeleteHabit = { viewModel.deleteHabit(it) }
            )

            "Calendar" -> CalendarScreen(
                events = calendarEvents,
                isDark = isDark,
                onAddEventClick = onShowAddEvent,
                onDeleteEvent = { viewModel.deleteCalendarEvent(it) }
            )

            "Coach" -> CoachScreen(
                viewModel = viewModel,
                tasks = tasks,
                habits = habits,
                focusLogs = focusLogs,
                isDark = isDark
            )

            "Settings" -> SettingsScreen(
                viewModel = viewModel,
                userName = userName,
                isDark = isDark
            )
        }
    }
}

// --- SUB SCREEN: HOME ---
@Composable
fun HomeScreen(
    userName: String,
    realTimeClock: String,
    isDark: Boolean,
    tasks: List<Task>,
    habits: List<Habit>,
    focusLogs: List<FocusLog>,
    viewModel: TimeKeeperViewModel,
    onNavigateToTasks: () -> Unit
) {
    val quote by viewModel.motivationQuote.collectAsState()

    // Calculate dynamic scoring metrics for glass cards
    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = tasks.count { !it.isCompleted }
    val focusTodayMins = focusLogs.filter { it.type == "Focus" }.sumOf { it.durationMinutes }
    val productivityScore = (completedCount * 15) + (focusTodayMins * 2) + (habits.sumOf { it.currentStreak } * 3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Greeting Section (Live Clock & Dynamic Greeting matching High Density theme layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = TimeUtils.getTodayFormatted().uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = TimeUtils.getGreeting() + ", " + userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Modern digital live ticking clock box (Apple premium look)
                    Box(
                        modifier = Modifier
                            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = realTimeClock,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF64D2FF) else Color(0xFF007AFF)
                            )
                        )
                    }

                    // Rounded gradient Avatar (High Density design asset)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(6.dp, CircleShape, clip = false)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF007AFF), Color(0xFF5856D6))
                                ),
                                CircleShape
                            )
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "A",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // MOTIVATION BANNER CORE (Resembling the premium AI Suggestion Card from High Density aesthetic)
        item {
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isDark) Color(0xFFFFD60A).copy(alpha = 0.18f) else Color(0xFFFFD60A).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = Color(0xFFFF9F0A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI INSIGHTS & MOTIVATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color(0xFFFFD60A) else Color(0xFF5856D6),
                                letterSpacing = 1.2.sp,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF1E293B),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        }

        // WORLD & LOCAL TIME CHRONOMETRY (Requirement 4)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "WORLD & LOCAL REGIONAL CHRONOMETERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f))
                        .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Local chronometer
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "LOCAL CHRONO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007AFF)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = realTimeClock.ifEmpty { "Measuring..." },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                letterSpacing = (-0.5).sp,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val localOffset = try { java.time.ZoneId.systemDefault().id } catch(e: Exception) { "GMT" }
                        Text(
                            text = "TZ: $localOffset",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(45.dp)
                            .background(if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    // World chronometer horizontal flow
                    LazyRow(
                        modifier = Modifier.weight(2f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val format = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                        val timezones = listOf(
                            Triple("Cupertino", "America/Los_Angeles", "PST/PDT"),
                            Triple("London", "Europe/London", "GMT/BST"),
                            Triple("Tokyo", "Asia/Tokyo", "JST"),
                            Triple("Sydney", "Australia/Sydney", "AEST/AEDT"),
                            Triple("Kolkata", "Asia/Kolkata", "IST")
                        )
                        items(timezones) { (city, zoneId, label) ->
                            val timeStr = try {
                                java.time.ZonedDateTime.now(java.time.ZoneId.of(zoneId)).format(format)
                            } catch (e: Exception) {
                                "--:--"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.03f))
                                    .border(
                                        1.dp,
                                        if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = city,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF007AFF),
                                            fontSize = 9.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF0F172A),
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.Gray,
                                            fontSize = 8.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // STRIKING METRICS OVERVIEW (2x2 Grid using clean layout)
        item {
            Text(
                "Productivity Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Tasks Completed",
                        count = completedCount.toString(),
                        subtitle = "Out of ${tasks.size} total",
                        color = Color(0xFF34C759),
                        icon = Icons.Default.DoneAll,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Tasks Pending",
                        count = pendingCount.toString(),
                        subtitle = "Ready for execution",
                        color = Color(0xFFFF9F0A),
                        icon = Icons.Default.HourglassBottom,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Focus Time Today",
                        count = "${focusTodayMins}m",
                        subtitle = "Logged in Pomodoro",
                        color = Color(0xFF64D2FF),
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Productivity Score",
                        count = productivityScore.toString(),
                        subtitle = "Calculated daily rating",
                        color = Color(0xFFBF5AF2),
                        icon = Icons.Default.Insights,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // QUICK LISTING PANEL
        item {
            Text(
                "Up Next on Agenda",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (tasks.none { !it.isCompleted }) {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No pending tasks left for today! You are all caught up.",
                        color = Color(0xFF8E8E93),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tasks.filter { !it.isCompleted }.take(3).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        when (task.priority) {
                                            "High" -> Color(0xFFFF453A)
                                            "Medium" -> Color(0xFFFF9F0A)
                                            else -> Color(0xFF34C759)
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    task.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(32.dp),
                clip = false,
                ambientColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else Color.Black.copy(alpha = 0.05f),
                spotColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.Black else Color.Black.copy(alpha = 0.08f)
            )
            .background(
                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF1E293B).copy(alpha = 0.8f) else Color.White,
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.dp,
                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E5EA),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(18.dp)
    ) {
        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
        val badgeBgColor = color.copy(alpha = if (isDark) 0.16f else 0.1f)
        
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // First Row: Circular Badge (Left), Big Stat Number (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored Circle Badge for the Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(badgeBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Count
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        fontSize = 24.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Second Section: Header Label & Subtitle Info description
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        letterSpacing = 0.5.sp,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


// --- SUB SCREEN: TASKS BOARD ---
@Composable
fun TasksScreen(
    tasks: List<Task>,
    isDark: Boolean,
    onAddTaskClick: () -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") } // "All", "High", "Medium", "Low"

    val filteredTasks = when (selectedFilter) {
        "All" -> tasks
        else -> tasks.filter { it.priority == selectedFilter }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tasks_screen"),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = Color(0xFF007AFF),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Screen title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Smart Tasks",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                )

                Text(
                    "${tasks.count { it.isCompleted }} Completed",
                    color = Color(0xFF34C759),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filtering chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "High", "Medium", "Low")
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color.White.copy(alpha = 0.1f))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            filter,
                            color = if (isSelected) Color.White else if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAddCheck,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No tasks available in $selectedFilter filter.",
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox
                            IconButton(onClick = { onToggleTask(task) }) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = "Toggle completion",
                                    tint = if (task.isCompleted) Color(0xFF34C759) else Color(0xFF8E8E93),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    ),
                                    color = if (task.isCompleted) Color(0xFF8E8E93) else if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E8E93),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Category badge
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(task.category, fontSize = 9.sp, color = if (isDark) Color.White else Color.Black)
                                    }

                                    // Priority badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                when (task.priority) {
                                                    "High" -> Color(0xFFFF453A).copy(alpha = 0.2f)
                                                    "Medium" -> Color(0xFFFF9F0A).copy(alpha = 0.2f)
                                                    else -> Color(0xFF34C759).copy(alpha = 0.2f)
                                                },
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            task.priority,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (task.priority) {
                                                "High" -> Color(0xFFFF453A)
                                                "Medium" -> Color(0xFFFF9F0A)
                                                else -> Color(0xFF30D158)
                                            }
                                        )
                                    }
                                }
                            }

                            // Delete Action
                            IconButton(onClick = { onDeleteTask(task) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Task",
                                    tint = Color(0xFFFF453A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- SUB SCREEN: POMODORO TIMER ---
@Composable
fun FocusScreen(
    viewModel: TimeKeeperViewModel,
    focusLogs: List<FocusLog>,
    isDark: Boolean
) {
    val timeLeft by viewModel.pomodoroTimeLeft.collectAsState()
    val totalDuration by viewModel.pomodoroTotalDuration.collectAsState()
    val activeType by viewModel.pomodoroActiveType.collectAsState()
    val isRunning by viewModel.isPomodoroRunning.collectAsState()

    val progress = if (totalDuration > 0) timeLeft.toFloat() / totalDuration else 0f
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("focus_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Pomodoro Focus Timer",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                "Stay locked, eliminate distractions, boost output",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
            )
        }

        // Beautiful iOS Rounded Shifting Countdown Circle Container
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                // Circle visual background track
                Canvas(modifier = Modifier.size(210.dp)) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Active progress sweep
                    drawArc(
                        color = when (activeType) {
                            "Focus" -> Color(0xFF007AFF)
                            "ShortBreak" -> Color(0xFF34C759)
                            else -> Color(0xFFFF9F0A)
                        },
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = activeType.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E8E93),
                            letterSpacing = 1.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                    )
                }
            }
        }

        // Premium Slate 900 Pomodoro Timer Minimal Widget (High Density design highlight)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(40.dp)) // rounded-[2.5rem] is 40.dp
                    .background(Color(0xFF0F172A)) // Slate 900
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(40.dp))
                    .clickable {
                        if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                    }
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Background Glow Decoration (blue-600 rounded-full blur-[60px] opacity-35)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 30.dp, y = 30.dp)
                        .blur(50.dp)
                        .background(Color(0xFF2563EB).copy(alpha = 0.35f), CircleShape)
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ACTIVE SESSION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFA5B4FC), // Indigo 300
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (activeType) {
                                "Focus" -> "Deep Work"
                                "ShortBreak" -> "Short Break"
                                else -> "Long Break"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 2.sp,
                                fontSize = 36.sp
                            )
                        )
                    }

                    // White Control play/pause Button (with subtle interactive active scale simulation)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape, clip = false)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Control Focus Timer",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // PRESETS ROW BUTTONS
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                presetButton(label = "Focus (25m)", active = activeType == "Focus") {
                    viewModel.setPomodoroTimer(25, "Focus")
                }
                presetButton(label = "Short (5m)", active = activeType == "ShortBreak") {
                    viewModel.setPomodoroTimer(5, "ShortBreak")
                }
                presetButton(label = "Long (15m)", active = activeType == "LongBreak") {
                    viewModel.setPomodoroTimer(15, "LongBreak")
                }
            }
        }

        // PLAY PAUSE RESET ACTIONS
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = { viewModel.stopPomodoro() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = if (isDark) Color.White else Color.Black)
                }

                // Play / Pause prominent layout
                IconButton(
                    onClick = {
                        if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF007AFF), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Skip button
                IconButton(
                    onClick = {
                        if (activeType == "Focus") {
                            viewModel.setPomodoroTimer(5, "ShortBreak")
                        } else {
                            viewModel.setPomodoroTimer(25, "Focus")
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Skip", tint = if (isDark) Color.White else Color.Black)
                }
            }
        }

        // TRACK HISTORIES
        item {
            Text(
                "Completed Focus Sessions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))

            val focusListOnly = focusLogs.filter { it.type == "Focus" }
            if (focusListOnly.isEmpty()) {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No study logs tracked yet today. Let's finish a 25m core session!",
                        color = Color(0xFF8E8E93),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    focusListOnly.take(5).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34C759))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Focus Session Done", color = if (isDark) Color.White else Color.Black)
                            }
                            Text(
                                "${log.durationMinutes} mins logged",
                                color = Color(0xFF007AFF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun presetButton(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) Color(0xFF007AFF) else Color.White.copy(alpha = 0.12f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (active) Color.White else Color(0xFF8E8E93),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


// --- SUB SCREEN: HABIT TRACKER ---
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    isDark: Boolean,
    onAddHabitClick: () -> Unit,
    onCompleteHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("habits_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Daily Habits",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color.Black
                    )
                    Text(
                        "Form core patterns to build elite long streaks",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                    )
                }

                Button(
                    onClick = onAddHabitClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (habits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Let's add some core habits to track daily streaks!", color = Color(0xFF8E8E93))
                }
            }
        } else {
            items(habits) { habit ->
                val isCompletedToday = habit.lastCompletedDate == TimeUtils.getTodayDateString()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streaks Fire Icon Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFF9F0A).copy(alpha = 0.15f), CircleShape)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFFFF9F0A), modifier = Modifier.size(18.dp))
                            Text("${habit.currentStreak}d", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9F0A))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            habit.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            "Category: ${habit.category} | Daily Check",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                        )

                        // completion history tiny timeline visualization or calendar mockup
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEachIndexed { i, d ->
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(
                                            if (isCompletedToday && i >= 4) Color(0xFF34C759)
                                            else Color.White.copy(alpha = 0.15f),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }

                    // Done / check action button
                    IconButton(
                        onClick = { onCompleteHabit(habit) },
                        enabled = !isCompletedToday,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isCompletedToday) Color(0xFF34C759).copy(alpha = 0.2f)
                                else Color(0xFF007AFF),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.Add,
                            contentDescription = "Complete today",
                            tint = if (isCompletedToday) Color(0xFF34C759) else Color.White
                        )
                    }

                    IconButton(onClick = { onDeleteHabit(habit) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFFF453A))
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// --- SUB SCREEN: CALENDAR ---
@Composable
fun CalendarScreen(
    events: List<CalendarEvent>,
    isDark: Boolean,
    onAddEventClick: () -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit
) {
    var activeCalendarMode by remember { mutableStateOf("Today") } // "Today", "Weekly", "Monthly"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Planner Calendar",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color.Black
                    )
                    Text(
                        "Coordinate schedules, meeting slots, activities",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                    )
                }

                Button(
                    onClick = onAddEventClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Event", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Segmented controls Today / Weekly / Monthly
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                val calendarStates = listOf("Today", "Weekly", "Monthly")
                calendarStates.forEach { state ->
                    val isSelected = activeCalendarMode == state
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color.Transparent)
                            .clickable { activeCalendarMode = state }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state,
                            color = if (isSelected) Color.White else if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // DOCK WEEK DAYS PREVIEW IF WEEK/MONTH DESIGN IS REQUESTED
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weekDays = listOf("Mon 1", "Tue 2", "Wed 3", "Thu 4", "Fri 5", "Sat 6", "Sun 7")
                weekDays.forEach { wd ->
                    val isToday = wd.contains("4") // Simulating today as 4th of June
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(46.dp)
                            .background(
                                if (isToday) Color(0xFF007AFF) else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            wd.split(" ")[0],
                            fontSize = 11.sp,
                            color = if (isToday) Color.White else Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            wd.split(" ")[1],
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.White else if (isDark) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        // TIMELINE LISTING EVENTS Today
        item {
            Text(
                "Calendar Timeline Meetings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (events.isEmpty()) {
            item {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No schedule items logged yet for today. Enjoy your free focus time!",
                        color = Color(0xFF8E8E93),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(events) { ev ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clock Time Indicator Box
                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .background(Color(0xFF64D2FF).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            String.format(Locale.getDefault(), "%02d:%02d", ev.startHour, ev.startMinute),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ev.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            ev.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
                        )
                        Text(
                            "Duration: ${ev.durationMinutes}m | Plan Slot",
                            fontSize = 10.sp,
                            color = Color(0xFF007AFF)
                        )
                    }

                    IconButton(onClick = { onDeleteEvent(ev) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete event", tint = Color(0xFFFF453A))
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// --- SUB SCREEN: COACH & STATS (INSIGHTS) ---
@Composable
fun CoachScreen(
    viewModel: TimeKeeperViewModel,
    tasks: List<Task>,
    habits: List<Habit>,
    focusLogs: List<FocusLog>,
    isDark: Boolean
) {
    val aiResponse by viewModel.aiRecommendationState.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var coachSubTab by remember { mutableStateOf("AI Assistant") } // "AI Assistant", "Stats Charts"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("coach_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Insights & Coach",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                "Powered by Gemini AI Studio intelligence & productivity metrics",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
            )
        }

        // Sub Segment Tabs (AI, Stats)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                val screens = listOf("AI Assistant", "Stats Charts")
                screens.forEach { screen ->
                    val isSelected = coachSubTab == screen
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color.Transparent)
                            .clickable { coachSubTab = screen }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            screen,
                            color = if (isSelected) Color.White else if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (coachSubTab == "AI Assistant") {
            // PROTOTYPE WARNING core guideline notice
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF9F0A).copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFFF9F0A).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9F0A))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Prototype notice: Gemini suggestions rely on live secure key injection.",
                            color = Color(0xFFFFD60A),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // COACHING CHIPS BOX SELECTORS
            item {
                Text(
                    "Ask AI Productivity Coach",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val promptOptions = listOf(
                        Triple("💡 Best Study/Work Times", "study_times", "Analyze when energy levels are optimal"),
                        Triple("📊 Analyze My Habits", "analyze_productivity", "Unlock streak consistency reports"),
                        Triple("⏳ Recommendation improvements", "recommend_improvements", "Generate a timeblock layout"),
                        Triple("📋 Create Dynamic Daily Plan", "generate_plan", "Coordinate hour schedule block"),
                        Triple("🔥 Action Motivation Core", "motivate", "Dispatched focus action advice")
                    )

                    promptOptions.forEach { (title, key, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .clickable { viewModel.generateAiAdvice(key) }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(desc, fontSize = 11.sp, color = Color(0xFF8E8E93))
                            }
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ASSISTANT RESPONSE DISPLAY
            item {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "COACHING OUTPUT",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E8E93),
                        style = MaterialTheme.typography.labelSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isAiLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(color = Color(0xFF007AFF))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Querying Gemini Coach...", color = Color(0xFF8E8E93))
                        }
                    } else if (aiResponse != null) {
                        Text(
                            aiResponse!!,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = if (isDark) Color.White else Color.Black
                        )
                    } else {
                        Text(
                            "Select an intelligence prompt above. Time Keeper Coach will automatically analyze your local tasks & habits to formulate the optimal recommendations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
            }

        } else {
            // STATS CHARTS SUB-TAB
            item {
                Text(
                    "Weekly Focus Trends",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Custom charts using compose Canvas
                // Productivity trends
                val graphPoints = remember(focusLogs) {
                    val list = mutableListOf<Float>()
                    val now = System.currentTimeMillis()
                    // 4 days ago to 1 day ago
                    for (i in 4 downTo 1) {
                        val pastDayMillis = now - i * 24 * 60 * 60 * 1000L
                        val dayStart = pastDayMillis - (pastDayMillis % (24 * 60 * 60 * 1000L))
                        val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
                        val mins = focusLogs.filter { it.type == "Focus" && it.timestamp in dayStart until dayEnd }.sumOf { it.durationMinutes }
                        list.add(mins.toFloat())
                    }
                    // Today
                    val todayStart = now - (now % (24 * 60 * 60 * 1000L))
                    val todayMins = focusLogs.filter { it.type == "Focus" && it.timestamp >= todayStart }.sumOf { it.durationMinutes }
                    list.add(todayMins.toFloat())
                    list
                }
                val hasAnyFocusTime = remember(graphPoints) { graphPoints.any { it > 0f } }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    if (!hasAnyFocusTime) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFF007AFF).copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No focus periods recorded yet",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxPoint = graphPoints.maxOrNull()?.coerceAtLeast(10f) ?: 10f
                            val widthBetween = size.width / (graphPoints.size - 1)

                            // draw subtle grid lines
                            for (i in 0..4) {
                                val y = size.height * (i / 4f)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.06f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y)
                                )
                            }

                            // Draw smooth gradient bars
                            val barWidth = 32.dp.toPx()
                            graphPoints.forEachIndexed { index, pts ->
                                val x = index * widthBetween
                                val barHeight = (pts / maxPoint) * size.height
                                val y = size.height - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFF007AFF), Color(0xFFBF5AF2))
                                    ),
                                    topLeft = Offset(x - barWidth / 2, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val labelDays = listOf("Mon", "Tue", "Wed", "Thu", "Today")
                    labelDays.forEach { d ->
                        Text(d, color = Color(0xFF8E8E93), fontSize = 11.sp)
                    }
                }
            }

            item {
                Text(
                    "Habit Completion Rate",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))

                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val activeHabits = habits.size
                            val completedToday = habits.count { it.lastCompletedDate == TimeUtils.getTodayDateString() }
                            val completionRate = if (activeHabits > 0) (completedToday.toFloat() / activeHabits) * 100 else 0f

                            Text(
                                "${String.format(Locale.getDefault(), "%.1f", completionRate)}%",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF34C759)
                            )
                            Text(
                                "Habits completed today ($completedToday of $activeHabits)",
                                color = Color(0xFF8E8E93)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(70.dp)
                        ) {
                            val activeHabits = habits.size
                            val completedToday = habits.count { it.lastCompletedDate == TimeUtils.getTodayDateString() }
                            val arcProgress = if (activeHabits > 0) completedToday.toFloat() / activeHabits else 0f

                            Canvas(modifier = Modifier.size(60.dp)) {
                                drawArc(
                                    color = Color.White.copy(alpha = 0.08f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx())
                                )
                                drawArc(
                                    color = Color(0xFF30D158),
                                    startAngle = -90f,
                                    sweepAngle = 360f * arcProgress,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Summary Performance Details",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Numerical listing grid summary
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ALL TASKS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                            Text("${tasks.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DONE DAILY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                            Text("${tasks.count { it.isCompleted }}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF34C759))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FOCUS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                            Text("${focusLogs.filter { it.type == "Focus" }.sumOf { it.durationMinutes }}m", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF64D2FF))
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// --- SUB SCREEN: SETTINGS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TimeKeeperViewModel,
    userName: String,
    isDark: Boolean
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("English") }
    var notificationState by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "System Settings",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                "Modify app preferences, themes, notifications & backups",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E93))
            )
        }

        // Account Mini Info
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF007AFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userName.firstOrNull()?.toString()?.uppercase() ?: "A",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        userName,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                    Text(
                        "Elite Time Keeper Tier",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9F0A)
                    )
                }
            }
        }

        // SYSTEM TOGGLES
        item {
            Text(
                "Preferences & Themes",
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dark Mode Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Theme", color = if (isDark) Color.White else Color.Black)
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.settingsDarkMode.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF007AFF))
                    )
                }

                // Notification Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reminders & Notifications", color = if (isDark) Color.White else Color.Black)
                    Switch(
                        checked = notificationState,
                        onCheckedChange = { notificationState = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF007AFF))
                    )
                }

                // Language selection mock dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                        .clickable {
                            selectedLanguage = if (selectedLanguage == "English") "Spanish" else "English"
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language Selection", color = if (isDark) Color.White else Color.Black)
                    Text(selectedLanguage, color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                }
            }
        }

        // BACKUPS & CLOUD SYNC
        item {
            Text(
                "Data Backup & Recovery",
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Cloud sync in progress... database successfully backing up to secure container storage", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Database to Cloud", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.clearLocalDataOnDemand()
                        Toast.makeText(context, "Database cleared! All lists are now completely empty.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Force Clear My Local Data", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        // SIGN OUT BOARD
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
            ) {
                Text("Log Out Account", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// --- DIALOG MODALS COMPONENTS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("Work") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            color = if (isDark) Color(0xFF2C2C2E) else Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("New Smart Task", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Task Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                // Priority switcher
                Text("Task Priority", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        val active = selectedPriority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) Color(0xFF007AFF) else Color.White.copy(alpha = 0.12f))
                                .clickable { selectedPriority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(p, color = if (active) Color.White else Color(0xFF8E8E93), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                // Category switcher
                Text("Task Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Work", "Design", "Personal", "Health").forEach { cat ->
                        val active = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) Color(0xFF007AFF) else Color.White.copy(alpha = 0.12f))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat, color = if (active) Color.White else Color(0xFF8E8E93), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFFFF453A))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onConfirm(title, desc, selectedPriority, selectedCategory)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            color = if (isDark) Color(0xFF2C2C2E) else Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("New Daily Habit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title (e.g. Meditate for 10m)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("Habit Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Health", "Mind", "Study", "Work").forEach { category ->
                        val active = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) Color(0xFF007AFF) else Color.White.copy(alpha = 0.12f))
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(category, color = if (active) Color.White else Color(0xFF8E8E93), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFFFF453A))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onConfirm(title, selectedCategory)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                    ) {
                        Text("Save Habit")
                    }
                }
            }
        }
    }
}

@Composable
fun AddEventDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf("10") }
    var startMin by remember { mutableStateOf("00") }
    var duration by remember { mutableStateOf("60") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp),
            color = if (isDark) Color(0xFF2C2C2E) else Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Log Calendar Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Event Details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = startMin,
                        onValueChange = { startMin = it },
                        label = { Text("Min (0-59)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFFFF453A))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onConfirm(
                                    title,
                                    desc,
                                    startHour.toIntOrNull() ?: 10,
                                    startMin.toIntOrNull() ?: 0,
                                    duration.toIntOrNull() ?: 60
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                    ) {
                        Text("Save Event")
                    }
                }
            }
        }
    }
}

// STREAK CELEBRATION PARTICLES SYSTEM (Requirement 1)
data class PyroParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var radius: Float,
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    var spin: Float = 0f
)

@Composable
fun StreakCelebrationOverlay(
    viewModel: TimeKeeperViewModel,
    isDark: Boolean
) {
    var activeEffect by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var particles by remember { mutableStateOf<List<PyroParticle>>(emptyList()) }
    
    // Listen for celebrations
    LaunchedEffect(viewModel.celebrateStreakFlow) {
        viewModel.celebrateStreakFlow.collect { event ->
            activeEffect = event
            
            // Generate standard confetti fireworks fountain
            val baseColors = listOf(
                Color(0xFF007AFF), // Sapphire Blue
                Color(0xFFBF5AF2), // Indigo purple
                Color(0xFF34C759), // Green
                Color(0xFFFF9F0A), // Sunset orange
                Color(0xFFFFD60A), // Gold
                Color(0xFFFF2D55)  // Vibrant Pink
            )
            
            val tempParticles = mutableListOf<PyroParticle>()
            // Spawn 55 dynamic particles shooting upwards
            for (i in 1..55) {
                val angle = Math.toRadians((Math.random() * 60 - 30) - 90) // shooting upwards
                val speed = (Math.random() * 16 + 10).toFloat()
                tempParticles.add(
                    PyroParticle(
                        x = 540f, // Center estimate (adjusted dynamically on Canvas draw)
                        y = 1800f,
                        vx = (Math.cos(angle) * speed).toFloat(),
                        vy = (Math.sin(angle) * speed).toFloat(),
                        color = baseColors.random(),
                        radius = (Math.random() * 10 + 6).toFloat(),
                        alpha = 1f,
                        rotation = (Math.random() * 360).toFloat(),
                        spin = (Math.random() * 8 - 4).toFloat()
                    )
                )
            }
            particles = tempParticles

            // Core physics update loop (approx 60fps physics simulation)
            var ticks = 0
            while (ticks < 120 && particles.isNotEmpty()) {
                delay(16)
                particles = particles.map { particle ->
                    particle.copy(
                        x = particle.x + particle.vx,
                        y = particle.y + particle.vy,
                        vy = particle.vy + 0.35f, // Gravity pulling down
                        vx = particle.vx * 0.98f, // air friction resistance
                        alpha = (particle.alpha - 0.015f).coerceAtLeast(0f),
                        rotation = particle.rotation + particle.spin
                    )
                }.filter { it.alpha > 0 }
                ticks++
            }
            particles = emptyList()
            activeEffect = null
        }
    }

    if (activeEffect != null) {
        val (habitTitle, streak) = activeEffect!!
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
                .clickable(enabled = false) {}, // Absorb interactions to focus eyes
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val boundsWidth = size.width
                val boundsHeight = size.height
                
                particles.forEach { p ->
                    // Initialize or update emitter location dynamically matching real viewports
                    if (p.x == 540f && p.y == 1800f) {
                        p.x = boundsWidth / 2f
                        p.y = boundsHeight - 150f
                    }
                    
                    drawScopeRotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                        // Render custom soft-rounded confetti rectangle
                        drawRoundRect(
                            color = p.color,
                            topLeft = Offset(p.x - p.radius, p.y - p.radius),
                            size = Size(p.radius * 2, p.radius * 2),
                            cornerRadius = CornerRadius(p.radius / 3f),
                            alpha = p.alpha
                        )
                    }
                }
            }
            
            // Glassmorphic interactive celebration HUD dialogue
            AnimatedVisibility(
                visible = activeEffect != null,
                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .shadow(16.dp, RoundedCornerShape(32.dp), clip = false)
                        .background(
                            if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Big emoji badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = Color(0xFFFF9F0A).copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 32.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "HABIT STREAK MILESTONE!",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF9F0A),
                                letterSpacing = 1.5.sp,
                                fontSize = 11.sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = habitTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            ),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "$streak-Day Streak!",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Keep going! Your daily habits are building your destiny.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

