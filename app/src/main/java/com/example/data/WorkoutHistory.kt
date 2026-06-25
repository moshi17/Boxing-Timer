package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val presetName: String,
    val roundsCompleted: Int,
    val totalRounds: Int,
    val totalWorkTimeSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)
