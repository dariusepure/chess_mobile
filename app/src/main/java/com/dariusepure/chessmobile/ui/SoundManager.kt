package com.dariusepure.chessmobile.ui

import android.media.AudioManager
import android.media.ToneGenerator

object SoundManager {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

    fun playMoveSound() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
    }

    fun playCaptureSound() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 100)
    }

    fun playCheckSound() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
    }
}
