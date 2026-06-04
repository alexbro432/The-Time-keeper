package com.example.data.database

import androidx.room.*
import com.example.data.model.Task
import com.example.data.model.Habit
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeKeeperDao {

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // --- Calendar Events ---
    @Query("SELECT * FROM calendar_events ORDER BY dateString ASC, startHour ASC, startMinute ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEvent)

    @Update
    suspend fun updateCalendarEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteCalendarEvent(event: CalendarEvent)

    // --- Focus Logs ---
    @Query("SELECT * FROM focus_logs ORDER BY timestamp DESC")
    fun getAllFocusLogs(): Flow<List<FocusLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusLog(log: FocusLog)

    // --- Dynamic Cleansing Queries to satisfy 100% empty local state requirement ---
    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()

    @Query("DELETE FROM calendar_events")
    suspend fun clearAllCalendarEvents()

    @Query("DELETE FROM focus_logs")
    suspend fun clearAllFocusLogs()
}
