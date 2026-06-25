package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BoxingRepository(private val boxingDao: BoxingDao) {

    val allPresets: Flow<List<PresetWorkout>> = boxingDao.getAllPresets()
        .flowOn(Dispatchers.IO)

    val workoutHistory: Flow<List<WorkoutHistory>> = boxingDao.getWorkoutHistory()
        .flowOn(Dispatchers.IO)

    suspend fun insertPreset(preset: PresetWorkout) = withContext(Dispatchers.IO) {
        boxingDao.insertPreset(preset)
    }

    suspend fun deletePresetById(id: Int) = withContext(Dispatchers.IO) {
        boxingDao.deletePresetById(id)
    }

    suspend fun insertHistory(history: WorkoutHistory) = withContext(Dispatchers.IO) {
        boxingDao.insertHistory(history)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        boxingDao.clearHistory()
    }
}
