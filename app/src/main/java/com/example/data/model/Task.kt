package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // "High", "Medium", "Low"
    val dueDate: Long, // timestamp
    val category: String, // "Work", "Personal", "Health", "Study", etc.
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
