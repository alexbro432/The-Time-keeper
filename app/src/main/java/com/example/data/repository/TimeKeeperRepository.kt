package com.example.data.repository

import com.example.data.database.TimeKeeperDao
import com.example.data.model.Task
import com.example.data.model.Habit
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusLog
import kotlinx.coroutines.flow.Flow

class TimeKeeperRepository(private val dao: TimeKeeperDao) {

    // --- Tasks ---
    val allTasks: Flow<List<Task>> = dao.getAllTasks()

    suspend fun insertTask(task: Task) = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: Int) = dao.deleteTaskById(id)

    // --- Habits ---
    val allHabits: Flow<List<Habit>> = dao.getAllHabits()

    suspend fun insertHabit(habit: Habit) = dao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    // --- Calendar Events ---
    val allCalendarEvents: Flow<List<CalendarEvent>> = dao.getAllCalendarEvents()

    suspend fun insertCalendarEvent(event: CalendarEvent) = dao.insertCalendarEvent(event)
    suspend fun updateCalendarEvent(event: CalendarEvent) = dao.updateCalendarEvent(event)
    suspend fun deleteCalendarEvent(event: CalendarEvent) = dao.deleteCalendarEvent(event)

    // --- Focus Logs ---
    val allFocusLogs: Flow<List<FocusLog>> = dao.getAllFocusLogs()

    suspend fun insertFocusLog(log: FocusLog) = dao.insertFocusLog(log)

    // --- Dynamic Cleansing Actions ---
    suspend fun wipeEverything() {
        dao.clearAllTasks()
        dao.clearAllHabits()
        dao.clearAllCalendarEvents()
        dao.clearAllFocusLogs()
    }
}
