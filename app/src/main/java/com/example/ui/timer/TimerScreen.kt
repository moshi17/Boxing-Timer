package com.example.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PresetWorkout
import com.example.data.WorkoutHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val phase by viewModel.currentPhase.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val currentRound by viewModel.currentRound.collectAsState()
    val secondsRemaining by viewModel.secondsRemaining.collectAsState()
    val totalSecondsInPhase by viewModel.totalSecondsInPhase.collectAsState()

    val presets by viewModel.presets.collectAsState()
    val history by viewModel.history.collectAsState()

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetSaveName by remember { mutableStateOf("") }

    // Immersive UI Theme Palette
    val darkBg = Color(0xFF0F0D0C) // Warm coal background
    val carbonSurface = Color(0xFF171412) // Slightly lighter textured slate card
    val steelGreyBorder = Color(0xFF292523) // High-end hardware border accent
    val intensityRed = Color(0xFFFF3131) // K.O. Red
    val softGold = Color(0xFFFFA000) // Warming signal gold
    val slateMuted = Color(0xFF8C8480) // Refined descriptive grey

    // Dynamic phase primary colors
    val phaseColor = when (phase) {
        TimerPhase.PREPARATION -> softGold
        TimerPhase.WORK -> intensityRed
        TimerPhase.REST -> Color(0xFF388E3C) // Vivid emerald green
        TimerPhase.FINISHED -> Color(0xFFFFD700) // Championship Gold
        else -> slateMuted
    }

    val animatedPhaseColor by animateColorAsState(
        targetValue = phaseColor,
        animationSpec = tween(durationMillis = 500),
        label = "phaseColorAnimation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsMartialArts,
                                contentDescription = "KO Timer Logo",
                                tint = intensityRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "K.O. TIMER",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = 1.5.sp,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { viewModel.isSoundEnabled = !viewModel.isSoundEnabled },
                            modifier = Modifier.testTag("sound_toggle")
                        ) {
                            Icon(
                                imageVector = if (viewModel.isSoundEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = "Toggle Audio",
                                tint = if (viewModel.isSoundEnabled) intensityRed else slateMuted
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBg,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = darkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(darkBg),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gym Banner Backdrop with customized gradient overlay
            if (phase == TimerPhase.IDLE) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = "Boxing Gym Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, darkBg.copy(alpha = 0.95f)),
                                        startY = 50f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "TRAIN HARD • WIN THE FIGHT",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Adjust rounds and work intervals below to configure your clock",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            // Main Active Timer Stage
            if (phase != TimerPhase.IDLE) {
                item {
                    ActiveTimerStage(
                        phase = phase,
                        isPaused = isPaused,
                        currentRound = currentRound,
                        secondsRemaining = secondsRemaining,
                        totalSecondsInPhase = totalSecondsInPhase,
                        animatedPhaseColor = animatedPhaseColor,
                        totalRounds = viewModel.activeRounds,
                        restDurationSeconds = viewModel.activeRestSeconds,
                        onPlayPause = {
                            if (isPaused) viewModel.startTimer() else viewModel.pauseTimer()
                        },
                        onStop = { viewModel.resetTimer() }
                    )
                }
            }

            // Standard Preset Templates Quick Tap Selection
            if (phase == TimerPhase.IDLE) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "INTERVAL TEMPLATES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            color = intensityRed,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presets) { preset ->
                                PresetChip(
                                    preset = preset,
                                    isSelected = viewModel.presetName == preset.name,
                                    onSelect = { viewModel.selectPreset(preset) },
                                    onDelete = { viewModel.deletePreset(preset.id) }
                                )
                            }
                        }
                    }
                }

                // Setup Adjuster Form
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "WORKOUT ADJUSTER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            color = intensityRed,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = carbonSurface),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.border(1.dp, steelGreyBorder, RoundedCornerShape(24.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Rounds Adjuster
                                ParameterAdjusterRow(
                                    label = "Rounds",
                                    value = viewModel.activeRounds.toString(),
                                    onDecrease = { viewModel.updateRounds(viewModel.activeRounds - 1) },
                                    onIncrease = { viewModel.updateRounds(viewModel.activeRounds + 1) },
                                    tag = "rounds"
                                )

                                HorizontalDivider(color = steelGreyBorder)

                                // Work Time Adjuster
                                ParameterAdjusterRow(
                                    label = "Round Duration",
                                    value = formatDuration(viewModel.activeWorkSeconds),
                                    onDecrease = { viewModel.updateWorkSeconds(viewModel.activeWorkSeconds - 15) },
                                    onIncrease = { viewModel.updateWorkSeconds(viewModel.activeWorkSeconds + 15) },
                                    tag = "work_seconds"
                                )

                                HorizontalDivider(color = steelGreyBorder)

                                // Rest Time Adjuster
                                ParameterAdjusterRow(
                                    label = "Rest Interval",
                                    value = formatDuration(viewModel.activeRestSeconds),
                                    onDecrease = { viewModel.updateRestSeconds(viewModel.activeRestSeconds - 15) },
                                    onIncrease = { viewModel.updateRestSeconds(viewModel.activeRestSeconds + 15) },
                                    tag = "rest_seconds"
                                )

                                HorizontalDivider(color = steelGreyBorder)

                                // Prep Time Adjuster
                                ParameterAdjusterRow(
                                    label = "Preparation Time",
                                    value = "${viewModel.activePrepSeconds}s",
                                    onDecrease = { viewModel.updatePrepSeconds(viewModel.activePrepSeconds - 1) },
                                    onIncrease = { viewModel.updatePrepSeconds(viewModel.activePrepSeconds + 1) },
                                    tag = "prep_seconds"
                                )

                                HorizontalDivider(color = steelGreyBorder)

                                // Warning Time Adjuster
                                ParameterAdjusterRow(
                                    label = "Warning Signal",
                                    value = "${viewModel.activeWarningSeconds}s",
                                    onDecrease = { viewModel.updateWarningSeconds(viewModel.activeWarningSeconds - 1) },
                                    onIncrease = { viewModel.updateWarningSeconds(viewModel.activeWarningSeconds + 1) },
                                    tag = "warning_seconds"
                                )
                            }
                        }
                    }
                }

                // Control Action Buttons (Start Timer / Save Preset)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startTimer() },
                            colors = ButtonDefaults.buttonColors(containerColor = intensityRed),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(56.dp)
                                .testTag("start_timer_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "START WORKOUT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = {
                                presetSaveName = if (viewModel.presetName == "Custom") "" else viewModel.presetName
                                showSavePresetDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF231E1C)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .border(1.dp, steelGreyBorder, RoundedCornerShape(16.dp))
                                .testTag("save_preset_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save Preset", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SAVE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Training Logs
            if (phase == TimerPhase.IDLE) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = "History", tint = softGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TRAINING LOGS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.5.sp,
                                    color = softGold
                                )
                            }
                            if (history.isNotEmpty()) {
                                Text(
                                    text = "CLEAR ALL",
                                    fontSize = 11.sp,
                                    color = intensityRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.clearHistory() }
                                        .padding(4.dp)
                                )
                            }
                        }

                        if (history.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = carbonSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, steelGreyBorder, RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "No history",
                                        tint = slateMuted.copy(alpha = 0.3f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No workouts recorded yet",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Complete a round-based boxing session to automatically record stats.",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        color = slateMuted,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                history.take(8).forEach { log ->
                                    HistoryLogCard(log)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { showSavePresetDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = carbonSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .border(1.dp, steelGreyBorder, RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Save Workout Template",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Enter a name to save this configuration as a quick-tap template.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = slateMuted
                    )

                    OutlinedTextField(
                        value = presetSaveName,
                        onValueChange = { presetSaveName = it },
                        placeholder = { Text("e.g. Heavy Bag Cardio", color = slateMuted.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = intensityRed,
                            unfocusedBorderColor = steelGreyBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSavePresetDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (presetSaveName.isNotBlank()) {
                                    viewModel.saveAsPreset(presetSaveName)
                                    showSavePresetDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = intensityRed),
                            modifier = Modifier.weight(1.5f).testTag("confirm_save_preset"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Template", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTimerStage(
    phase: TimerPhase,
    isPaused: Boolean,
    currentRound: Int,
    secondsRemaining: Int,
    totalSecondsInPhase: Int,
    animatedPhaseColor: Color,
    totalRounds: Int,
    restDurationSeconds: Int,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    val progress = if (totalSecondsInPhase > 0) {
        secondsRemaining.toFloat() / totalSecondsInPhase.toFloat()
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timerProgressAnimation"
    )

    val phaseLabel = when (phase) {
        TimerPhase.PREPARATION -> "PREPARE"
        TimerPhase.WORK -> "WORK PHASE"
        TimerPhase.REST -> "REST INTERVAL"
        TimerPhase.FINISHED -> "WORKOUT COMPLETE"
        else -> ""
    }

    val carbonSurface = Color(0xFF171412)
    val steelGreyBorder = Color(0xFF292523)
    val intensityRed = Color(0xFFFF3131)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Display Stage Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(carbonSurface)
                .border(1.dp, steelGreyBorder, RoundedCornerShape(32.dp))
                .padding(vertical = 32.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Neon Atmospheric Glow
            if (phase == TimerPhase.WORK && !isPaused) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(intensityRed.copy(alpha = 0.12f), Color.Transparent),
                                radius = 350f
                            )
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // High-visibility status badge with flashing indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(animatedPhaseColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(animatedPhaseColor, CircleShape)
                    )
                    Text(
                        text = phaseLabel,
                        color = animatedPhaseColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                }

                // Super massive Display
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(180.dp)
                ) {
                    if (phase == TimerPhase.FINISHED) {
                        Text(
                            text = "CHAMP",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = animatedPhaseColor,
                            letterSpacing = 1.sp
                        )
                    } else {
                        Text(
                            text = formatDuration(secondsRemaining),
                            fontSize = 84.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-2).sp
                        )
                    }
                }

                // Subtle Progress Dots Bar resembling mockup design style
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    repeat(4) { index ->
                        val isActive = animatedProgress >= (index / 4f)
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(32.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isActive) animatedPhaseColor else steelGreyBorder
                                )
                        )
                    }
                }
            }
        }

        // Side-by-Side Symmetrical Grid Stats Cards (Match Mockup style!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Round Stat
            Card(
                colors = CardDefaults.cardColors(containerColor = carbonSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, steelGreyBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ROUND",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", currentRound),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = " / $totalRounds",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                }
            }

            // Card 2: Rest Interval Period
            Card(
                colors = CardDefaults.cardColors(containerColor = carbonSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, steelGreyBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "REST PERIOD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatDuration(restDurationSeconds),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // Docked Action Controllers Footer Panel (Glassmorphic look)
        Card(
            colors = CardDefaults.cardColors(containerColor = carbonSurface),
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(listOf(steelGreyBorder, Color.Transparent)),
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                // Return/Reset
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(56.dp)
                        .border(1.dp, steelGreyBorder, CircleShape)
                        .background(Color.White.copy(alpha = 0.03f), CircleShape)
                        .testTag("reset_timer_active")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Signature rounded square play/pause button (Mockup theme focal point)
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(animatedPhaseColor)
                        .testTag("play_pause_timer_active")
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Placeholder / balanced layout container
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Transparent, CircleShape)
                )
            }
        }
    }
}

