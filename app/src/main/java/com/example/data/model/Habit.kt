package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Health", "Mind", "Work", etc.
    val currentStreak: Int = 0,
    val lastCompletedDate: String = "", // "YYYY-MM-DD" style
    val completionHistoryString: String = "", // comma-separated dates "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)
