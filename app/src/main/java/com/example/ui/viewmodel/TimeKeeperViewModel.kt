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

class TimeKeeperViewModel(
    application: Application,
    private val repository: TimeKeeperRepository
) : AndroidViewModel(application) {

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
