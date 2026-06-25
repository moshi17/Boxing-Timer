package com.example.ui.timer

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BoxingSoundPlayer(context: Context) {
    private val applicationContext = context.applicationContext
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            // STREAM_ALARM or STREAM_NOTIFICATION for high auditability
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            Log.e("BoxingSoundPlayer", "Failed to initialize ToneGenerator: ${e.message}")
        }
    }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playRoundStart(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            repeat(3) {
                // TONE_SUP_PIP or TONE_CDMA_ALERT_CALL_GUARD gives a high-pitched ring resembling a bell
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 200)
                vibrate(150)
                delay(300)
            }
        }
    }

    fun playRoundEnd(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 800)
            vibrate(500)
        }
    }

    fun playWarningClapper(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            repeat(3) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                vibrate(60)
                delay(200)
            }
        }
    }

    fun playCountdownBeep() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        vibrate(80)
    }

    private fun vibrate(durationMs: Long) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BoxingSoundPlayer", "Failed to vibrate: ${e.message}")
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
