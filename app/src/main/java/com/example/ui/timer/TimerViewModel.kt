package com.example.ui.timer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BoxingDatabase
import com.example.data.BoxingRepository
import com.example.data.PresetWorkout
import com.example.data.WorkoutHistory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimerPhase {
    IDLE,
    PREPARATION,
    WORK,
    REST,
    FINISHED
}

class TimerViewModel(
    application: Application,
    private val repository: BoxingRepository
) : AndroidViewModel(application) {

    // Sound player
    private val soundPlayer = BoxingSoundPlayer(application)

    // Observables from DB
    val presets: StateFlow<List<PresetWorkout>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<WorkoutHistory>> = repository.workoutHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Parameters (modifiable in IDLE mode)
    var activeRounds by mutableStateOf(3)
        private set
    var activeWorkSeconds by mutableStateOf(180) // 3 mins default
        private set
    var activeRestSeconds by mutableStateOf(60) // 1 min default
    var activePrepSeconds by mutableStateOf(10) // 10s default
        private set
    var activeWarningSeconds by mutableStateOf(10) // 10s default
        private set

    var presetName by mutableStateOf("Custom")
        private set

    // Audio Preference
    var isSoundEnabled by mutableStateOf(true)

    // Current Live Timer States
    private val _currentPhase = MutableStateFlow(TimerPhase.IDLE)
    val currentPhase: StateFlow<TimerPhase> = _currentPhase.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(0)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private val _totalSecondsInPhase = MutableStateFlow(0)
    val totalSecondsInPhase: StateFlow<Int> = _totalSecondsInPhase.asStateFlow()

    // Keep track of statistics
    private var roundsCompletedInSession = 0
    private var totalSessionWorkSeconds = 0

    // Timer Job
    private var timerJob: Job? = null

    fun selectPreset(preset: PresetWorkout) {
        if (_currentPhase.value != TimerPhase.IDLE) return
        presetName = preset.name
        activeRounds = preset.rounds
        activeWorkSeconds = preset.workSeconds
        activeRestSeconds = preset.restSeconds
        activePrepSeconds = preset.prepSeconds
        activeWarningSeconds = preset.warningSeconds
    }

    fun updateRounds(value: Int) {
        if (_currentPhase.value == TimerPhase.IDLE) {
            activeRounds = value.coerceIn(1, 15)
            presetName = "Custom"
        }
    }

    fun updateWorkSeconds(value: Int) {
        if (_currentPhase.value == TimerPhase.IDLE) {
            activeWorkSeconds = value.coerceIn(10, 600)
            presetName = "Custom"
        }
    }

    fun updateRestSeconds(value: Int) {
        if (_currentPhase.value == TimerPhase.IDLE) {
            activeRestSeconds = value.coerceIn(0, 300)
            presetName = "Custom"
        }
    }

    fun updatePrepSeconds(value: Int) {
        if (_currentPhase.value == TimerPhase.IDLE) {
            activePrepSeconds = value.coerceIn(0, 60)
            presetName = "Custom"
        }
    }

    fun updateWarningSeconds(value: Int) {
        if (_currentPhase.value == TimerPhase.IDLE) {
            activeWarningSeconds = value.coerceIn(0, 30)
            presetName = "Custom"
        }
    }

    fun startTimer() {
        if (timerJob != null && _isPaused.value) {
            // Resume
            _isPaused.value = false
            return
        }

        // Cancel previous if any
        timerJob?.cancel()
        _isPaused.value = false
        roundsCompletedInSession = 0
        totalSessionWorkSeconds = 0

        timerJob = viewModelScope.launch {
            // Phase 1: Preparation if greater than 0
            _currentRound.value = 1
            if (activePrepSeconds > 0) {
                runPhase(TimerPhase.PREPARATION, activePrepSeconds)
            }

            // Loop through all rounds
            for (r in 1..activeRounds) {
                _currentRound.value = r
                
                // Work Phase
                runPhase(TimerPhase.WORK, activeWorkSeconds)
                roundsCompletedInSession = r
                totalSessionWorkSeconds += activeWorkSeconds

                // Rest Phase (only if there are more rounds and rest is specified)
                if (r < activeRounds && activeRestSeconds > 0) {
                    runPhase(TimerPhase.REST, activeRestSeconds)
                }
            }

            // Finished
            _currentPhase.value = TimerPhase.FINISHED
            _secondsRemaining.value = 0
            _totalSecondsInPhase.value = 0
            
            if (isSoundEnabled) {
                soundPlayer.playRoundEnd(this)
            }

            // Log this workout to history
            repository.insertHistory(
                WorkoutHistory(
                    presetName = presetName,
                    roundsCompleted = roundsCompletedInSession,
                    totalRounds = activeRounds,
                    totalWorkTimeSeconds = totalSessionWorkSeconds
                )
            )
        }
    }

    private suspend fun runPhase(phase: TimerPhase, durationSeconds: Int) {
        _currentPhase.value = phase
        _secondsRemaining.value = durationSeconds
        _totalSecondsInPhase.value = durationSeconds

        // Trigger Phase Start Sound
        if (isSoundEnabled) {
            when (phase) {
                TimerPhase.PREPARATION -> soundPlayer.playCountdownBeep()
                TimerPhase.WORK -> soundPlayer.playRoundStart(viewModelScope)
                TimerPhase.REST -> soundPlayer.playRoundEnd(viewModelScope)
                else -> {}
            }
        }

        while (_secondsRemaining.value > 0) {
            if (_isPaused.value) {
                delay(100)
                continue
            }

            delay(1000)
            if (_isPaused.value) continue

            _secondsRemaining.value -= 1

            // Trigger specific warning beeps or sounds
            if (isSoundEnabled) {
                val remaining = _secondsRemaining.value
                if (phase == TimerPhase.PREPARATION && remaining in 1..3) {
                    soundPlayer.playCountdownBeep()
                } else if (phase == TimerPhase.WORK && remaining == activeWarningSeconds && remaining > 0) {
                    soundPlayer.playWarningClapper(viewModelScope)
                } else if (phase == TimerPhase.REST && remaining in 1..3) {
                    soundPlayer.playCountdownBeep()
                }
            }
        }
    }

    fun pauseTimer() {
        if (_currentPhase.value != TimerPhase.IDLE && _currentPhase.value != TimerPhase.FINISHED) {
            _isPaused.value = true
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        _isPaused.value = false
        _currentPhase.value = TimerPhase.IDLE
        _currentRound.value = 1
        _secondsRemaining.value = 0
        _totalSecondsInPhase.value = 0
    }

    fun saveAsPreset(name: String) {
        viewModelScope.launch {
            repository.insertPreset(
                PresetWorkout(
                    name = name,
                    rounds = activeRounds,
                    workSeconds = activeWorkSeconds,
                    restSeconds = activeRestSeconds,
                    prepSeconds = activePrepSeconds,
                    warningSeconds = activeWarningSeconds,
                    isCustom = true
                )
            )
        }
    }

    fun deletePreset(id: Int) {
        viewModelScope.launch {
            repository.deletePresetById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.release()
    }

    // Factory
    class Factory(
        private val application: Application,
        private val repository: BoxingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
                return TimerViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
