package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dateString: String, // "YYYY-MM-DD"
    val startHour: Int, // 0 - 23
    val startMinute: Int, // 0 - 59
    val durationMinutes: Int,
    val createdAt: Long = System.currentTimeMillis()
)
