package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.Alarm
import com.example.ui.viewmodel.TimeKeeperViewModel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    viewModel: TimeKeeperViewModel,
    isDark: Boolean
) {
    val alarms by viewModel.alarms.collectAsState()

    // Screen-scoped states for temporary editing / creating
    var alarmHour by remember { mutableStateOf(7) }
    var alarmMinute by remember { mutableStateOf(0) }
    var alarmIsAm by remember { mutableStateOf(true) }
    var alarmLabel by remember { mutableStateOf("Sleek Morning Rise") }
    var alarmDays by remember { mutableStateOf(listOf("Mon", "Tue", "Wed", "Thu", "Fri")) }
    var alarmSound by remember { mutableStateOf("Serene Forest Spark") }
    var alarmSmartInterval by remember { mutableStateOf(10) } // mins pre-wake

    var listEditingAlarmId by remember { mutableStateOf<String?>(null) }
    var isSetHourMode by remember { mutableStateOf(true) } // true = Hour dial, false = Minute dial

    val sounds = listOf(
        "Serene Forest Spark",
        "Echoes of Dawn",
        "Dynamic Hustle Clang",
        "Calm Lofi Ocean",
        "Gentle Harp Ripple",
        "Cosmic Deep Space"
    )

    val smartIntervals = listOf(
        Pair("None", 0),
        Pair("5m Interval", 5),
        Pair("10m Interval", 10),
        Pair("15m Interval", 15),
        Pair("20m Interval", 20),
        Pair("30m Interval", 30)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("alarms_screen"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- LUXURY BRAND HEADER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column {
                    Text(
                        text = "LUXURY CHRONOSPORTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF),
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Smart Wake-Up Station",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Interactive premium alarm matrices & custom audio triggers.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }
        }

        // --- INTERACTIVE VISUAL DIAL CONTROLS CONTAINER ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.White)
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE5E5EA),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Time Readout Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "CURRENT SELECTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("%02d : %02d", alarmHour, alarmMinute),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (alarmIsAm) "AM" else "PM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF)
                                    )
                                )
                            }
                        }

                        // Toggle AM/PM selection
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray, RoundedCornerShape(10.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(if (alarmIsAm) (if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF)) else Color.Transparent)
                                    .clickable { alarmIsAm = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "AM",
                                    color = if (alarmIsAm) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(if (!alarmIsAm) (if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF)) else Color.Transparent)
                                    .clickable { alarmIsAm = false }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "PM",
                                    color = if (!alarmIsAm) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    // Mode selectors (Set Hour vs Set Minute)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.02f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isSetHourMode = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSetHourMode) (if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = if (isDark) Color.White else Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Hour", color = if (isDark) Color.White else Color.Black, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = { isSetHourMode = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isSetHourMode) (if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = if (isDark) Color.White else Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Minute", color = if (isDark) Color.White else Color.Black, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    // --- VISUAL ANALOG DIAL CANVAS ---
                    var viewSize by remember { mutableStateOf(IntSize.Zero) }
                    val interactiveColor = if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF)
                    val passiveColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)

                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f))
                            .border(
                                width = 1.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        interactiveColor.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .onGloballyPositioned { viewSize = it.size }
                            .pointerInput(isSetHourMode) {
                                detectDragGestures { change, _ ->
                                    val size = viewSize
                                    if (size.width > 0 && size.height > 0) {
                                        val offset = change.position
                                        val cX = size.width / 2f
                                        val cY = size.height / 2f
                                        val dx = offset.x - cX
                                        val dy = offset.y - cY
                                        val angleRadians = kotlin.math.atan2(dy, dx)
                                        var angleDegrees = Math.toDegrees(angleRadians.toDouble()) + 90.0
                                        if (angleDegrees < 0) {
                                            angleDegrees += 360.0
                                        }
                                        if (isSetHourMode) {
                                            val hour = ((angleDegrees + 15) % 360 / 30).toInt()
                                            alarmHour = if (hour == 0) 12 else hour
                                        } else {
                                            alarmMinute = ((angleDegrees + 3) % 360 / 6).toInt()
                                        }
                                    }
                                }
                            }
                            .pointerInput(isSetHourMode) {
                                detectTapGestures { offset ->
                                    val size = viewSize
                                    if (size.width > 0 && size.height > 0) {
                                        val cX = size.width / 2f
                                        val cY = size.height / 2f
                                        val dx = offset.x - cX
                                        val dy = offset.y - cY
                                        val angleRadians = kotlin.math.atan2(dy, dx)
                                        var angleDegrees = Math.toDegrees(angleRadians.toDouble()) + 90.0
                                        if (angleDegrees < 0) {
                                            angleDegrees += 360.0
                                        }
                                        if (isSetHourMode) {
                                            val hour = ((angleDegrees + 15) % 360 / 30).toInt()
                                            alarmHour = if (hour == 0) 12 else hour
                                        } else {
                                            alarmMinute = ((angleDegrees + 3) % 360 / 6).toInt()
                                        }
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val r = w / 2f - 16.dp.toPx()

                            // Draw central point
                            drawCircle(
                                color = interactiveColor,
                                radius = 6.dp.toPx()
                            )

                            // Draw outer clock ring circle limit
                            drawCircle(
                                color = passiveColor,
                                radius = r + 4.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Hour hand math (1 hour is 30 degrees, plus fractional minute shift)
                            val hourAngleDeg = (alarmHour % 12) * 30f + (alarmMinute / 60f) * 30f
                            val hourAngleRad = Math.toRadians((hourAngleDeg - 90f).toDouble())
                            val hourHandLen = r * 0.55f
                            drawContext.canvas.apply {
                                drawLine(
                                    color = if (isDark) Color(0xFFFFCC00) else Color(0xFFE28400),
                                    start = Offset(w / 2f, h / 2f),
                                    end = Offset(
                                        (w / 2f + cos(hourAngleRad) * hourHandLen).toFloat(),
                                        (h / 2f + sin(hourAngleRad) * hourHandLen).toFloat()
                                    ),
                                    strokeWidth = 5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Minute hand math (1 minute is 6 degrees)
                            val minAngleDeg = alarmMinute * 6f
                            val minAngleRad = Math.toRadians((minAngleDeg - 90f).toDouble())
                            val minHandLen = r * 0.82f
                            drawLine(
                                color = interactiveColor,
                                start = Offset(w / 2f, h / 2f),
                                end = Offset(
                                    (w / 2f + cos(minAngleRad) * minHandLen).toFloat(),
                                    (h / 2f + sin(minAngleRad) * minHandLen).toFloat()
                                ),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Clock Ticks & Indicators
                            for (i in 0 until 12) {
                                val tickAngleRad = Math.toRadians((i * 30 - 90).toDouble())
                                val innerPointLen = r * 0.95f
                                val isMajor = (i % 3 == 0)
                                val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                                val startPointLen = r - tickLen

                                val startX = (w / 2f + cos(tickAngleRad) * startPointLen).toFloat()
                                val startY = (h / 2f + sin(tickAngleRad) * startPointLen).toFloat()
                                val endX = (w / 2f + cos(tickAngleRad) * r).toFloat()
                                val endY = (h / 2f + sin(tickAngleRad) * r).toFloat()

                                drawLine(
                                    color = if (isMajor) interactiveColor else passiveColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = if (isMajor) 3.5.dp.toPx() else 1.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // Text labels placed around analog dial inside the overlay absolute positions
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Text("12", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black), modifier = Modifier.align(Alignment.TopCenter))
                            Text("6", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black), modifier = Modifier.align(Alignment.BottomCenter))
                            Text("9", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black), modifier = Modifier.align(Alignment.CenterStart))
                            Text("3", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black), modifier = Modifier.align(Alignment.CenterEnd))
                        }
                    }

                    // Dial guidance instruction
                    Text(
                        text = if (isSetHourMode) "Drag on the dial to adjust hours smoothly" else "Drag on the dial to adjust minutes smoothly",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // --- CUSTOM ALARM PROFILE SPECIFICATIONS FORM ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.White)
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE5E5EA),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "METADATA & Smart Customizations",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black,
                            letterSpacing = 1.2.sp
                        )
                    )

                    // Label input text field
                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text("Alarm Note / Label") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Unique Sound Settings selection
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF), modifier = Modifier.size(16.dp))
                            Text(
                                "UNIQUE AUDIO PROFILE",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sounds) { s ->
                                val selected = alarmSound == s
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) (if (isDark) Color(0xFF007AFF).copy(alpha = 0.2f) else Color(0xFF1D5CFF).copy(alpha = 0.15f)) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) (if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF)) else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { alarmSound = s }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = s,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (selected) (if (isDark) Color.White else Color(0xFF1D5CFF)) else (if (isDark) Color.LightGray else Color.DarkGray),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Smart Wake-up intervals
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Waves, contentDescription = null, tint = if (isDark) Color(0xFFBF5AF2) else Color(0xFF9842F5), modifier = Modifier.size(16.dp))
                            Text(
                                "SMART PRE-WAKE CHRONOMETRIC COGNITION",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Gentle pre-wake starts playing soft micro-frequencies prior to main blast to secure optimal alpha brainwaves.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(smartIntervals) { (label, value) ->
                                val selected = alarmSmartInterval == value
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selected) (if (isDark) Color(0xFFBF5AF2).copy(alpha = 0.2f) else Color(0xFF9842F5).copy(alpha = 0.15f)) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) (if (isDark) Color(0xFFBF5AF2) else Color(0xFF9842F5)) else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { alarmSmartInterval = value }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (selected) (if (isDark) Color.White else Color(0xFF9842F5)) else (if (isDark) Color.LightGray else Color.DarkGray),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Days repeat selector
                    Column {
                        Text(
                            "REPEAT CORNERSTONES",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                val isSelected = alarmDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) (if (isDark) Color(0xFF34C759) else Color(0xFF24B147))
                                            else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f))
                                        )
                                        .clickable {
                                            alarmDays = if (isSelected) {
                                                alarmDays - day
                                            } else {
                                                alarmDays + day
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.take(1),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.White else (if (isDark) Color.White else Color.Black),
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons to SAVE or CANCEL EDIT (or standard ADD)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (listEditingAlarmId != null) {
                            OutlinedButton(
                                onClick = {
                                    listEditingAlarmId = null
                                    alarmLabel = "Sleek Morning Rise"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Abort Edit", color = if (isDark) Color.White else Color.Black)
                            }

                            Button(
                                onClick = {
                                    val currentId = listEditingAlarmId ?: return@Button
                                    val updatedAlarm = Alarm(
                                        id = currentId,
                                        hour = alarmHour,
                                        minute = alarmMinute,
                                        isAm = alarmIsAm,
                                        label = alarmLabel,
                                        days = alarmDays,
                                        sound = alarmSound,
                                        smartInterval = alarmSmartInterval
                                    )
                                    viewModel.updateAlarm(updatedAlarm)
                                    listEditingAlarmId = null
                                    alarmLabel = "Sleek Morning Rise"
                                },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Update Alarm", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.addAlarm(
                                        hour = alarmHour,
                                        minute = alarmMinute,
                                        isAm = alarmIsAm,
                                        label = alarmLabel,
                                        days = alarmDays,
                                        sound = alarmSound,
                                        smartInterval = alarmSmartInterval
                                    )
                                    alarmLabel = "Sleek Morning Rise"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddAlarm, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Commit New Smart Alarm", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- SMART ALARMS ACTIVE MATRIX LIST ---
        item {
            Text(
                text = "COMMITTED SENSORY REPERTOIRES (${alarms.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.2.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (alarms.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.02f) else Color.Black.copy(alpha = 0.01f))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AlarmOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Text(
                            text = "No custom smart alarms active.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            }
        } else {
            items(alarms) { alarm ->
                val isEditingMe = listEditingAlarmId == alarm.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isEditingMe) {
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.03f)
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.04f) else Color.White
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isEditingMe) {
                                if (isDark) Color(0xFF34C759) else Color(0xFF24B147)
                            } else if (alarm.isEnabled) {
                                if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.4f)
                            } else {
                                Color.Transparent
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("%02d:%02d", alarm.hour, alarm.minute),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (alarm.isEnabled) (if (isDark) Color.White else Color.Black) else Color.Gray,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 24.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (alarm.isAm) "AM" else "PM",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (alarm.isEnabled) (if (isDark) Color(0xFF007AFF) else Color(0xFF1D5CFF)) else Color.Gray
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = alarm.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (alarm.isEnabled) (if (isDark) Color.White else Color.Black) else Color.Gray
                                )
                            )

                            // Display metadata fields (Sound, pre-wake interval)
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                                        Text(alarm.sound, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.Gray))
                                    }
                                }

                                if (alarm.smartInterval > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isDark) Color(0xFFBF5AF2).copy(alpha = 0.15f) else Color(0xFF9842F5).copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Waves, contentDescription = null, tint = if (isDark) Color(0xFFBF5AF2) else Color(0xFF9842F5), modifier = Modifier.size(10.dp))
                                            Text("Pre-wake: -${alarm.smartInterval}m", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = if (isDark) Color(0xFFBF5AF2) else Color(0xFF9842F5)))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Display selected repeat days
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { d ->
                                    val isSelected = alarm.days.contains(d)
                                    Text(
                                        text = d.take(1),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                            color = if (isSelected && alarm.isEnabled) (if (isDark) Color(0xFF34C759) else Color(0xFF24B147)) else Color.Gray.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Switches, Edit and Delete controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    listEditingAlarmId = alarm.id
                                    alarmHour = alarm.hour
                                    alarmMinute = alarm.minute
                                    alarmIsAm = alarm.isAm
                                    alarmLabel = alarm.label
                                    alarmDays = alarm.days
                                    alarmSound = alarm.sound
                                    alarmSmartInterval = alarm.smartInterval
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit alarm parameters",
                                    tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.DarkGray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Switch(
                                checked = alarm.isEnabled,
                                onCheckedChange = { viewModel.toggleAlarmEnabled(alarm.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = if (isDark) Color(0xFF34C759) else Color(0xFF24B147),
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.2f)
                                )
                            )

                            IconButton(
                                onClick = { viewModel.deleteAlarm(alarm.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete alarm configuration",
                                    tint = Color.Red.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
