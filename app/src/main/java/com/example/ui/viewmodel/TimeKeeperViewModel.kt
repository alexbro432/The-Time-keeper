package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusLog
import com.example.data.model.Habit
import com.example.data.model.Task
import com.example.data.repository.TimeKeeperRepository
import com.example.data.api.GeminiRetrofitClient
import com.example.utils.TimeUtils
import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- Luxury Timekeeper Custom Models ---
data class Alarm(
    val id: String = java.util.UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val isAm: Boolean,
    val isEnabled: Boolean = true,
    val label: String = "Wake Up",
    val days: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
    val sound: String = "Serene Forest Spark",
    val smartInterval: Int = 10
)

data class WorldCity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val city: String,
    val zoneId: String,
    val offsetLabel: String
)

class TimeKeeperViewModel(
    application: Application,
    private val repository: TimeKeeperRepository
) : AndroidViewModel(application) {

    // --- Moshi Adapters for Fast Persistence ---
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val alarmAdapter = moshi.adapter<List<Alarm>>(Types.newParameterizedType(List::class.java, Alarm::class.java))
    private val cityAdapter = moshi.adapter<List<WorldCity>>(Types.newParameterizedType(List::class.java, WorldCity::class.java))

    // --- Database Streams ---
    val tasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val habits: StateFlow<List<Habit>> = repository.allHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val calendarEvents: StateFlow<List<CalendarEvent>> = repository.allCalendarEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val focusLogs: StateFlow<List<FocusLog>> = repository.allFocusLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Local SharedPreferences ---
    private val prefs = application.getSharedPreferences("TimeKeeperPrefs", Context.MODE_PRIVATE)

    // --- Onboarding / Auth State ---
    val isOnboardingCompleted = MutableStateFlow(false)
    val isUserLoggedIn = MutableStateFlow(false)
    val currentUserEmail = MutableStateFlow("")
    val currentUserName = MutableStateFlow("")

    // --- Custom Theme Toggle (Settings) ---
    val settingsDarkMode = MutableStateFlow(true) // Start in cool premium dark mode by default!

    // --- Navigation ---
    val currentScreen = MutableStateFlow("welcome") // "welcome", "dashboard", etc.

    // --- Pomodoro State ---
    val pomodoroTimeLeft = MutableStateFlow(1500) // 1500 seconds = 25 minutes
    val pomodoroTotalDuration = MutableStateFlow(1500)
    val pomodoroActiveType = MutableStateFlow("Focus") // "Focus", "ShortBreak", "LongBreak"
    val isPomodoroRunning = MutableStateFlow(false)
    private var pomodoroJob: Job? = null

    // --- AI Assistant States ---
    val aiRecommendationState = MutableStateFlow<String?>(null)
    val isAiLoading = MutableStateFlow(false)

    // --- Motivational Code ---
    val motivationQuote = MutableStateFlow("Focus on being productive instead of busy. Keep moving forward!")

    // --- Celebratory Effect Trigger ---
    val celebrateStreakFlow = MutableSharedFlow<Pair<String, Int>>(replay = 0)

    // --- Dynamic Timekeeper Extension States ---
    val alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val worldCities = MutableStateFlow<List<WorldCity>>(emptyList())

    val countdownTimeLeft = MutableStateFlow(0)
    val countdownTotalDuration = MutableStateFlow(0)
    val isCountdownRunning = MutableStateFlow(false)
    private var countdownJob: Job? = null

    // AI Daily Motivational Quote Notification Hook States
    val isAi5AmNotificationScheduled = MutableStateFlow(false)
    val ai5AmNotificationCategory = MutableStateFlow("Daily Mindfulness & Calm")
    val ai5AmNotificationDays = MutableStateFlow<List<String>>(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
    val ai5AmNotificationLastQuote = MutableStateFlow<String?>(null)
    val isGeneratingAi5AmQuote = MutableStateFlow(false)

    init {
        // Load local saving values
        val savedName = prefs.getString("USER_NAME", "") ?: ""
        val onboardingDone = prefs.getBoolean("ONBOARDING_DONE", false)
        if (onboardingDone && savedName.isNotEmpty()) {
            currentUserName.value = savedName
            isOnboardingCompleted.value = true
            currentScreen.value = "dashboard"
        } else {
            currentScreen.value = "welcome"
        }

        // Load Saved Alarms from Local Storage
        val alarmsJson = prefs.getString("SAVED_ALARMS", "") ?: ""
        if (alarmsJson.isNotEmpty()) {
            try {
                alarms.value = alarmAdapter.fromJson(alarmsJson) ?: emptyList()
            } catch (e: Exception) {
                // Recoverable parsing error fallback
            }
        }
        if (alarms.value.isEmpty()) {
            alarms.value = listOf(
                Alarm(hour = 6, minute = 30, isAm = true, label = "Morning Cardio Workout", days = listOf("Mon", "Wed", "Fri")),
                Alarm(hour = 8, minute = 0, isAm = true, label = "Strategic Standup Meeting", days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")),
                Alarm(hour = 22, minute = 15, isAm = false, label = "Sleek Daily Progression Review", days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            )
            saveAlarmsToPrefs()
        }

        // Load World Clock Custom Cities
        val citiesJson = prefs.getString("SAVED_CITIES", "") ?: ""
        if (citiesJson.isNotEmpty()) {
            try {
                worldCities.value = cityAdapter.fromJson(citiesJson) ?: emptyList()
            } catch (e: Exception) {
                // Recoverable parsing error
            }
        }
        if (worldCities.value.isEmpty()) {
            worldCities.value = listOf(
                WorldCity(city = "Cupertino", zoneId = "America/Los_Angeles", offsetLabel = "UTC -7/8 (PST)"),
                WorldCity(city = "London", zoneId = "Europe/London", offsetLabel = "UTC +0/1 (GMT)"),
                WorldCity(city = "Kolkata", zoneId = "Asia/Kolkata", offsetLabel = "UTC +5.5 (IST)"),
                WorldCity(city = "Tokyo", zoneId = "Asia/Tokyo", offsetLabel = "UTC +9 (JST)"),
                WorldCity(city = "Sydney", zoneId = "Australia/Sydney", offsetLabel = "UTC +10/11 (AEST)")
            )
            saveCitiesToPrefs()
        }

        // Load Notification preferences
        isAi5AmNotificationScheduled.value = prefs.getBoolean("AI_5AM_SCHEDULED", false)
        val savedCategory = prefs.getString("AI_5AM_CATEGORY", "Daily Mindfulness & Calm") ?: "Daily Mindfulness & Calm"
        ai5AmNotificationCategory.value = savedCategory
    }

    fun saveAlarmsToPrefs() {
        try {
            val json = alarmAdapter.toJson(alarms.value)
            prefs.edit().putString("SAVED_ALARMS", json).apply()
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun saveCitiesToPrefs() {
        try {
            val json = cityAdapter.toJson(worldCities.value)
            prefs.edit().putString("SAVED_CITIES", json).apply()
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    // --- Alarm Actions ---
    fun addAlarm(hour: Int, minute: Int, isAm: Boolean, label: String, days: List<String>, sound: String = "Serene Forest Spark", smartInterval: Int = 10) {
        val newAlarm = Alarm(hour = hour, minute = minute, isAm = isAm, label = label, days = days, sound = sound, smartInterval = smartInterval)
        alarms.value = alarms.value + newAlarm
        saveAlarmsToPrefs()
    }

    fun updateAlarm(updated: Alarm) {
        alarms.value = alarms.value.map {
            if (it.id == updated.id) updated else it
        }
        saveAlarmsToPrefs()
    }

    fun deleteAlarm(alarmId: String) {
        alarms.value = alarms.value.filter { it.id != alarmId }
        saveAlarmsToPrefs()
    }

    fun toggleAlarmEnabled(alarmId: String) {
        alarms.value = alarms.value.map {
            if (it.id == alarmId) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveAlarmsToPrefs()
    }

    // --- Custom Cities Actions ---
    fun addWorldCity(city: String, zoneId: String, offsetLabel: String) {
        val newCity = WorldCity(city = city, zoneId = zoneId, offsetLabel = offsetLabel)
        worldCities.value = worldCities.value + newCity
        saveCitiesToPrefs()
    }

    fun removeWorldCity(cityId: String) {
        worldCities.value = worldCities.value.filter { it.id != cityId }
        saveCitiesToPrefs()
    }

    // --- Countdown Timer actions ---
    fun setCountdownDuration(seconds: Int) {
        stopCountdown()
        countdownTimeLeft.value = seconds
        countdownTotalDuration.value = seconds
    }

    fun startCountdown() {
        if (isCountdownRunning.value || countdownTimeLeft.value <= 0) return
        isCountdownRunning.value = true
        countdownJob = viewModelScope.launch {
            while (countdownTimeLeft.value > 0) {
                delay(1000)
                if (isCountdownRunning.value) {
                    countdownTimeLeft.value -= 1
                }
            }
            isCountdownRunning.value = false
        }
    }

    fun pauseCountdown() {
        isCountdownRunning.value = false
        countdownJob?.cancel()
    }

    fun stopCountdown() {
        isCountdownRunning.value = false
        countdownJob?.cancel()
        countdownTimeLeft.value = countdownTotalDuration.value
    }

    // --- AI 5:00 AM Quotes Broadcast Trigger ---
    fun toggleAi5AmNotificationSchedule(scheduled: Boolean) {
        isAi5AmNotificationScheduled.value = scheduled
        prefs.edit().putBoolean("AI_5AM_SCHEDULED", scheduled).apply()
    }

    fun updateAi5AmNotificationCategory(category: String) {
        ai5AmNotificationCategory.value = category
        prefs.edit().putString("AI_5AM_CATEGORY", category).apply()
    }

    fun toggleAi5AmNotificationDay(day: String) {
        val currentDays = ai5AmNotificationDays.value
        ai5AmNotificationDays.value = if (currentDays.contains(day)) {
            currentDays - day
        } else {
            currentDays + day
        }
    }

    fun simulateAi5AmNotificationBroadcast() {
        viewModelScope.launch {
            isGeneratingAi5AmQuote.value = true
            ai5AmNotificationLastQuote.value = null
            val prompt = """
                You are a professional daily productivity AI. Generate a concise, powerful 5:00 AM morning motivation notification quote for the user named ${currentUserName.value}.
                Since their preferred motivation style is "${ai5AmNotificationCategory.value}", write it specifically aligned with that theme.
                Keep it very crisp, high impact, beautifully encouraging, and elite (max 25 words).
                Return only the quote, no extra remarks or markdown.
            """.trimIndent()
            val quote = GeminiRetrofitClient.askGemini(prompt)
            ai5AmNotificationLastQuote.value = quote
            isGeneratingAi5AmQuote.value = false
        }
    }

    fun saveOnboardingAndName(name: String) {
        viewModelScope.launch {
            repository.wipeEverything()
        }
        prefs.edit()
            .putString("USER_NAME", name)
            .putBoolean("ONBOARDING_DONE", true)
            .apply()
        currentUserName.value = name
        isOnboardingCompleted.value = true
        currentScreen.value = "dashboard"
    }

    fun clearLocalDataOnDemand() {
        viewModelScope.launch {
            repository.wipeEverything()
        }
    }

    // --- Task Actions ---
    fun addTask(title: String, description: String, priority: String, dueDate: Long, category: String) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    category = category,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Habit Actions ---
    fun addHabit(title: String, category: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(title = title, category = category))
        }
    }

    fun completeHabitToday(habit: Habit) {
        viewModelScope.launch {
            val todayStr = TimeUtils.getTodayDateString()
            val lastStr = habit.lastCompletedDate

            if (lastStr == todayStr) {
                // Already completed today, do nothing or toggle off
                return@launch
            }

            val yesStr = TimeUtils.getYesterdayDateString()
            val newStreak = if (lastStr == yesStr) {
                habit.currentStreak + 1
            } else {
                1
            }

            // Append today to history string
            val updatedHistory = if (habit.completionHistoryString.isEmpty()) {
                todayStr
            } else {
                "${habit.completionHistoryString},$todayStr"
            }

            repository.updateHabit(
                habit.copy(
                    currentStreak = newStreak,
                    lastCompletedDate = todayStr,
                    completionHistoryString = updatedHistory
                )
            )
            celebrateStreakFlow.emit(Pair(habit.title, newStreak))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // --- Calendar Actions ---
    fun addCalendarEvent(title: String, description: String, dateString: String, startHour: Int, startMinute: Int, duration: Int) {
        viewModelScope.launch {
            repository.insertCalendarEvent(
                CalendarEvent(
                    title = title,
                    description = description,
                    dateString = dateString,
                    startHour = startHour,
                    startMinute = startMinute,
                    durationMinutes = duration
                )
            )
        }
    }

    fun deleteCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(event)
        }
    }

    // --- Pomodoro Controller ---
    fun setPomodoroTimer(minutes: Int, type: String) {
        stopPomodoro()
        pomodoroActiveType.value = type
        pomodoroTimeLeft.value = minutes * 60
        pomodoroTotalDuration.value = minutes * 60
    }

    fun startPomodoro() {
        if (isPomodoroRunning.value) return
        isPomodoroRunning.value = true
        pomodoroJob = viewModelScope.launch {
            while (pomodoroTimeLeft.value > 0) {
                delay(1000)
                pomodoroTimeLeft.value -= 1
            }
            // Timer Finished! Log inside database
            repository.insertFocusLog(
                FocusLog(
                    type = pomodoroActiveType.value,
                    durationMinutes = pomodoroTotalDuration.value / 60
                )
            )
            isPomodoroRunning.value = false
            // Auto switch or reset
            if (pomodoroActiveType.value == "Focus") {
                setPomodoroTimer(5, "ShortBreak") // transition to short break automatically
            } else {
                setPomodoroTimer(25, "Focus")
            }
        }
    }

    fun pausePomodoro() {
        isPomodoroRunning.value = false
        pomodoroJob?.cancel()
    }

    fun stopPomodoro() {
        isPomodoroRunning.value = false
        pomodoroJob?.cancel()
        pomodoroTimeLeft.value = pomodoroTotalDuration.value
    }

    // --- Simulated Account Authentication ---
    fun login(email: String, name: String) {
        currentUserEmail.value = email
        currentUserName.value = name.ifEmpty { "Alex" }
        isUserLoggedIn.value = true
        currentScreen.value = "dashboard"
    }

    fun signup(email: String, name: String) {
        currentUserEmail.value = email
        currentUserName.value = name.ifEmpty { "Alex" }
        isUserLoggedIn.value = true
        currentScreen.value = "dashboard"
    }

    fun logout() {
        viewModelScope.launch {
            repository.wipeEverything()
        }
        prefs.edit().clear().apply()
        currentUserName.value = ""
        isOnboardingCompleted.value = false
        isUserLoggedIn.value = false
        currentScreen.value = "welcome"
    }

    // --- AI PRODUCTIVITY ADVICE (Generative AI) ---
    fun generateAiAdvice(queryType: String) {
        viewModelScope.launch {
            isAiLoading.value = true
            aiRecommendationState.value = null

            // Construct rich prompt referencing actual user data so that the advice is extremely tailored and real!
            val taskList = tasks.value
            val habitsList = habits.value
            val eventList = calendarEvents.value
            val logsList = focusLogs.value

            val totalTasksCount = taskList.size
            val completedTasksCount = taskList.count { it.isCompleted }
            val totalFocusSessions = logsList.filter { it.type == "Focus" }.sumOf { it.durationMinutes }

            val prompt = when (queryType) {
                "study_times" -> """
                    You are the Time Keeper iOS Productivity Coach. Recommend the absolute best study/work times for the user named ${currentUserName.value} based on these current details:
                    - Completed tasks: $completedTasksCount/$totalTasksCount
                    - Total focus log minutes recorded today: $totalFocusSessions minutes
                    - Currently tracking ${habitsList.size} habits.
                    Provide 3 professional, highly elegant iOS styling tips for study optimization. Keep your tone encouraging and elite, like Apple's finest guides. Give direct schedules. Bullet points only, 100-150 words.
                """.trimIndent()
                "analyze_productivity" -> """
                    You are the Time Keeper iOS Productivity Coach. Analyze productivity habits for ${currentUserName.value}.
                    - Tasks: $completedTasksCount of $totalTasksCount completed.
                    - Habits: ${habitsList.joinToString { "${it.title} (Streak: ${it.currentStreak}d)" }}
                    - Calendar Events: ${eventList.size} items scheduled today.
                    Provide a sharp, brilliant analysis of their habit consistency and suggest where they might be losing time. Bullet points only, 100-150 words.
                """.trimIndent()
                "recommend_improvements" -> """
                    You are the Time Keeper iOS Productivity Coach. Recommend schedule improvements for ${currentUserName.value}.
                    - Out of $totalTasksCount tasks, $completedTasksCount are completed. 
                    - Focus sessions: $totalFocusSessions minutes.
                    How can they restructure their daily timeline? Recommend a custom timeblocking layout. Bullet points only, 100-150 words.
                """.trimIndent()
                "motivate" -> """
                    You are the Time Keeper iOS Productivity Coach. Write a powerful, personalized, elite motivation note for ${currentUserName.value}.
                    They have logged $totalFocusSessions focus minutes today and completed $completedTasksCount tasks.
                    Give a short, high-impact 2-sentence quote and an action step. 100 words max.
                """.trimIndent()
                else -> """
                    You are the Time Keeper iOS Productivity Coach. Plan today's schedule for ${currentUserName.value}.
                    - Tasks: ${taskList.joinToString { "${it.title} (${it.priority} Priority)" }}
                    - Events: ${eventList.joinToString { "${it.title} at ${it.startHour}:${it.startMinute}" }}
                    Provide an hour-by-hour perfect tactical daily plan. Keep it actionable and crisp. Bullet points only, 100-150 words.
                """.trimIndent()
            }

            val advice = GeminiRetrofitClient.askGemini(prompt)
            aiRecommendationState.value = advice
            isAiLoading.value = false
        }
    }
}

class TimeKeeperViewModelFactory(
    private val application: Application,
    private val repository: TimeKeeperRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeKeeperViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeKeeperViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
