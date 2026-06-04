package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_logs")
data class FocusLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Focus", "ShortBreak", "LongBreak"
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