@Composable
fun ParameterAdjusterRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    tag: String
) {
    val steelGreyBorder = Color(0xFF292523)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, steelGreyBorder, CircleShape)
                    .testTag("decrease_$tag")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Decrease $label",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier.width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            IconButton(
                onClick = onIncrease,
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, steelGreyBorder, CircleShape)
                    .testTag("increase_$tag")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Increase $label",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PresetChip(
    preset: PresetWorkout,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val intensityRed = Color(0xFFFF3131)
    val steelGreyBorder = Color(0xFF292523)
    val chipBg = if (isSelected) intensityRed else Color(0xFF171412)
    val borderCol = if (isSelected) Color.Transparent else steelGreyBorder
    val textCol = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)

    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = preset.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textCol,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${preset.rounds} Rds • ${formatDuration(preset.workSeconds)}",
                fontSize = 10.sp,
                color = textCol.copy(alpha = 0.6f)
            )
        }

        if (preset.isCustom) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete custom preset",
                tint = textCol.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun HistoryLogCard(log: WorkoutHistory) {
    val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(log.timestamp))
    val steelGreyBorder = Color(0xFF292523)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171412)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, steelGreyBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.presetName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = dateString,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${log.roundsCompleted}/${log.totalRounds} Rds",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(0xFFFFD700)
                )
                Text(
                    text = "${log.totalWorkTimeSeconds / 60}m work",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Format duration helper (e.g. 180 seconds -> "03:00")
fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
