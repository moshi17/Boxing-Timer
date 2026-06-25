package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxingDao {
    // Preset queries
    @Query("SELECT * FROM preset_workouts ORDER BY id ASC")
    fun getAllPresets(): Flow<List<PresetWorkout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetWorkout): Long

    @Query("DELETE FROM preset_workouts WHERE id = :id")
    suspend fun deletePresetById(id: Int)

    // History queries
    @Query("SELECT * FROM workout_history ORDER BY timestamp DESC")
    fun getWorkoutHistory(): Flow<List<WorkoutHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WorkoutHistory): Long

    @Query("DELETE FROM workout_history")
    suspend fun clearHistory()
}
