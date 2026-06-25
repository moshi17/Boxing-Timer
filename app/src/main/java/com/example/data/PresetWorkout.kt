package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preset_workouts")
data class PresetWorkout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rounds: Int,
    val workSeconds: Int,
    val restSeconds: Int,
    val prepSeconds: Int = 10,
    val warningSeconds: Int = 10,
    val isCustom: Boolean = true
)
